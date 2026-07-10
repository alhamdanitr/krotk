import { Injectable } from '@nestjs/common';
import { PrismaService } from '../../prisma/prisma.service';
import { DateRangeDto } from '../../common/dto/date-range.dto';
import { PaginationDto } from '../../common/dto/pagination.dto';
import { CreateExpenseDto } from '../dto/create-expense.dto';
import { CreateCapitalDto, RecordPaymentDto } from '../dto/finance.dto';
import { DistributorCustomersService } from '../customers/customers.service';

@Injectable()
export class DistributorFinanceService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly customersService: DistributorCustomersService,
  ) {}

  // ─── 1. Financial Dashboard (Quick Summary) ───────────────────────────────

  async getFinanceDashboard(tenantId: string) {
    const todayRange = new DateRangeDto();
    todayRange.period = 'today';
    const monthRange = new DateRangeDto();
    monthRange.period = 'month';

    const [todayProfit, monthProfit, todayExpenses, totalReceivables, capital] =
      await Promise.all([
        this._aggregateSalesProfit(tenantId, todayRange),
        this._aggregateSalesProfit(tenantId, monthRange),
        this._aggregateExpenses(tenantId, todayRange),
        this._computeTotalReceivables(tenantId),
        this._computeCapital(tenantId),
      ]);

    return {
      today: {
        grossProfit: todayProfit.grossProfit,
        totalSales: todayProfit.totalSales,
        cogs: todayProfit.cogs,
        expenses: todayExpenses,
        netProfit: todayProfit.grossProfit - todayExpenses,
      },
      month: {
        grossProfit: monthProfit.grossProfit,
        totalSales: monthProfit.totalSales,
        netProfit: monthProfit.grossProfit,
        marginPercent: monthProfit.marginPercent,
      },
      totalReceivables,
      capital,
    };
  }

  // ─── 2. Profit Board (احترافي) ────────────────────────────────────────────

  async getProfits(tenantId: string, query: DateRangeDto) {
    const { gte, lte } = query.getDateRange();

    const [salesAgg, topProducts, topCustomers] = await Promise.all([
      this.prisma.distributorSale.aggregate({
        where: { tenantId, createdAt: { gte, lte } },
        _sum: { profit: true, totalAmount: true, quantity: true },
        _count: { id: true },
      }),
      // أفضل الأصناف ربحاً
      this.prisma.distributorSale.groupBy({
        by: ['categoryValue', 'cardType'],
        where: { tenantId, createdAt: { gte, lte } },
        _sum: { profit: true, quantity: true },
        orderBy: { _sum: { profit: 'desc' } },
        take: 5,
      }),
      // أفضل العملاء
      this.prisma.distributorSale.groupBy({
        by: ['customerId'],
        where: { tenantId, createdAt: { gte, lte }, customerId: { not: null } },
        _sum: { totalAmount: true, profit: true },
        orderBy: { _sum: { totalAmount: 'desc' } },
        take: 5,
      }),
    ]);

    const grossProfit = salesAgg._sum.profit ?? 0;
    const totalSales = salesAgg._sum.totalAmount ?? 0;
    const totalQty = salesAgg._sum.quantity ?? 0;
    const cogs = totalSales - grossProfit;
    const marginPercent = totalSales > 0 ? parseFloat(((grossProfit / totalSales) * 100).toFixed(2)) : 0;

    // حساب المصروفات في الفترة لصافي الربح
    const expensesAgg = await this.prisma.distributorExpense.aggregate({
      where: { tenantId, expenseDate: { gte, lte } },
      _sum: { amount: true },
    });
    const totalExpenses = expensesAgg._sum.amount ?? 0;
    const netProfit = grossProfit - totalExpenses;

    // حساب متوسط الربح اليومي
    const diffDays = Math.max(1, Math.ceil((lte.getTime() - gte.getTime()) / (1000 * 60 * 60 * 24)));
    const avgDailyProfit = parseFloat((grossProfit / diffDays).toFixed(0));

    // Enrich top customers with names
    const enrichedCustomers = await Promise.all(
      topCustomers.map(async (c) => {
        if (!c.customerId) return null;
        const customer = await this.prisma.distributorCustomer.findUnique({
          where: { id: c.customerId },
          select: { id: true, name: true, phone: true },
        });
        return { ...c, customer };
      }),
    );

    return {
      period: { from: gte.toISOString(), to: lte.toISOString() },
      summary: {
        grossProfit,
        netProfit,
        totalSales,
        cogs,
        totalExpenses,
        marginPercent,
        totalQuantity: totalQty,
        salesCount: salesAgg._count.id,
        avgDailyProfit,
      },
      topProducts,
      topCustomers: enrichedCustomers.filter(Boolean),
    };
  }

  // ─── 3. Cash Flow ─────────────────────────────────────────────────────────

  async getCashFlow(tenantId: string, query: DateRangeDto) {
    const { gte, lte } = query.getDateRange();

    // الرصيد الافتتاحي: حركات رأس المال قبل الفترة
    const openingCapital = await this.prisma.distributorCapital.groupBy({
      by: ['type'],
      where: { tenantId, createdAt: { lt: gte } },
      _sum: { amount: true },
    });
    const openingInjections = openingCapital.find((c) => c.type === 'injection')?._sum?.amount ?? 0;
    const openingWithdrawals = openingCapital.find((c) => c.type === 'withdrawal')?._sum?.amount ?? 0;
    const openingBalance = openingInjections - openingWithdrawals;

    // المقبوضات خلال الفترة
    const [cashSalesAgg, collectionsAgg, injectionAgg] = await Promise.all([
      this.prisma.distributorSale.aggregate({
        where: { tenantId, isCredit: false, createdAt: { gte, lte } },
        _sum: { totalAmount: true },
      }),
      this.prisma.distributorPayment.aggregate({
        where: { tenantId, createdAt: { gte, lte } },
        _sum: { amount: true },
      }),
      this.prisma.distributorCapital.aggregate({
        where: { tenantId, type: 'injection', createdAt: { gte, lte } },
        _sum: { amount: true },
      }),
    ]);

    // المصروفات خلال الفترة
    const [expensesAgg, withdrawalAgg] = await Promise.all([
      this.prisma.distributorExpense.aggregate({
        where: { tenantId, expenseDate: { gte, lte } },
        _sum: { amount: true },
      }),
      this.prisma.distributorCapital.aggregate({
        where: { tenantId, type: 'withdrawal', createdAt: { gte, lte } },
        _sum: { amount: true },
      }),
    ]);

    const cashSales = cashSalesAgg._sum.totalAmount ?? 0;
    const collections = collectionsAgg._sum.amount ?? 0;
    const injections = injectionAgg._sum.amount ?? 0;
    const expenses = expensesAgg._sum.amount ?? 0;
    const withdrawals = withdrawalAgg._sum.amount ?? 0;

    const totalInflows = cashSales + collections + injections;
    const totalOutflows = expenses + withdrawals;
    const netFlow = totalInflows - totalOutflows;
    const closingBalance = openingBalance + netFlow;

    // بيانات الرسم البياني اليومي (Sparkline)
    const chartData = await this._buildCashFlowChart(tenantId, gte, lte);

    return {
      period: { from: gte.toISOString(), to: lte.toISOString() },
      openingBalance,
      inflows: { cashSales, collections, injections, total: totalInflows },
      outflows: { expenses, withdrawals, total: totalOutflows },
      netFlow,
      closingBalance,
      chartData,
    };
  }

  // ─── 4. Receivables (الذمم) ───────────────────────────────────────────────

  async getReceivables(tenantId: string, query: PaginationDto) {
    const { page = 1, limit = 10, search } = query;
    const skip = (page - 1) * limit;

    const where: any = { tenantId, isActive: true };
    if (search) where.name = { contains: search, mode: 'insensitive' };

    const customers = await this.prisma.distributorCustomer.findMany({
      where,
      skip,
      take: limit,
    });
    const total = await this.prisma.distributorCustomer.count({ where });

    const enriched = await Promise.all(
      customers.map(async (c) => {
        const debt = await this.customersService.computeCustomerDebt(tenantId, c.id);
        if (debt === 0) return null;
        return { ...c, currentDebt: debt };
      }),
    );

    const filtered = enriched.filter(Boolean);
    const totalReceivables = filtered.reduce((s: number, c: any) => s + c.currentDebt, 0);

    return {
      data: filtered,
      totalReceivables,
      meta: { total, page, limit, totalPages: Math.ceil(total / limit) },
    };
  }

  // ─── 5. Expenses ──────────────────────────────────────────────────────────

  async createExpense(tenantId: string, dto: CreateExpenseDto) {
    const finalCategory = dto.category === 'other' && dto.customCategory
      ? dto.customCategory
      : dto.category;

    return this.prisma.distributorExpense.create({
      data: {
        tenantId,
        category: finalCategory,
        amount: dto.amount,
        description: dto.description,
        expenseDate: dto.expenseDate ? new Date(dto.expenseDate) : new Date(),
      },
    });
  }

  async getExpenses(tenantId: string, query: PaginationDto & { category?: string }, dateRange: DateRangeDto) {
    const { page = 1, limit = 10, category } = query;
    const { gte, lte } = dateRange.getDateRange();
    const skip = (page - 1) * limit;

    const where: any = { tenantId, expenseDate: { gte, lte } };
    if (category) where.category = category;

    const [data, total, breakdown] = await Promise.all([
      this.prisma.distributorExpense.findMany({
        where,
        skip,
        take: limit,
        orderBy: { expenseDate: 'desc' },
      }),
      this.prisma.distributorExpense.count({ where }),
      this.prisma.distributorExpense.groupBy({
        by: ['category'],
        where: { tenantId, expenseDate: { gte, lte } },
        _sum: { amount: true },
        orderBy: { _sum: { amount: 'desc' } },
      }),
    ]);

    return {
      data,
      breakdown,
      meta: { total, page, limit, totalPages: Math.ceil(total / limit) },
    };
  }

  // ─── 6. Capital ───────────────────────────────────────────────────────────

  async createCapital(tenantId: string, dto: CreateCapitalDto) {
    return this.prisma.distributorCapital.create({
      data: { tenantId, type: dto.type, amount: dto.amount, description: dto.description },
    });
  }

  async getCapitalHistory(tenantId: string, query: PaginationDto) {
    const { page = 1, limit = 10 } = query;
    const skip = (page - 1) * limit;

    const [data, total, summary] = await Promise.all([
      this.prisma.distributorCapital.findMany({
        where: { tenantId },
        skip,
        take: limit,
        orderBy: { createdAt: 'desc' },
      }),
      this.prisma.distributorCapital.count({ where: { tenantId } }),
      this.prisma.distributorCapital.groupBy({
        by: ['type'],
        where: { tenantId },
        _sum: { amount: true },
      }),
    ]);

    const injections = summary.find((s) => s.type === 'injection')?._sum?.amount ?? 0;
    const withdrawals = summary.find((s) => s.type === 'withdrawal')?._sum?.amount ?? 0;

    return {
      data,
      currentCapital: injections - withdrawals,
      meta: { total, page, limit, totalPages: Math.ceil(total / limit) },
    };
  }

  // ─── 7. Top Customers & Top Products ─────────────────────────────────────

  async getTopCustomers(tenantId: string, query: DateRangeDto, take = 5) {
    const { gte, lte } = query.getDateRange();
    const grouped = await this.prisma.distributorSale.groupBy({
      by: ['customerId'],
      where: { tenantId, customerId: { not: null }, createdAt: { gte, lte } },
      _sum: { totalAmount: true, profit: true, quantity: true },
      orderBy: { _sum: { totalAmount: 'desc' } },
      take,
    });

    return Promise.all(
      grouped.map(async (g) => {
        const customer = await this.prisma.distributorCustomer.findUnique({
          where: { id: g.customerId! },
          select: { id: true, name: true, phone: true },
        });
        return { ...g, customer };
      }),
    );
  }

  async getTopProducts(tenantId: string, query: DateRangeDto, take = 5) {
    const { gte, lte } = query.getDateRange();
    return this.prisma.distributorSale.groupBy({
      by: ['categoryValue', 'cardType'],
      where: { tenantId, createdAt: { gte, lte } },
      _sum: { profit: true, quantity: true, totalAmount: true },
      orderBy: { _sum: { profit: 'desc' } },
      take,
    });
  }

  // ─── Private Helpers ──────────────────────────────────────────────────────

  private async _aggregateSalesProfit(tenantId: string, range: DateRangeDto) {
    const { gte, lte } = range.getDateRange();
    const agg = await this.prisma.distributorSale.aggregate({
      where: { tenantId, createdAt: { gte, lte } },
      _sum: { profit: true, totalAmount: true },
    });
    const grossProfit = agg._sum.profit ?? 0;
    const totalSales = agg._sum.totalAmount ?? 0;
    const cogs = totalSales - grossProfit;
    const marginPercent = totalSales > 0 ? parseFloat(((grossProfit / totalSales) * 100).toFixed(2)) : 0;
    return { grossProfit, totalSales, cogs, marginPercent };
  }

  private async _aggregateExpenses(tenantId: string, range: DateRangeDto): Promise<number> {
    const { gte, lte } = range.getDateRange();
    const agg = await this.prisma.distributorExpense.aggregate({
      where: { tenantId, expenseDate: { gte, lte } },
      _sum: { amount: true },
    });
    return agg._sum.amount ?? 0;
  }

  private async _computeTotalReceivables(tenantId: string): Promise<number> {
    const creditSales = await this.prisma.distributorSale.aggregate({
      where: { tenantId, isCredit: true },
      _sum: { totalAmount: true },
    });
    const payments = await this.prisma.distributorPayment.aggregate({
      where: { tenantId },
      _sum: { amount: true },
    });
    return Math.max(0, (creditSales._sum.totalAmount ?? 0) - (payments._sum.amount ?? 0));
  }

  private async _computeCapital(tenantId: string): Promise<number> {
    const capital = await this.prisma.distributorCapital.groupBy({
      by: ['type'],
      where: { tenantId },
      _sum: { amount: true },
    });
    const injections = capital.find((c) => c.type === 'injection')?._sum?.amount ?? 0;
    const withdrawals = capital.find((c) => c.type === 'withdrawal')?._sum?.amount ?? 0;
    return injections - withdrawals;
  }

  private async _buildCashFlowChart(tenantId: string, gte: Date, lte: Date) {
    const days: { date: string; inflow: number; outflow: number; net: number }[] = [];
    const cursor = new Date(gte);
    cursor.setHours(0, 0, 0, 0);

    while (cursor <= lte) {
      const dayStart = new Date(cursor);
      const dayEnd = new Date(cursor);
      dayEnd.setHours(23, 59, 59, 999);

      const [sales, payments, expenses] = await Promise.all([
        this.prisma.distributorSale.aggregate({
          where: { tenantId, isCredit: false, createdAt: { gte: dayStart, lte: dayEnd } },
          _sum: { totalAmount: true },
        }),
        this.prisma.distributorPayment.aggregate({
          where: { tenantId, createdAt: { gte: dayStart, lte: dayEnd } },
          _sum: { amount: true },
        }),
        this.prisma.distributorExpense.aggregate({
          where: { tenantId, expenseDate: { gte: dayStart, lte: dayEnd } },
          _sum: { amount: true },
        }),
      ]);

      const inflow = (sales._sum.totalAmount ?? 0) + (payments._sum.amount ?? 0);
      const outflow = expenses._sum.amount ?? 0;
      days.push({ date: cursor.toISOString().split('T')[0], inflow, outflow, net: inflow - outflow });

      cursor.setDate(cursor.getDate() + 1);
      // تحديد للـ 60 يوم لتجنب استهلاك الموارد
      if (days.length >= 60) break;
    }

    return days;
  }
}
