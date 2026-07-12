import { IsString, IsInt, IsPositive, IsOptional, IsBoolean } from 'class-validator';

export class CreateDepositDto {
  @IsString() tenantId: string;
  @IsString() idempotencyKey: string;
  @IsString() senderPhone: string;
  @IsOptional() @IsString() senderCode?: string;
  @IsInt() @IsPositive() amount: number;
  @IsString() walletType: string;
  @IsOptional() @IsString() rawSmsText?: string;
  @IsOptional() @IsBoolean() isShared?: boolean;
}
