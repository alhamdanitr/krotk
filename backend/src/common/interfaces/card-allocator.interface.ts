export interface CardAllocationRequest {
  tenantId: string;
  amount: number;
  depositId: string;
  recipientPhone: string;
}

export interface CardAllocationResult {
  cardId: string;
  code: string;
  username?: string;
  password?: string;
  transactionId: string;
}

export interface ICardAllocator {
  reserveCard(req: CardAllocationRequest): Promise<CardAllocationResult | null>;
}
