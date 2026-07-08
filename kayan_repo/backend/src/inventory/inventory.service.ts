import { Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class InventoryService {
  constructor(private prisma: PrismaService) {}

  async getStock(tenantId: string) {
    const categories = await this.prisma.category.findMany({
      where: { tenantId },
      include: {
        _count: {
          select: { cards: { where: { isUsed: false } } }
        }
      },
      orderBy: { value: 'asc' }
    });

    return categories.map(cat => ({
      categoryId: cat.id,
      value: cat.value,
      availableCards: cat._count.cards,
    }));
  }
}
