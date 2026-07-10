import { ISmsParser, RawSms, ParsedDeposit } from '../../common/interfaces/sms-parser.interface';
import { Injectable } from '@nestjs/common';

/** حاسب - Hasib */
@Injectable()
export class HasibParser implements ISmsParser {
  readonly walletName = 'Hasib';
  private readonly AMOUNT_PATTERN = /([\d,]+(?:\.\d+)?)\s*(?:ريال|YER)/i;
  private readonly SENDER_PATTERN = /(?:من|from)[\s:]+([\w\d]+)/i;
  private readonly SENDERS = ['Hasib', 'حاسب'];

  canParse(sms: RawSms): boolean {
    return this.SENDERS.includes(sms.sender) || /hasib|حاسب/i.test(sms.body);
  }

  parse(sms: RawSms): ParsedDeposit {
    const amountMatch = sms.body.match(this.AMOUNT_PATTERN);
    const senderMatch = sms.body.match(this.SENDER_PATTERN);
    if (!amountMatch) throw new Error(`[HasibParser] Cannot extract amount from: ${sms.body}`);
    return {
      amount: Math.round(parseFloat(amountMatch[1].replace(/,/g, ''))),
      senderCode: senderMatch?.[1] ?? 'UNKNOWN',
      walletName: this.walletName,
      rawSmsText: sms.body,
    };
  }
}
