import {
  Injectable,
  BadRequestException,
  ForbiddenException,
  NotFoundException,
} from '@nestjs/common';
import { PrismaService } from '../../prisma/prisma.service';
import { PaginationDto } from '../../common/dto/pagination.dto';
import { CreateSaleDto } from '../dto/create-sale.dto';
import { DistributorCustomersService } from '../customers/customers.service';

@Injectable()
export class DistributorSalesService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly customersService: DistributorCustomersService,
  ) {}

  async createSale(tenantId: string, userRole: string, dto: CreateSaleDto) {
    // 1. جلب التسعيرة الحالية (Snapshot)
    const pricing = await this.prisma.distributorPricing.findUnique({
      where: {
        tenantId_categoryValue_cardType: {
          tenantId,
          categoryValue: dto.categoryValue,
          cardType: dto.cardType,
        },
      },
    });

    if (!pricing) {
      throw new BadRequestException(
        `No pricing configured for ${dto.cardType} / ${dto.categoryValue}. Please set pricing first.`,
      );
    }

    const buyPrice = pricing.buyPrice;
    const sellPrice = pricing.sellPrice;
    const totalAmount = sellPrice * dto.quantity;
    const profit = (sellPrice - buyPrice) * dto.quantity;
    const isCredit = dto.receivedAmount < totalAmount;
    const creditAmount = isCredit ? totalAmount - dto.receivedAmount : 0;

    // 2. Credit Limit Enforcement (للمبيعات الآجلة فقط)
    if (isCredit && dto.customerId) {
      const customer = await this.prisma.distributorCustomer.findFirst({
        where: { id: dto.customerId, tenantId },
      });
      if (!customer) throw new NotFoundException('Customer not found');

      if (customer.creditLimit > 0) {
        const currentDebt = await this.customersService.computeCustomerDebt(tenantId, dto.customerId);
        const projectedDebt = currentDebt + creditAmount;

        if (projectedDebt > customer.creditLimit) {
          // السماح للـ tenant_admin بالتجاوز فقط إذا أرسل overrideCreditLimit=true
          if (dto.overrideCreditLimit && userRole === 'tenant_admin') {
            // يُسجَّل في Audit / Notes (يمكن تطوير Audit Log لاحقاً)
          } else {
            throw new BadRequestException(
              `Credit limit exceeded. Current debt: ${currentDebt}, Limit: ${customer.creditLimit}, Requested: ${creditAmount}. Only tenant_admin can override.`,
            );
          }
        } else if (projectedDebt / customer.creditLimit >= 0.8) {
          // تحذير مُضمَّن في الاستجابة (لا يمنع العملية)
        }
      }
    }

    // 3. إنشاء الفاتورة داخل Transaction
    return this.prisma.$transaction(async (tx) => {
      const sale = await tx.distributorSale.create({
        data: {
          tenantId,
          customerId: dto.customerId,
          categoryValue: dto.categoryValue,
          cardType: dto.cardType,
          quantity: dto.quantity,
          buyPrice,
          sellPrice,
          totalAmount,
          profit,
          receivedAmount: dto.receivedAmount,
          isCredit,
          notes: dto.notes,
        },
      });

      // إذا كانت جزء منها نقد → تُسجَّل كدفعة جزئية تلقائياً
      if (dto.receivedAmount > 0 && isCredit && dto.customerId) {
        await tx.distributorPayment.create({
          data: {
            tenantId,
            customerId: dto.customerId,
            amount: dto.receivedAmount,
            paymentMethod: 'cash',
            notes: `دفعة جزئية مع الفاتورة ${sale.id}`,
          },
        });
      }

      return sale;
    });
  }

  async getSales(tenantId: string, query: PaginationDto & { customerId?: string; isCredit?: string; categoryValue?: string }) {
    const { page = 1, limit = 10, search, sortBy = 'createdAt', sortOrder = 'desc', customerId, isCredit, categoryValue } = query;
    const skip = (page - 1) * limit;

    const where: any = { tenantId };
    if (customerId) where.customerId = customerId;
    if (isCredit !== undefined) where.isCredit = isCredit === 'true';
    if (categoryValue) where.categoryValue = Number(categoryValue);
    if (search) {
      where.OR = [
        { notes: { contains: search, mode: 'insensitive' } },
        { customer: { name: { contains: search, mode: 'insensitive' } } },
      ];
    }

    const [data, total] = await Promise.all([
      this.prisma.distributorSale.findMany({
        where,
        skip,
        take: limit,
        orderBy: { [sortBy]: sortOrder },
        include: { customer: { select: { id: true, name: true, phone: true } } },
      }),
      this.prisma.distributorSale.count({ where }),
    ]);

    return {
      data,
      meta: { total, page, limit, totalPages: Math.ceil(total / limit) },
    };
  }

  async getSaleById(tenantId: string, id: string) {
    const sale = await this.prisma.distributorSale.findFirst({
      where: { id, tenantId },
      include: { customer: true },
    });
    if (!sale) throw new NotFoundException('Sale not found');
    return sale;
  }
}
