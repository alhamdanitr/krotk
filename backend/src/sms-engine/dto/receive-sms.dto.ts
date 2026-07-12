import { IsString, IsNotEmpty, IsDateString, IsOptional } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class ReceiveSmsDto {
  @ApiProperty({ example: 'Jaib', description: 'Sender number or name' })
  @IsString() @IsNotEmpty()
  sender: string;

  @ApiProperty({ example: 'تم استلام مبلغ 500 ريال من X1' })
  @IsString() @IsNotEmpty()
  body: string;

  @ApiProperty({ example: '2026-06-30T00:00:00Z' })
  @IsDateString()
  receivedAt: string;
}
