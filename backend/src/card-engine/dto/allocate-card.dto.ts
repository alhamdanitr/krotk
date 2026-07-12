import { IsString, IsInt, IsPositive } from 'class-validator';

export class AllocateCardDto {
  @IsString() tenantId: string;
  @IsInt() @IsPositive() amount: number;
  @IsString() depositId: string;
}
