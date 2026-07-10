import { Controller, Get, Post, Body, Query, Param } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse, ApiBearerAuth, ApiQuery } from '@nestjs/swagger';
import { DistributorFinanceService } from './finance.service';
import { CurrentUser } from '../../common/decorators/current-user.decorator';
import { Roles } from '../../common/decorators/roles.decorator';
import { DateRangeDto } from '../../common/dto/date-range.dto';
import { PaginationDto } from '../../common/dto/pagination.dto';
import { CreateExpenseDto } from '../dto/create-expense.dto';
import { CreateCapitalDto } from '../dto/finance.dto';

@ApiTags('Distributor — Finance')
@ApiBearerAuth()
@Controller('distributor/finance')
export class DistributorFinanceController {
  constructor(private readonly financeService: DistributorFinanceService) {}

  @Get('dashboard')
  @ApiOperation({ summary: 'الملخص المالي الشامل (اليوم + الشهر + رأس المال + الذمم)' })
  @ApiResponse({ status: 200 })
  getDashboard(@CurrentUser() user: any) {
    return this.financeService.getFinanceDashboard(user.tenantId);
  }

  @Get('profits')
  @ApiOperation({ summary: 'لوحة الأرباح الاحترافية (اليوم/الأسبوع/الشهر/السنة/مخصص)' })
  @ApiQuery({ name: 'period', required: false, enum: ['today', 'week', 'month', 'year', 'custom'] })
  @ApiQuery({ name: 'startDate', required: false })
  @ApiQuery({ name: 'endDate', required: false })
  @ApiResponse({ status: 200, description: 'لوحة أرباح كاملة مع Top Products و Top Customers' })
  getProfits(@CurrentUser() user: any, @Query() query: DateRangeDto) {
    return this.financeService.getProfits(user.tenantId, query);
  }

  @Get('cash-flow')
  @ApiOperation({ summary: 'التدفق النقدي (رصيد افتتاحي، مقبوضات، مصروفات، رصيد ختامي)' })
  @ApiQuery({ name: 'period', required: false, enum: ['today', 'week', 'month', 'year', 'custom'] })
  @ApiResponse({ status: 200, description: 'تقرير التدفق النقدي مع بيانات الرسم البياني' })
  getCashFlow(@CurrentUser() user: any, @Query() query: DateRangeDto) {
    return this.financeService.getCashFlow(user.tenantId, query);
  }

  @Get('receivables')
  @ApiOperation({ summary: 'الذمم الإجمالية — قائمة العملاء المدينين' })
  @ApiResponse({ status: 200 })
  getReceivables(@CurrentUser() user: any, @Query() query: PaginationDto) {
    return this.financeService.getReceivables(user.tenantId, query);
  }

  @Post('expenses')
  @ApiOperation({ summary: 'تسجيل مصروف جديد' })
  @ApiResponse({ status: 201 })
  createExpense(@CurrentUser() user: any, @Body() dto: CreateExpenseDto) {
    return this.financeService.createExpense(user.tenantId, dto);
  }

  @Get('expenses')
  @ApiOperation({ summary: 'قائمة المصروفات مع Breakdown حسب الفئة' })
  @ApiQuery({ name: 'period', required: false, enum: ['today', 'week', 'month', 'year', 'custom'] })
  @ApiQuery({ name: 'category', required: false })
  @ApiResponse({ status: 200 })
  getExpenses(@CurrentUser() user: any, @Query() query: any) {
    return this.financeService.getExpenses(user.tenantId, query, query);
  }

  @Post('capital')
  @Roles('tenant_admin')
  @ApiOperation({ summary: 'تسجيل حركة رأس مال (إيداع أو سحب)' })
  @ApiResponse({ status: 201 })
  createCapital(@CurrentUser() user: any, @Body() dto: CreateCapitalDto) {
    return this.financeService.createCapital(user.tenantId, dto);
  }

  @Get('capital')
  @Roles('tenant_admin')
  @ApiOperation({ summary: 'سجل حركات رأس المال + الرصيد الحالي' })
  @ApiResponse({ status: 200 })
  getCapitalHistory(@CurrentUser() user: any, @Query() query: PaginationDto) {
    return this.financeService.getCapitalHistory(user.tenantId, query);
  }

  @Get('top-customers')
  @ApiOperation({ summary: 'أفضل العملاء حسب حجم المشتريات' })
  @ApiQuery({ name: 'period', required: false, enum: ['today', 'week', 'month', 'year', 'custom'] })
  @ApiResponse({ status: 200 })
  getTopCustomers(@CurrentUser() user: any, @Query() query: DateRangeDto) {
    return this.financeService.getTopCustomers(user.tenantId, query);
  }

  @Get('top-products')
  @ApiOperation({ summary: 'أفضل الأصناف ربحاً' })
  @ApiQuery({ name: 'period', required: false, enum: ['today', 'week', 'month', 'year', 'custom'] })
  @ApiResponse({ status: 200 })
  getTopProducts(@CurrentUser() user: any, @Query() query: DateRangeDto) {
    return this.financeService.getTopProducts(user.tenantId, query);
  }
}
