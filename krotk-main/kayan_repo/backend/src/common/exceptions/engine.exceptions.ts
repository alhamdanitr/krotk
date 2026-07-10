import { HttpException, HttpStatus } from '@nestjs/common';

export class DuplicateSmsException extends HttpException {
  constructor(idempotencyKey: string) {
    super({ code: 'DUPLICATE_SMS', message: 'SMS already processed', idempotencyKey }, HttpStatus.CONFLICT);
  }
}

export class CardNotFoundException extends HttpException {
  constructor(amount: number) {
    super({ code: 'CARD_NOT_FOUND', message: `No available card for amount ${amount}` }, HttpStatus.NOT_FOUND);
  }
}

export class CardAlreadyReservedException extends HttpException {
  constructor(cardId: string) {
    super({ code: 'CARD_ALREADY_RESERVED', message: `Card ${cardId} is already reserved` }, HttpStatus.CONFLICT);
  }
}

export class UnsupportedWalletException extends HttpException {
  constructor(sender: string) {
    super({ code: 'UNSUPPORTED_WALLET', message: `Cannot identify wallet from sender: ${sender}` }, HttpStatus.UNPROCESSABLE_ENTITY);
  }
}

export class SubscriptionExpiredException extends HttpException {
  constructor() {
    super({ code: 'SUBSCRIPTION_EXPIRED', message: 'Your subscription has expired. Please renew.' }, HttpStatus.FORBIDDEN);
  }
}

export class InvalidDepositException extends HttpException {
  constructor(reason: string) {
    super({ code: 'INVALID_DEPOSIT', message: `Invalid deposit: ${reason}` }, HttpStatus.BAD_REQUEST);
  }
}

export class TenantSuspendedException extends HttpException {
  constructor() {
    super({ code: 'TENANT_SUSPENDED', message: 'Tenant account is suspended.' }, HttpStatus.FORBIDDEN);
  }
}
