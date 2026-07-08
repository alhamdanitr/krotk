import { ApiPropertyOptional } from '@nestjs/swagger';
import { IsOptional, IsDateString, IsIn } from 'class-validator';

export type PeriodType = 'today' | 'week' | 'month' | 'year' | 'custom';

export class DateRangeDto {
  @ApiPropertyOptional({ enum: ['today', 'week', 'month', 'year', 'custom'], description: 'فترة التقرير' })
  @IsOptional()
  @IsIn(['today', 'week', 'month', 'year', 'custom'])
  period?: PeriodType = 'month';

  @ApiPropertyOptional({ description: 'تاريخ البداية (ISO) — مطلوب عند period=custom' })
  @IsOptional()
  @IsDateString()
  startDate?: string;

  @ApiPropertyOptional({ description: 'تاريخ النهاية (ISO) — مطلوب عند period=custom' })
  @IsOptional()
  @IsDateString()
  endDate?: string;

  /** يُعيد كائن { gte, lte } بناءً على الفترة المختارة */
  getDateRange(): { gte: Date; lte: Date } {
    const now = new Date();
    const lte = new Date(now);
    lte.setHours(23, 59, 59, 999);

    switch (this.period) {
      case 'today': {
        const gte = new Date(now);
        gte.setHours(0, 0, 0, 0);
        return { gte, lte };
      }
      case 'week': {
        const gte = new Date(now);
        gte.setDate(gte.getDate() - 6);
        gte.setHours(0, 0, 0, 0);
        return { gte, lte };
      }
      case 'month': {
        const gte = new Date(now.getFullYear(), now.getMonth(), 1);
        return { gte, lte };
      }
      case 'year': {
        const gte = new Date(now.getFullYear(), 0, 1);
        return { gte, lte };
      }
      case 'custom':
      default: {
        const gte = this.startDate ? new Date(this.startDate) : new Date(now.getFullYear(), now.getMonth(), 1);
        const customLte = this.endDate ? new Date(this.endDate) : lte;
        customLte.setHours(23, 59, 59, 999);
        return { gte, lte: customLte };
      }
    }
  }
}
