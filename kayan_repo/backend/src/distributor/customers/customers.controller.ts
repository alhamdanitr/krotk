import {
  Controller,
  Get,
  Post,
  Put,
  Delete,
  Body,
  Param,
  Query,
} from '@nestjs/common';
import {
  ApiTags,
  ApiOperation,
  ApiResponse,
  ApiBearerAuth,
  ApiParam,
} from '@nestjs/swagger';
import { DistributorCustomersService } from './customers.service';
import { CurrentUser } from '../../common/decorators/current-user.decorator';
import { Roles } from '../../common/decorators/roles.decorator';
import { PaginationDto } from '../../common/dto/pagination.dto';
import { CreateCustomerDto, UpdateCustomerDto } from '../dto/create-customer.dto';
import { RecordPaymentDto } from '../dto/finance.dto';

@ApiTags('Distributor — Customers')
@ApiBearerAuth()
@Controller('distributor/customers')
export class DistributorCustomersController {
  constructor(private readonly customersService: DistributorCustomersService) {}

  @Get()
  @ApiOperation({ summary: 'قائمة العملاء (بحث + Pagination)' })
  @ApiResponse({ status: 200, description: 'قائمة العملاء مع الذمة الحالية' })
  getCustomers(@CurrentUser() user: any, @Query() query: PaginationDto) {
    return this.customersService.getCustomers(user.tenantId, query);
  }

  @Post()
  @Roles('tenant_admin')
  @ApiOperation({ summary: 'إضافة عميل جديد' })
  @ApiResponse({ status: 201, description: 'تم إنشاء العميل' })
  createCustomer(@CurrentUser() user: any, @Body() dto: CreateCustomerDto) {
    return this.customersService.createCustomer(user.tenantId, dto);
  }

  @Get('alerts')
  @ApiOperation({ summary: 'تنبيهات العملاء: تجاوزو الحد، اقتربوا من الحد، المتعثرون' })
  @ApiResponse({ status: 200, description: 'تنبيهات مصنفة' })
  getAlerts(@CurrentUser() user: any) {
    return this.customersService.getCustomerAlerts(user.tenantId);
  }

  @Get(':id')
  @ApiParam({ name: 'id', description: 'معرف العميل' })
  @ApiOperation({ summary: 'تفاصيل عميل' })
  getCustomerById(@CurrentUser() user: any, @Param('id') id: string) {
    return this.customersService.getCustomerById(user.tenantId, id);
  }

  @Put(':id')
  @Roles('tenant_admin')
  @ApiParam({ name: 'id', description: 'معرف العميل' })
  @ApiOperation({ summary: 'تعديل بيانات عميل' })
  updateCustomer(
    @CurrentUser() user: any,
    @Param('id') id: string,
    @Body() dto: UpdateCustomerDto,
  ) {
    return this.customersService.updateCustomer(user.tenantId, id, dto);
  }

  @Delete(':id')
  @Roles('tenant_admin')
  @ApiParam({ name: 'id', description: 'معرف العميل' })
  @ApiOperation({ summary: 'حذف عميل' })
  deleteCustomer(@CurrentUser() user: any, @Param('id') id: string) {
    return this.customersService.deleteCustomer(user.tenantId, id);
  }

  @Get(':id/statement')
  @ApiParam({ name: 'id', description: 'معرف العميل' })
  @ApiOperation({ summary: 'كشف حساب العميل التفصيلي' })
  getStatement(@CurrentUser() user: any, @Param('id') id: string) {
    return this.customersService.getCustomerStatement(user.tenantId, id);
  }

  @Post(':id/payments')
  @ApiParam({ name: 'id', description: 'معرف العميل' })
  @ApiOperation({ summary: 'تسجيل دفعة من العميل' })
  @ApiResponse({ status: 201, description: 'تم تسجيل الدفعة' })
  recordPayment(
    @CurrentUser() user: any,
    @Param('id') id: string,
    @Body() dto: RecordPaymentDto,
  ) {
    return this.customersService.recordPayment(user.tenantId, {
      ...dto,
      customerId: id,
    });
  }
}
