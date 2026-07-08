import { Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class ReportsService {
  constructor(private prisma: PrismaService) {}

  private getDateRange(startDate?: string, endDate?: string) {
    const range: any = {};
    if (startDate) range.gte = new Date(startDate);
    if (endDate) range.lte = new Date(endDate);
    return Object.keys(range).length > 0 ? range : undefined;
  }

  async getProfits(tenantId: string, startDate?: string, endDate?: string) {
    const createdAt = this.getDateRange(startDate, endDate);
    
    // Profit calculation: 
    // In a real scenario, you'd need the "cost" of each card category.
    // For now, let's assume a generic 2% profit margin for demonstration.
    const sales = await this.prisma.transaction.aggregate({
      where: { tenantId, ...(createdAt ? { createdAt } : {}) },
      _sum: { amount: true },
    });

    const totalSales = sales._sum.amount || 0;
    const estimatedProfit = totalSales * 0.02; // 2% 

    return {
      totalSales,
      estimatedProfit,
      margin: '2%',
    };
  }

  async getSummary(tenantId: string, startDate?: string, endDate?: string) {
    const createdAt = this.getDateRange(startDate, endDate);

    const deposits = await this.prisma.deposit.aggregate({
      where: { tenantId, ...(createdAt ? { createdAt } : {}) },
      _sum: { amount: true },
      _count: { id: true },
    });

    const sales = await this.prisma.transaction.aggregate({
      where: { tenantId, ...(createdAt ? { createdAt } : {}) },
      _sum: { amount: true },
      _count: { id: true },
    });

    return {
      deposits: {
        totalAmount: deposits._sum.amount || 0,
        count: deposits._count.id,
      },
      sales: {
        totalAmount: sales._sum.amount || 0,
        count: sales._count.id,
      },
    };
  }
}
