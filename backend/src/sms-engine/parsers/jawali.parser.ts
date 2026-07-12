import { ISmsParser, RawSms, ParsedDeposit } from '../../common/interfaces/sms-parser.interface';
import { Injectable } from '@nestjs/common';

/** شبكة جوالي - Jawali Yemen */
@Injectable()
export class JawaliParser implements ISmsParser {
  readonly walletName = 'Jawali';
  private readonly AMOUNT_PATTERN = /([\d,]+(?:\.\d+)?)\s*(?:ر\.ي|ريال|YER)/i;
  private readonly SENDER_PATTERN = /(?:المرسل|من|رقم)\s*[:#]?\s*([\d]+)/i;
  private readonly SENDERS = ['Jawali', '717717', '773773'];

  canParse(sms: RawSms): boolean {
    return this.SENDERS.includes(sms.sender) || /jawali|جوالي/i.test(sms.body);
  }

  parse(sms: RawSms): ParsedDeposit {
    const amountMatch = sms.body.match(this.AMOUNT_PATTERN);
    const senderMatch = sms.body.match(this.SENDER_PATTERN);
    if (!amountMatch) throw new Error(`[JawaliParser] Cannot extract amount from: ${sms.body}`);
    return {
      amount: Math.round(parseFloat(amountMatch[1].replace(/,/g, ''))),
      senderCode: senderMatch?.[1] ?? 'UNKNOWN',
      walletName: this.walletName,
      rawSmsText: sms.body,
    };
  }
}
