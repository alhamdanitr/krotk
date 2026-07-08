import { Controller, Get, UseGuards, Post, Body, Put, Param, Delete } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiBearerAuth } from '@nestjs/swagger';
import { NotificationsService } from './notifications.service';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';

@ApiTags('Notifications (SMS Templates)')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('notifications/templates')
export class NotificationsController {
  constructor(private readonly notificationsService: NotificationsService) {}

  @Get()
  @ApiOperation({ summary: 'Get all SMS templates for the tenant' })
  getTemplates(@CurrentUser() user: any) {
    return this.notificationsService.getTemplates(user.tenantId);
  }
}
