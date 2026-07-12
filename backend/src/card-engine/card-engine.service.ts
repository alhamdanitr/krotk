import { Injectable, Logger } from '@nestjs/common';
import { ICardAllocator, CardAllocationRequest, CardAllocationResult } from '../common/interfaces/card-allocator.interface';
import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class CardEngineService implements ICardAllocator {
  private readonly logger = new Logger(CardEngineService.name);

  constructor(private readonly prisma: PrismaService) {}

  async reserveCard(req: CardAllocationRequest): Promise<CardAllocationResult | null> {
    try {
      return await this.prisma.$transaction(async (tx) => {
        // 1. SELECT FOR UPDATE SKIP LOCKED to prevent race conditions
        const cards: any[] = await tx.$queryRaw`
          SELECT id, code, username, password 
          FROM cards 
          WHERE tenant_id = ${req.tenantId} 
            AND category_value = ${req.amount} 
            AND is_used = false 
          LIMIT 1 
          FOR UPDATE SKIP LOCKED
        `;

        if (!cards || cards.length === 0) {
          this.logger.warn(`No cards available for amount ${req.amount} in tenant ${req.tenantId}`);
          return null;
        }

        const card = cards[0];

        // 2. Mark card as used
        await tx.card.update({
          where: { id: card.id },
          data: {
            isUsed: true,
            usedAt: new Date(),
          },
        });

        // 3. Create Transaction record
        const transaction = await tx.transaction.create({
          data: {
            tenantId: req.tenantId,
            depositId: req.depositId,
            cardId: card.id,
            amount: req.amount,
            recipientPhone: req.recipientPhone,
          },
        });

        // 4. Return successful allocation result
        return {
          cardId: card.id,
          code: card.code,
          username: card.username,
          password: card.password,
          transactionId: transaction.id,
        };
      });
    } catch (error: any) {
      this.logger.error(`Failed to reserve card: ${error.message}`, error.stack);
      throw error;
    }
  }
}
