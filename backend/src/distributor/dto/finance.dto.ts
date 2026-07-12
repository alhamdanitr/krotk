import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsNotEmpty, IsInt, IsString, IsOptional, IsIn, Min } from 'class-validator';
import { Type } from 'class-transformer';

export class RecordPaymentDto {
  @ApiProperty({ description: 'معرف العميل' })
  @IsNotEmpty()
  @IsString()
  customerId: string;

  @ApiProperty({ description: 'مبلغ الدفعة بالريال', example: 10000 })
  @IsNotEmpty()
  @Type(() => Number)
  @IsInt()
  @Min(1)
  amount: number;

  @ApiPropertyOptional({ description: 'طريقة الدفع', enum: ['cash', 'wallet', 'bank'], default: 'cash' })
  @IsOptional()
  @IsIn(['cash', 'wallet', 'bank'])
  paymentMethod?: string = 'cash';

  @ApiPropertyOptional({ description: 'ملاحظات' })
  @IsOptional()
  @IsString()
  notes?: string;
}

export class CreateCapitalDto {
  @ApiProperty({ description: 'نوع الحركة', enum: ['injection', 'withdrawal'] })
  @IsNotEmpty()
  @IsIn(['injection', 'withdrawal'])
  type: string;

  @ApiProperty({ description: 'المبلغ', example: 500000 })
  @IsNotEmpty()
  @Type(() => Number)
  @IsInt()
  @Min(1)
  amount: number;

  @ApiPropertyOptional({ description: 'وصف الحركة' })
  @IsOptional()
  @IsString()
  description?: string;
}

export class UpdatePricingDto {
  @ApiProperty({ example: 200 })
  @IsNotEmpty()
  @Type(() => Number)
  @IsInt()
  categoryValue: number;

  @ApiProperty({ enum: ['REGULAR', 'PRO'], example: 'REGULAR' })
  @IsNotEmpty()
  @IsIn(['REGULAR', 'PRO'])
  cardType: string;

  @ApiProperty({ description: 'سعر الشراء', example: 180 })
  @IsNotEmpty()
  @Type(() => Number)
  @IsInt()
  @Min(0)
  buyPrice: number;

  @ApiProperty({ description: 'سعر البيع', example: 210 })
  @IsNotEmpty()
  @Type(() => Number)
  @IsInt()
  @Min(0)
  sellPrice: number;
}
