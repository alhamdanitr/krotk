import { Controller, Get, Put, Post, Body } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse, ApiBearerAuth } from '@nestjs/swagger';
import { DistributorPricingService } from './pricing.service';
import { CurrentUser } from '../../common/decorators/current-user.decorator';
import { Roles } from '../../common/decorators/roles.decorator';
import { UpdatePricingDto } from '../dto/finance.dto';

@ApiTags('Distributor — Pricing')
@ApiBearerAuth()
@Controller('distributor/pricing')
export class DistributorPricingController {
  constructor(private readonly pricingService: DistributorPricingService) {}

  @Get()
  @ApiOperation({ summary: 'جلب التسعيرة الحالية (أو الافتراضية إن لم تُضبط)' })
  @ApiResponse({ status: 200 })
  getPricing(@CurrentUser() user: any) {
    return this.pricingService.getPricing(user.tenantId);
  }

  @Put()
  @Roles('tenant_admin')
  @ApiOperation({ summary: 'تحديث سعر فئة ونوع محدد' })
  @ApiResponse({ status: 200 })
  updatePricing(@CurrentUser() user: any, @Body() dto: UpdatePricingDto) {
    return this.pricingService.updatePricing(user.tenantId, dto);
  }

  @Post('reset')
  @Roles('tenant_admin')
  @ApiOperation({ summary: 'إعادة التسعيرة للأسعار الافتراضية' })
  @ApiResponse({ status: 200, description: 'تم إعادة الضبط' })
  resetPricing(@CurrentUser() user: any) {
    return this.pricingService.resetToDefaults(user.tenantId);
  }
}
