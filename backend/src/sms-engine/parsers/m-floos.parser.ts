import { ISmsParser, RawSms, ParsedDeposit } from '../../common/interfaces/sms-parser.interface';
import { Injectable } from '@nestjs/common';

/** إم فلوس - M-Floos */
@Injectable()
export class MFloosParser implements ISmsParser {
  readonly walletName = 'MFloos';
  private readonly AMOUNT_PATTERN = /([\d,]+(?:\.\d+)?)\s*(?:YER|ريال|فلس)/i;
  private readonly SENDER_PATTERN = /(?:من|sender)[\s:]+([\w\d]+)/i;
  private readonly SENDERS = ['M-Floos', 'MFloos', 'Floos'];

  canParse(sms: RawSms): boolean {
    return this.SENDERS.includes(sms.sender) || /m.?floos|ام فلوس|إم فلوس/i.test(sms.body);
  }

  parse(sms: RawSms): ParsedDeposit {
    const amountMatch = sms.body.match(this.AMOUNT_PATTERN);
    const senderMatch = sms.body.match(this.SENDER_PATTERN);
    if (!amountMatch) throw new Error(`[MFloosParser] Cannot extract amount from: ${sms.body}`);
    return {
      amount: Math.round(parseFloat(amountMatch[1].replace(/,/g, ''))),
      senderCode: senderMatch?.[1] ?? 'UNKNOWN',
      walletName: this.walletName,
      rawSmsText: sms.body,
    };
  }
}
