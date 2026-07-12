import { Controller, Get, Post, Body, Param, Query } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse, ApiBearerAuth, ApiParam, ApiQuery } from '@nestjs/swagger';
import { DistributorSalesService } from './sales.service';
import { CurrentUser } from '../../common/decorators/current-user.decorator';
import { PaginationDto } from '../../common/dto/pagination.dto';
import { CreateSaleDto } from '../dto/create-sale.dto';

@ApiTags('Distributor — Sales')
@ApiBearerAuth()
@Controller('distributor/sales')
export class DistributorSalesController {
  constructor(private readonly salesService: DistributorSalesService) {}

  @Get()
  @ApiOperation({ summary: 'قائمة الفواتير (Pagination + Filter)' })
  @ApiQuery({ name: 'customerId', required: false })
  @ApiQuery({ name: 'isCredit', required: false, enum: ['true', 'false'] })
  @ApiQuery({ name: 'categoryValue', required: false })
  @ApiResponse({ status: 200, description: 'قائمة مبيعات مُصفَّاة' })
  getSales(@CurrentUser() user: any, @Query() query: any) {
    return this.salesService.getSales(user.tenantId, query);
  }

  @Post()
  @ApiOperation({ summary: 'تسجيل فاتورة بيع جديدة' })
  @ApiResponse({ status: 201, description: 'تم تسجيل الفاتورة' })
  @ApiResponse({ status: 400, description: 'تجاوز حد الائتمان أو لا يوجد تسعيرة' })
  createSale(@CurrentUser() user: any, @Body() dto: CreateSaleDto) {
    return this.salesService.createSale(user.tenantId, user.role, dto);
  }

  @Get(':id')
  @ApiParam({ name: 'id', description: 'معرف الفاتورة' })
  @ApiOperation({ summary: 'تفاصيل فاتورة' })
  getSaleById(@CurrentUser() user: any, @Param('id') id: string) {
    return this.salesService.getSaleById(user.tenantId, id);
  }
}
