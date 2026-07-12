import { ISmsParser, RawSms, ParsedDeposit } from '../../common/interfaces/sms-parser.interface';
import { Injectable } from '@nestjs/common';

/** ون كاش - OneCash Yemen */
@Injectable()
export class OneCashParser implements ISmsParser {
  readonly walletName = 'OneCash';
  private readonly AMOUNT_PATTERN = /(?:مبلغ|amount)[\s:]+([\d,]+(?:\.\d+)?)/i;
  private readonly SENDER_PATTERN = /(?:من|from|مرسل)[\s:]+([\w\d]+)/i;
  private readonly SENDERS = ['OneCash', 'ONE-CASH', '700700'];

  canParse(sms: RawSms): boolean {
    return this.SENDERS.includes(sms.sender) || /one.?cash|ون كاش/i.test(sms.body);
  }

  parse(sms: RawSms): ParsedDeposit {
    const amountMatch = sms.body.match(this.AMOUNT_PATTERN);
    const senderMatch = sms.body.match(this.SENDER_PATTERN);
    if (!amountMatch) throw new Error(`[OneCashParser] Cannot extract amount from: ${sms.body}`);
    return {
      amount: Math.round(parseFloat(amountMatch[1].replace(/,/g, ''))),
      senderCode: senderMatch?.[1] ?? 'UNKNOWN',
      walletName: this.walletName,
      rawSmsText: sms.body,
    };
  }
}
