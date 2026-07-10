import {
  Injectable,
  BadRequestException,
  ForbiddenException,
  NotFoundException,
} from '@nestjs/common';
import { PrismaService } from '../../prisma/prisma.service';
import { PaginationDto } from '../../common/dto/pagination.dto';
import { CreateCustomerDto, UpdateCustomerDto } from '../dto/create-customer.dto';
import { RecordPaymentDto } from '../dto/finance.dto';

@Injectable()
export class DistributorCustomersService {
  constructor(private prisma: PrismaService) {}

  // ─── 1. Customer CRUD ────────────────────────────────────────────────────────

  async getCustomers(tenantId: string, query: PaginationDto) {
    const { page = 1, limit = 10, search, sortBy = 'createdAt', sortOrder = 'desc' } = query;
    const skip = (page - 1) * limit;

    const where: any = { tenantId };
    if (search) {
      where.OR = [
        { name: { contains: search, mode: 'insensitive' } },
        { phone: { contains: search } },
      ];
    }

    const [data, total] = await Promise.all([
      this.prisma.distributorCustomer.findMany({
        where,
        skip,
        take: limit,
        orderBy: { [sortBy]: sortOrder },
      }),
      this.prisma.distributorCustomer.count({ where }),
    ]);

    // Attach computed debt to each customer
    const enriched = await Promise.all(
      data.map(async (customer) => {
        const debt = await this.computeCustomerDebt(tenantId, customer.id);
        const lastPayment = await this.getLastPaymentDate(tenantId, customer.id);
        return {
          ...customer,
          currentDebt: debt,
          lastPaymentDate: lastPayment,
          creditUsagePercent: customer.creditLimit > 0
            ? Math.round((debt / customer.creditLimit) * 100)
            : null,
        };
      }),
    );

    return {
      data: enriched,
      meta: { total, page, limit, totalPages: Math.ceil(total / limit) },
    };
  }

  async createCustomer(tenantId: string, dto: CreateCustomerDto) {
    return this.prisma.distributorCustomer.create({
      data: { tenantId, ...dto },
    });
  }

  async getCustomerById(tenantId: string, id: string) {
    const customer = await this.prisma.distributorCustomer.findFirst({
      where: { id, tenantId },
    });
    if (!customer) throw new NotFoundException('Customer not found');

    const debt = await this.computeCustomerDebt(tenantId, id);
    const lastPayment = await this.getLastPaymentDate(tenantId, id);
    return {
      ...customer,
      currentDebt: debt,
      lastPaymentDate: lastPayment,
      creditUsagePercent: customer.creditLimit > 0
        ? Math.round((debt / customer.creditLimit) * 100)
        : null,
    };
  }

  async updateCustomer(tenantId: string, id: string, dto: UpdateCustomerDto) {
    const exists = await this.prisma.distributorCustomer.findFirst({ where: { id, tenantId } });
    if (!exists) throw new NotFoundException('Customer not found');
    return this.prisma.distributorCustomer.update({ where: { id }, data: dto });
  }

  async deleteCustomer(tenantId: string, id: string) {
    const exists = await this.prisma.distributorCustomer.findFirst({ where: { id, tenantId } });
    if (!exists) throw new NotFoundException('Customer not found');
    await this.prisma.distributorCustomer.delete({ where: { id } });
    return { message: 'Customer deleted successfully' };
  }

  // ─── 2. Customer Statement (كشف الحساب) ─────────────────────────────────────

  async getCustomerStatement(tenantId: string, customerId: string) {
    const customer = await this.getCustomerById(tenantId, customerId);

    const [sales, payments] = await Promise.all([
      this.prisma.distributorSale.findMany({
        where: { tenantId, customerId },
        orderBy: { createdAt: 'desc' },
      }),
      this.prisma.distributorPayment.findMany({
        where: { tenantId, customerId },
        orderBy: { createdAt: 'desc' },
      }),
    ]);

    const totalSales = sales.reduce((s, r) => s + r.totalAmount, 0);
    const totalPaid = payments.reduce((s, r) => s + r.amount, 0);
    const currentDebt = totalSales - totalPaid;

    // Build merged timeline
    const timeline = [
      ...sales.map((s) => ({ type: 'sale', date: s.createdAt, amount: s.totalAmount, detail: `${s.quantity} × ${s.cardType} ${s.categoryValue}` })),
      ...payments.map((p) => ({ type: 'payment', date: p.createdAt, amount: p.amount, detail: `دفعة — ${p.paymentMethod}` })),
    ].sort((a, b) => b.date.getTime() - a.date.getTime());

    return { customer, totalSales, totalPaid, currentDebt, timeline };
  }

  // ─── 3. Payments / Collections ───────────────────────────────────────────────

  async recordPayment(tenantId: string, dto: RecordPaymentDto) {
    const customer = await this.prisma.distributorCustomer.findFirst({
      where: { id: dto.customerId, tenantId },
    });
    if (!customer) throw new NotFoundException('Customer not found');

    return this.prisma.distributorPayment.create({
      data: {
        tenantId,
        customerId: dto.customerId,
        amount: dto.amount,
        paymentMethod: dto.paymentMethod ?? 'cash',
        notes: dto.notes,
      },
    });
  }

  // ─── 4. Alerts — Customers at risk ───────────────────────────────────────────

  async getCustomerAlerts(tenantId: string) {
    const customers = await this.prisma.distributorCustomer.findMany({
      where: { tenantId, isActive: true, creditLimit: { gt: 0 } },
    });

    const overLimit: any[] = [];
    const nearLimit: any[] = [];   // >= 80%
    const defaulted: any[] = [];   // no payment in 30 days with debt

    const thirtyDaysAgo = new Date();
    thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);

    await Promise.all(
      customers.map(async (c) => {
        const debt = await this.computeCustomerDebt(tenantId, c.id);
        if (debt <= 0) return;

        const lastPayment = await this.getLastPaymentDate(tenantId, c.id);
        const ratio = debt / c.creditLimit;

        if (ratio > 1) overLimit.push({ ...c, currentDebt: debt });
        else if (ratio >= 0.8) nearLimit.push({ ...c, currentDebt: debt, usagePercent: Math.round(ratio * 100) });

        if (!lastPayment || lastPayment < thirtyDaysAgo) {
          defaulted.push({ ...c, currentDebt: debt, lastPaymentDate: lastPayment });
        }
      }),
    );

    return { overLimit, nearLimit, defaulted };
  }

  // ─── 5. Shared helpers ────────────────────────────────────────────────────────

  /** حساب إجمالي ذمة عميل: مجموع المبيعات الآجلة — مجموع الدفعات */
  async computeCustomerDebt(tenantId: string, customerId: string): Promise<number> {
    const [salesAgg, paymentsAgg] = await Promise.all([
      this.prisma.distributorSale.aggregate({
        where: { tenantId, customerId, isCredit: true },
        _sum: { totalAmount: true },
      }),
      this.prisma.distributorPayment.aggregate({
        where: { tenantId, customerId },
        _sum: { amount: true },
      }),
    ]);

    const totalCredit = salesAgg._sum.totalAmount ?? 0;
    const totalPaid = paymentsAgg._sum.amount ?? 0;
    return Math.max(0, totalCredit - totalPaid);
  }

  private async getLastPaymentDate(tenantId: string, customerId: string): Promise<Date | null> {
    const last = await this.prisma.distributorPayment.findFirst({
      where: { tenantId, customerId },
      orderBy: { createdAt: 'desc' },
      select: { createdAt: true },
    });
    return last?.createdAt ?? null;
  }
}
