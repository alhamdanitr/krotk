import { Controller, Get, Post, Delete, Body, Param, UseGuards } from '@nestjs/common';
import { BackupsService } from './backups.service';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { RolesGuard } from '../common/guards/roles.guard';
import { Roles } from '../common/decorators/roles.decorator';

@Controller('backups')
@UseGuards(JwtAuthGuard, RolesGuard)
@Roles('ADMIN')
export class BackupsController {
  constructor(private readonly backupsService: BackupsService) {}

  @Get()
  async listBackups() {
    const data = await this.backupsService.listBackups();
    return { success: true, data };
  }

  @Post()
  async createBackup() {
    const data = await this.backupsService.createBackup();
    return { success: true, ...data };
  }

  @Post('restore/:filename')
  async restoreBackup(@Param('filename') filename: string) {
    const data = await this.backupsService.restoreBackup(filename);
    return { success: true, ...data };
  }

  @Delete(':filename')
  async deleteBackup(@Param('filename') filename: string) {
    const data = await this.backupsService.deleteBackup(filename);
    return { success: true, ...data };
  }

  @Get('settings')
  async exportSettings() {
    const data = await this.backupsService.exportSettings();
    return { success: true, data };
  }

  @Post('settings')
  async importSettings(@Body() settings: any) {
    const data = await this.backupsService.importSettings(settings);
    return { success: true, ...data };
  }
}
