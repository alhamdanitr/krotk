// Events are plain objects — use NestJS EventEmitter2 to dispatch them

export class SmsReceivedEvent {
  constructor(
    public readonly tenantId: string,
    public readonly sender: string,
    public readonly body: string,
    public readonly receivedAt: Date,
  ) {}
}

export class DepositCreatedEvent {
  constructor(
    public readonly depositId: string,
    public readonly tenantId: string,
    public readonly amount: number,
    public readonly walletName: string,
  ) {}
}

export class CardReservedEvent {
  constructor(
    public readonly cardId: string,
    public readonly depositId: string,
    public readonly tenantId: string,
  ) {}
}

export class CardDeliveredEvent {
  constructor(
    public readonly transactionId: string,
    public readonly tenantId: string,
    public readonly recipientPhone: string,
    public readonly cardCode: string,
  ) {}
}

export class CardDeliveryFailedEvent {
  constructor(
    public readonly depositId: string,
    public readonly tenantId: string,
    public readonly reason: string,
  ) {}
}

export class TransactionCreatedEvent {
  constructor(
    public readonly transactionId: string,
    public readonly tenantId: string,
    public readonly amount: number,
  ) {}
}
