import { ISmsParser, RawSms, ParsedDeposit } from '../../common/interfaces/sms-parser.interface';
import { Injectable } from '@nestjs/common';

/** بنك كريمي - Karimi Bank */
@Injectable()
export class KarimiParser implements ISmsParser {
  readonly walletName = 'Karimi';
  private readonly AMOUNT_PATTERN = /([\d,]+(?:\.\d+)?)\s*(?:ريال|YER|ر\.ي)/i;
  private readonly SENDER_PATTERN = /(?:من حساب|من|sender)\s+([\w\d]+)/i;
  private readonly SENDERS = ['Karimi', 'BankKarimi', '01'];

  canParse(sms: RawSms): boolean {
    return this.SENDERS.includes(sms.sender) || /karimi|كريمي/i.test(sms.body);
  }

  parse(sms: RawSms): ParsedDeposit {
    const amountMatch = sms.body.match(this.AMOUNT_PATTERN);
    const senderMatch = sms.body.match(this.SENDER_PATTERN);
    if (!amountMatch) throw new Error(`[KarimiParser] Cannot extract amount from: ${sms.body}`);
    return {
      amount: Math.round(parseFloat(amountMatch[1].replace(/,/g, ''))),
      senderCode: senderMatch?.[1] ?? 'UNKNOWN',
      walletName: this.walletName,
      rawSmsText: sms.body,
    };
  }
}
