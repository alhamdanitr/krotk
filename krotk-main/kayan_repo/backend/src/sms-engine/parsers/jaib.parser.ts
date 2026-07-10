import { ISmsParser, RawSms, ParsedDeposit } from '../../common/interfaces/sms-parser.interface';
import { Injectable } from '@nestjs/common';

/** محفظة جيب - Jaib Yemen */
@Injectable()
export class JaibParser implements ISmsParser {
  readonly walletName = 'Jaib';
  private readonly AMOUNT_PATTERN = /(?:استلمت|تم استلام|وصلك)\s*([\d,]+(?:\.\d+)?)\s*(?:ريال|YER)/i;
  private readonly SENDER_PATTERN = /(?:من|from)\s+([\w]+)/i;
  private readonly SENDERS = ['Jaib', '711711'];

  canParse(sms: RawSms): boolean {
    return this.SENDERS.includes(sms.sender) || /jaib/i.test(sms.body);
  }

  parse(sms: RawSms): ParsedDeposit {
    const amountMatch = sms.body.match(this.AMOUNT_PATTERN);
    const senderMatch = sms.body.match(this.SENDER_PATTERN);
    if (!amountMatch) throw new Error(`[JaibParser] Cannot extract amount from: ${sms.body}`);
    return {
      amount: Math.round(parseFloat(amountMatch[1].replace(/,/g, ''))),
      senderCode: senderMatch?.[1] ?? 'UNKNOWN',
      walletName: this.walletName,
      rawSmsText: sms.body,
    };
  }
}
