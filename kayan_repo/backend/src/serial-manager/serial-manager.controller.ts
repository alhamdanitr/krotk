import { Controller, Post, Get, Delete, Body, Param, Headers, Ip, HttpCode, HttpStatus } from '@nestjs/common';
import { SerialManagerService } from './serial-manager.service';
import { Public } from '../common/decorators/public.decorator';

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
  // 2. Admin Management Endpoints
  // -----------------------------------------------------------------
  @Public() // Keeping public so admin view can fetch from mobile easily without complex auth barriers
  @Get('api/v1/admin/serial/stats')
  async getStats() {
    return this.serialManagerService.getStats();
  }

  @Public()
  @Get('api/v1/admin/serial/list')
  async getAllSerials() {
    return this.serialManagerService.getAllSerials();
  }

  @Public()
  @Post('api/v1/admin/serial/create')
  async createSerial(@Body() body: any) {
    return this.serialManagerService.createSerial(body);
  }

  @Public()
  @Post('api/v1/admin/serial/reset/:id')
  async resetDeviceLock(@Param('id') id: string) {
    return this.serialManagerService.resetDeviceLock(parseInt(id, 10));
  }

  @Public()
  @Post('api/v1/admin/serial/toggle/:id')
  async toggleSerialStatus(@Param('id') id: string) {
    return this.serialManagerService.toggleSerialStatus(parseInt(id, 10));
  }

  @Public()
  @Delete('api/v1/admin/serial/delete/:id')
  async deleteSerial(@Param('id') id: string) {
    return this.serialManagerService.deleteSerial(parseInt(id, 10));
  }

  @Public()
  @Get('api/v1/admin/serial/logs')
  async getLogs() {
    return this.serialManagerService.getLogs();
  }
}
