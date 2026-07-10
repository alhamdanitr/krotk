import { Injectable } from '@nestjs/common';
import { INotificationProvider, SmsNotificationPayload } from '../common/interfaces/notification-provider.interface';
import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class NotificationsService implements INotificationProvider {
  constructor(private readonly prisma: PrismaService) {}

  async sendSms(payload: SmsNotificationPayload): Promise<void> {
    // Phase 4: Call Android SMS API or SMS Gateway
    // For now, this is a stub as per design.
  }

  async getTemplates(tenantId: string) {
    return this.prisma.smsTemplate.findMany({
      where: { tenantId }
    });
  }
}
