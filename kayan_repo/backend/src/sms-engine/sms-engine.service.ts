import { Injectable, Logger } from '@nestjs/common';
import { IDepositProcessor, DepositProcessingResult } from '../common/interfaces/deposit-processor.interface';
import { RawSms } from '../common/interfaces/sms-parser.interface';
import { ParserRegistryService } from '../parser-registry/parser-registry.service';
import { CardEngineService } from '../card-engine/card-engine.service';
import { NotificationsService } from '../notifications/notifications.service';
import { PrismaService } from '../prisma/prisma.service';
import * as crypto from 'crypto';

@Injectable()
export class SmsEngineService implements IDepositProcessor {
  private readonly logger = new Logger(SmsEngineService.name);

  constructor(
    private readonly parserRegistry: ParserRegistryService,
    private readonly cardEngine: CardEngineService,
    private readonly notifications: NotificationsService,
    private readonly prisma: PrismaService,
  ) {}

  async process(sms: RawSms, tenantId: string): Promise<DepositProcessingResult> {
    try {
      // 1. Detect and parse SMS
      const parser = this.parserRegistry.detect(sms);
      const parsedDeposit = parser.parse(sms);

      // 2. Generate Idempotency Key
      const rawData = `${tenantId}:${sms.sender}:${sms.body}:${sms.receivedAt.toISOString()}`;
      const idempotencyKey = crypto.createHash('sha256').update(rawData).digest('hex');

      // 3. Check for duplicates
      const existing = await this.prisma.deposit.findUnique({
        where: { idempotencyKey }
      });
      
      if (existing) {
        this.logger.warn(`Duplicate SMS detected for tenant ${tenantId}`);
        return { status: 'duplicate', depositId: existing.id };
      }

      // 4. Create Deposit Record
      const deposit = await this.prisma.deposit.create({
        data: {
          tenantId,
          idempotencyKey,
          senderPhone: sms.sender,
          senderCode: parsedDeposit.senderCode,
          amount: parsedDeposit.amount,
          walletType: parsedDeposit.walletName,
          rawSmsText: parsedDeposit.rawSmsText,
        }
      });

      // 5. Attempt Card Allocation
      const allocation = await this.cardEngine.reserveCard({
        tenantId,
        amount: parsedDeposit.amount,
        depositId: deposit.id,
        recipientPhone: parsedDeposit.senderCode,
      });

      // 6. Handle Pending or Success
      if (!allocation) {
        await this.prisma.pendingApproval.create({
          data: {
            tenantId,
            depositId: deposit.id,
          }
        });
        
        await this.notifications.sendSms({
          tenantId,
          toPhone: 'ADMIN', // notify admin
          message: `طلب قيد الانتظار: ${parsedDeposit.amount} من ${parsedDeposit.senderCode}`,
        }).catch(e => this.logger.error(e));

        return { status: 'pending', depositId: deposit.id };
      }

      // 7. Successful Allocation - Notify user
      const message = `تم استلام ${parsedDeposit.amount} بنجاح. كرتك: ${allocation.code}`;
      await this.notifications.sendSms({
        tenantId,
        toPhone: parsedDeposit.senderCode,
        message,
      }).catch(e => this.logger.error(e));

      return { 
        status: 'delivered', 
        transactionId: allocation.transactionId, 
        cardCode: allocation.code 
      };

    } catch (error: any) {
      this.logger.error(`Failed to process SMS: ${error.message}`);
      return { status: 'unknown' };
    }
  }
}
