import { Injectable } from '@nestjs/common';
import { PrismaService } from '../../prisma/prisma.service';
import { UpdatePricingDto } from '../dto/finance.dto';

// الأسعار الافتراضية للكروت
const DEFAULT_PRICING = [
  { categoryValue: 100, cardType: 'REGULAR', buyPrice: 90,  sellPrice: 100 },
  { categoryValue: 100, cardType: 'PRO',     buyPrice: 90,  sellPrice: 105 },
  { categoryValue: 200, cardType: 'REGULAR', buyPrice: 180, sellPrice: 210 },
  { categoryValue: 200, cardType: 'PRO',     buyPrice: 180, sellPrice: 220 },
  { categoryValue: 250, cardType: 'REGULAR', buyPrice: 225, sellPrice: 260 },
  { categoryValue: 250, cardType: 'PRO',     buyPrice: 225, sellPrice: 270 },
  { categoryValue: 300, cardType: 'REGULAR', buyPrice: 270, sellPrice: 315 },
  { categoryValue: 300, cardType: 'PRO',     buyPrice: 270, sellPrice: 330 },
  { categoryValue: 500, cardType: 'REGULAR', buyPrice: 450, sellPrice: 520 },
  { categoryValue: 500, cardType: 'PRO',     buyPrice: 450, sellPrice: 540 },
];

@Injectable()
export class DistributorPricingService {
  constructor(private readonly prisma: PrismaService) {}

  async getPricing(tenantId: string) {
    const existing = await this.prisma.distributorPricing.findMany({
      where: { tenantId, isActive: true },
      orderBy: [{ categoryValue: 'asc' }, { cardType: 'asc' }],
    });

    // إذا لم تُعرَّف تسعيرة بعد → أعِد الافتراضية بدون حفظها
    if (existing.length === 0) {
      return DEFAULT_PRICING.map((p) => ({ ...p, tenantId, id: null, isActive: true }));
    }
    return existing;
  }

  async updatePricing(tenantId: string, dto: UpdatePricingDto) {
    return this.prisma.distributorPricing.upsert({
      where: {
        tenantId_categoryValue_cardType: {
          tenantId,
          categoryValue: dto.categoryValue,
          cardType: dto.cardType,
        },
      },
      update: { buyPrice: dto.buyPrice, sellPrice: dto.sellPrice },
      create: {
        tenantId,
        categoryValue: dto.categoryValue,
        cardType: dto.cardType,
        buyPrice: dto.buyPrice,
        sellPrice: dto.sellPrice,
      },
    });
  }

  async resetToDefaults(tenantId: string) {
    // حذف التسعيرة الحالية وإنشاء الافتراضية
    await this.prisma.distributorPricing.deleteMany({ where: { tenantId } });
    await this.prisma.distributorPricing.createMany({
      data: DEFAULT_PRICING.map((p) => ({ ...p, tenantId })),
    });
    return { message: 'Pricing reset to defaults successfully' };
  }
}
