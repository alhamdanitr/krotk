import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsNotEmpty, IsInt, IsString, IsOptional, IsBoolean, IsIn, Min } from 'class-validator';
import { Type } from 'class-transformer';

export class CreateSaleDto {
  @ApiPropertyOptional({ description: 'معرف العميل (اختياري للبيع النقدي)' })
  @IsOptional()
  @IsString()
  customerId?: string;

  @ApiProperty({ description: 'فئة الكرت', example: 200 })
  @IsNotEmpty()
  @Type(() => Number)
  @IsInt()
  categoryValue: number;

  @ApiProperty({ description: 'نوع الكرت', enum: ['REGULAR', 'PRO'], example: 'REGULAR' })
  @IsNotEmpty()
  @IsIn(['REGULAR', 'PRO'])
  cardType: string;

  @ApiProperty({ description: 'الكمية', example: 5 })
  @IsNotEmpty()
  @Type(() => Number)
  @IsInt()
  @Min(1)
  quantity: number;

  @ApiProperty({ description: 'المبلغ المقبوض (0 = آجل كامل)', example: 1000 })
  @IsNotEmpty()
  @Type(() => Number)
  @IsInt()
  @Min(0)
  receivedAmount: number;

  @ApiPropertyOptional({ description: 'تجاوز حد الائتمان (tenant_admin فقط)', default: false })
  @IsOptional()
  @IsBoolean()
  overrideCreditLimit?: boolean = false;

  @ApiPropertyOptional({ description: 'سبب تجاوز حد الائتمان' })
  @IsOptional()
  @IsString()
  overrideReason?: string;

  @ApiPropertyOptional({ description: 'ملاحظات' })
  @IsOptional()
  @IsString()
  notes?: string;
}
