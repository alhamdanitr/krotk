import { Controller, Get, Query } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse, ApiBearerAuth, ApiQuery } from '@nestjs/swagger';
import { DashboardService } from './dashboard.service';
import { CurrentUser } from '../common/decorators/current-user.decorator';

@ApiTags('Dashboard')
@ApiBearerAuth()
@Controller('dashboard')
export class DashboardController {
  constructor(private readonly dashboardService: DashboardService) {}

  @Get('overview')
  @ApiOperation({ summary: 'لوحة التحكم الشاملة: بطاقات إحصائية + مخزون + اشتراك' })
  @ApiResponse({ status: 200, description: 'نظرة عامة متكاملة على المنصة' })
  getOverview(@CurrentUser() user: any) {
    return this.dashboardService.getOverview(user.tenantId);
  }

  @Get('alerts')
  @ApiOperation({ summary: 'جميع التنبيهات المُجمَّعة: ذمم، مخزون، اشتراك، طلبات معلقة' })
  @ApiResponse({ status: 200, description: 'قائمة تنبيهات مصنفة بالخطورة' })
  getAlerts(@CurrentUser() user: any) {
    return this.dashboardService.getAlerts(user.tenantId);
  }

  @Get('charts')
  @ApiOperation({ summary: 'بيانات الرسوم البيانية (مبيعات SMS + الموزع + الأرباح)' })
  @ApiQuery({ name: 'days', required: false, description: 'عدد الأيام (افتراضي: 30)', example: 30 })
  @ApiResponse({ status: 200 })
  getCharts(@CurrentUser() user: any, @Query('days') days?: string) {
    return this.dashboardService.getChartData(user.tenantId, Number(days) || 30);
  }

  @Get('recent-activity')
  @ApiOperation({ summary: 'آخر العمليات (SMS + مبيعات الموزع مدمجة)' })
  @ApiQuery({ name: 'limit', required: false, example: 10 })
  @ApiResponse({ status: 200 })
  getRecentActivity(@CurrentUser() user: any, @Query('limit') limit?: string) {
    return this.dashboardService.getRecentActivity(user.tenantId, Number(limit) || 10);
  }
}
