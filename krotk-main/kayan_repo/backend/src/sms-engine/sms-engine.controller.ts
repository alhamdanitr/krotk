import { Body, Controller, Post, HttpCode, HttpStatus } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiBearerAuth } from '@nestjs/swagger';
import { ReceiveSmsDto } from './dto/receive-sms.dto';
import { SmsEngineService } from './sms-engine.service';
import { CurrentUser } from '../common/decorators/current-user.decorator';

@ApiTags('SMS Engine')
@ApiBearerAuth()
@Controller('sms')
export class SmsEngineController {
  constructor(private readonly smsEngineService: SmsEngineService) {}

  @Post('receive')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Receive raw SMS from Android Agent and process it' })
  async receive(@CurrentUser() user: any, @Body() dto: ReceiveSmsDto) {
    const rawSms = {
      sender: dto.sender,
      body: dto.body,
      receivedAt: new Date(dto.receivedAt),
      tenantId: user.tenantId,
    };
    
    return await this.smsEngineService.process(rawSms, user.tenantId);
  }
}
