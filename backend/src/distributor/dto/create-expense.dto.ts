import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsNotEmpty, IsInt, IsString, IsOptional, IsIn, Min } from 'class-validator';
import { Type } from 'class-transformer';

export class CreateExpenseDto {
  @ApiProperty({
    description: 'تصنيف المصروف',
    enum: ['rent', 'salary', 'electricity', 'internet', 'transport', 'maintenance', 'other'],
  })
  @IsNotEmpty()
  @IsString()
  category: string;

  @ApiPropertyOptional({ description: 'اسم التصنيف المخصص (عند اختيار other)' })
  @IsOptional()
  @IsString()
  customCategory?: string;

  @ApiProperty({ description: 'المبلغ بالريال اليمني', example: 50000 })
  @IsNotEmpty()
  @Type(() => Number)
  @IsInt()
  @Min(1)
  amount: number;

  @ApiPropertyOptional({ description: 'وصف المصروف' })
  @IsOptional()
  @IsString()
  description?: string;

  @ApiPropertyOptional({ description: 'تاريخ المصروف (ISO)', example: '2026-07-01' })
  @IsOptional()
  expenseDate?: string;
}
