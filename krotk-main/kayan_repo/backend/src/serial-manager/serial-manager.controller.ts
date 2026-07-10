import { Controller, Post, Get, Delete, Body, Param, Headers, Ip, HttpCode, HttpStatus, UseGuards } from '@nestjs/common';
import { SerialManagerService } from './serial-manager.service';
import { Public } from '../common/decorators/public.decorator';
import { Roles } from '../common/decorators/roles.decorator';

@Controller()
export class SerialManagerController {
  constructor(private readonly serialManagerService: SerialManagerService) {}

  // -----------------------------------------------------------------
  // 1. Mobile App Activation Endpoint (Public but HMAC Signed)
  // -----------------------------------------------------------------
  @Public()
  @Post('api/v1/serial/validate')
  @HttpCode(HttpStatus.OK)
  async validateSerial(
    @Ip() ip: string,
    @Headers('X-Signature') signature: string,
    @Headers('X-Timestamp') timestampStr: string,
    @Body() body: any,
  ) {
    const timestamp = parseInt(timestampStr || body.timestamp || '0', 10);
    const sig = signature || body.signature || '';
    return this.serialManagerService.validateSerial(ip, sig, timestamp, body);
  }

  // -----------------------------------------------------------------
  // 2. Admin Management Endpoints — تتطلب JWT صالح ودور super_admin.
  // الحارس العام (JwtAuthGuard + RolesGuard) مُفعّل تلقائيًا على كل المسارات
  // في app.module.ts؛ عدم وضع @Public() هنا يكفي لتفعيل حماية JWT، ونضيف
  // @Roles('super_admin') صراحة لمنع أي مستخدم مُصادَق لكن بدور أقل من الوصول.
  // -----------------------------------------------------------------
  @Roles('super_admin')
  @Get('api/v1/admin/serial/stats')
  async getStats() {
    return this.serialManagerService.getStats();
  }

  @Roles('super_admin')
  @Get('api/v1/admin/serial/list')
  async getAllSerials() {
    return this.serialManagerService.getAllSerials();
  }

  @Roles('super_admin')
  @Post('api/v1/admin/serial/create')
  async createSerial(@Body() body: any) {
    return this.serialManagerService.createSerial(body);
  }

  @Roles('super_admin')
  @Post('api/v1/admin/serial/reset/:id')
  async resetDeviceLock(@Param('id') id: string) {
    return this.serialManagerService.resetDeviceLock(parseInt(id, 10));
  }

  @Roles('super_admin')
  @Post('api/v1/admin/serial/toggle/:id')
  async toggleSerialStatus(@Param('id') id: string) {
    return this.serialManagerService.toggleSerialStatus(parseInt(id, 10));
  }

  @Roles('super_admin')
  @Delete('api/v1/admin/serial/delete/:id')
  async deleteSerial(@Param('id') id: string) {
    return this.serialManagerService.deleteSerial(parseInt(id, 10));
  }

  @Roles('super_admin')
  @Get('api/v1/admin/serial/logs')
  async getLogs() {
    return this.serialManagerService.getLogs();
  }
}
