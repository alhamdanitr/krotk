import { Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class DashboardService {
  constructor(private readonly prisma: PrismaService) {}

  async getOverview(tenantId: string) {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const todayEnd = new Date();
    todayEnd.setHours(23, 59, 59, 999);
    const firstDayOfMonth = new Date(today.getFullYear(), today.getMonth(), 1);

    const [
      todaySmsTransactions,
      monthSmsTransactions,
      todaySmsDeposits,
      pendingCount,
      availableCards,
      todayDistributorSales,
      monthDistributorSales,
      todayDistributorProfit,
      subscription,
      inventoryByCategory,
    ] = await Promise.all([
      // SMS Engine stats
      this.prisma.transaction.aggregate({
        where: { tenantId, createdAt: { gte: today, lte: todayEnd } },
        _sum: { amount: true }, _count: { id: true },
      }),
      this.prisma.transaction.aggregate({
        where: { tenantId, createdAt: { gte: firstDayOfMonth } },
        _sum: { amount: true }, _count: { id: true },
      }),
      this.prisma.deposit.aggregate({
        where: { tenantId, createdAt: { gte: today, lte: todayEnd } },
        _sum: { amount: true }, _count: { id: true },
      }),
      this.prisma.pendingApproval.count({ where: { tenantId, status: 'pending' } }),
      this.prisma.card.count({ where: { tenantId, isUsed: false } }),

      // Distributor stats
      this.prisma.distributorSale.aggregate({
        where: { tenantId, createdAt: { gte: today, lte: todayEnd } },
        _sum: { totalAmount: true, profit: true }, _count: { id: true },
      }),
      this.prisma.distributorSale.aggregate({
        where: { tenantId, createdAt: { gte: firstDayOfMonth } },
        _sum: { totalAmount: true, profit: true }, _count: { id: true },
      }),
      this.prisma.distributorExpense.aggregate({
        where: { tenantId, expenseDate: { gte: today, lte: todayEnd } },
        _sum: { amount: true },
      }),

      // Subscription
      this.prisma.subscription.findUnique({
        where: { tenantId },
        include: { plan: true },
      }),

      // Inventory breakdown by category
      this.prisma.card.groupBy({
        by: ['categoryValue'],
        where: { tenantId, isUsed: false },
        _count: { id: true },
        orderBy: { categoryValue: 'asc' },
      }),
    ]);

    // Total receivables
    const creditSalesTotal = await this.prisma.distributorSale.aggregate({
      where: { tenantId, isCredit: true },
      _sum: { totalAmount: true },
    });
    const paymentsTotal = await this.prisma.distributorPayment.aggregate({
      where: { tenantId },
      _sum: { amount: true },
    });
    const totalReceivables = Math.max(
      0,
      (creditSalesTotal._sum.totalAmount ?? 0) - (paymentsTotal._sum.amount ?? 0),
    );

    const todayExpenses = todayDistributorProfit._sum.amount ?? 0;
    const todayGrossProfit = todayDistributorSales._sum.profit ?? 0;
    const todayNetProfit = todayGrossProfit - todayExpenses;

    return {
      smsEngine: {
        today: {
          salesAmount: todaySmsTransactions._sum.amount ?? 0,
          salesCount: todaySmsTransactions._count.id,
          depositsAmount: todaySmsDeposits._sum.amount ?? 0,
          depositsCount: todaySmsDeposits._count.id,
        },
        month: {
          salesAmount: monthSmsTransactions._sum.amount ?? 0,
          salesCount: monthSmsTransactions._count.id,
        },
      },
      distributor: {
        today: {
          salesAmount: todayDistributorSales._sum.totalAmount ?? 0,
          salesCount: todayDistributorSales._count.id,
          grossProfit: todayGrossProfit,
          expenses: todayExpenses,
          netProfit: todayNetProfit,
        },
        month: {
          salesAmount: monthDistributorSales._sum.totalAmount ?? 0,
          salesCount: monthDistributorSales._count.id,
          grossProfit: monthDistributorSales._sum.profit ?? 0,
        },
      },
      inventory: {
        totalAvailable: availableCards,
        byCategory: inventoryByCategory.map((c) => ({
          categoryValue: c.categoryValue,
          count: c._count.id,
          isLow: c._count.id < 10,
        })),
      },
      pendingApprovals: pendingCount,
      totalReceivables,
      subscription: subscription
        ? {
            planName: subscription.plan.name,
            expiryDate: subscription.expiryDate,
            daysRemaining: Math.ceil(
              (subscription.expiryDate.getTime() - Date.now()) / (1000 * 60 * 60 * 24),
            ),
            status: subscription.status,
          }
        : null,
    };
  }

  async getAlerts(tenantId: string) {
    const alerts: { type: string; severity: string; message: string; data?: any }[] = [];

    // 1. Pending Approvals Alert
    const pendingCount = await this.prisma.pendingApproval.count({
      where: { tenantId, status: 'pending' },
    });
    if (pendingCount > 0) {
      alerts.push({ type: 'pending_approvals', severity: 'red', message: `${pendingCount} طلب معلق ينتظر الموافقة` });
    }

    // 2. Low Inventory Alerts
    const lowStock = await this.prisma.card.groupBy({
      by: ['categoryValue'],
      where: { tenantId, isUsed: false },
      _count: { id: true },
      having: { id: { _count: { lt: 10 } } },
    });
    for (const cat of lowStock) {
      alerts.push({ type: 'low_inventory', severity: 'orange', message: `مخزون منخفض: فئة ${cat.categoryValue} — فقط ${cat._count.id} كروت` });
    }

    // 3. Subscription Expiry Alert
    const sub = await this.prisma.subscription.findUnique({ where: { tenantId } });
    if (sub) {
      const daysLeft = Math.ceil((sub.expiryDate.getTime() - Date.now()) / (1000 * 60 * 60 * 24));
      if (daysLeft <= 7 && daysLeft > 0) {
        alerts.push({ type: 'subscription_expiry', severity: 'blue', message: `الاشتراك ينتهي خلال ${daysLeft} أيام` });
      } else if (daysLeft <= 0) {
        alerts.push({ type: 'subscription_expired', severity: 'red', message: 'الاشتراك منتهي — يرجى التجديد' });
      }
    }

    // 4. Over-Limit Credit Customers
    const creditCustomers = await this.prisma.distributorCustomer.findMany({
      where: { tenantId, isActive: true, creditLimit: { gt: 0 } },
    });
    for (const c of creditCustomers) {
      const creditSales = await this.prisma.distributorSale.aggregate({
        where: { tenantId, customerId: c.id, isCredit: true },
        _sum: { totalAmount: true },
      });
      const payments = await this.prisma.distributorPayment.aggregate({
        where: { tenantId, customerId: c.id },
        _sum: { amount: true },
      });
      const debt = Math.max(0, (creditSales._sum.totalAmount ?? 0) - (payments._sum.amount ?? 0));
      const ratio = c.creditLimit > 0 ? debt / c.creditLimit : 0;

      if (ratio > 1) {
        alerts.push({ type: 'credit_over_limit', severity: 'red', message: `${c.name} تجاوز حد الائتمان (${debt} / ${c.creditLimit})`, data: { customerId: c.id } });
      } else if (ratio >= 0.8) {
        alerts.push({ type: 'credit_near_limit', severity: 'yellow', message: `${c.name} اقترب من حد الائتمان (${Math.round(ratio * 100)}%)`, data: { customerId: c.id } });
      }
    }

    return alerts;
  }

  async getChartData(tenantId: string, days: number) {
    const data: { date: string; smsSales: number; distributorSales: number; distributorProfit: number; deposits: number }[] = [];

    for (let i = days - 1; i >= 0; i--) {
      const d = new Date();
      d.setDate(d.getDate() - i);
      d.setHours(0, 0, 0, 0);
      const nextD = new Date(d);
      nextD.setDate(d.getDate() + 1);

      const [smsSales, deposits, distSales] = await Promise.all([
        this.prisma.transaction.aggregate({
          where: { tenantId, createdAt: { gte: d, lt: nextD } },
          _sum: { amount: true },
        }),
        this.prisma.deposit.aggregate({
          where: { tenantId, createdAt: { gte: d, lt: nextD } },
          _sum: { amount: true },
        }),
        this.prisma.distributorSale.aggregate({
          where: { tenantId, createdAt: { gte: d, lt: nextD } },
          _sum: { totalAmount: true, profit: true },
        }),
      ]);

      data.push({
        date: d.toISOString().split('T')[0],
        smsSales: smsSales._sum.amount ?? 0,
        deposits: deposits._sum.amount ?? 0,
        distributorSales: distSales._sum.totalAmount ?? 0,
        distributorProfit: distSales._sum.profit ?? 0,
      });
    }
    return data;
  }

  async getRecentActivity(tenantId: string, limit = 10) {
    const [smsTransactions, distributorSales] = await Promise.all([
      this.prisma.transaction.findMany({
        where: { tenantId },
        orderBy: { createdAt: 'desc' },
        take: limit,
      }),
      this.prisma.distributorSale.findMany({
        where: { tenantId },
        orderBy: { createdAt: 'desc' },
        take: limit,
        include: { customer: { select: { name: true } } },
      }),
    ]);

    const merged = [
      ...smsTransactions.map((t) => ({ type: 'sms_transaction', date: t.createdAt, amount: t.amount, detail: `شحن للرقم ${t.recipientPhone}` })),
      ...distributorSales.map((s) => ({ type: 'distributor_sale', date: s.createdAt, amount: s.totalAmount, detail: `بيع ${s.quantity} × ${s.cardType} ${s.categoryValue}${s.customer ? ` لـ ${s.customer.name}` : ''}` })),
    ]
      .sort((a, b) => b.date.getTime() - a.date.getTime())
      .slice(0, limit);

    return merged;
  }
}
