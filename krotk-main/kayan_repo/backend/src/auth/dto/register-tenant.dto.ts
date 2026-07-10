import { IsString, IsNotEmpty, MinLength, IsOptional } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class RegisterTenantDto {
  @ApiProperty({ example: 'شبكة المستقبل' })
  @IsString()
  @IsNotEmpty()
  networkName: string;

  @ApiProperty({ example: 'https://example.com/logo.png', required: false })
  @IsOptional()
  @IsString()
  logo?: string;

  @ApiProperty({ example: 'Yemen' })
  @IsString()
  @IsNotEmpty()
  country: string;

  @ApiProperty({ example: 'YER' })
  @IsString()
  @IsNotEmpty()
  currency: string;

  @ApiProperty({ example: 'admin' })
  @IsString()
  @IsNotEmpty()
  username: string;

  @ApiProperty({ example: 'password123' })
  @IsString()
  @MinLength(6)
  password: string;
}
