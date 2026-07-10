import { IsString, IsNotEmpty } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class ActivateSerialDto {
  @ApiProperty({ example: 'XXXX-YYYY-ZZZZ-WWWW' })
  @IsString()
  @IsNotEmpty()
  serialKey: string;

  @ApiProperty({ example: '123456' })
  @IsString()
  @IsNotEmpty()
  activationCode: string;
}
