import { Injectable } from '@nestjs/common';
import { ISmsParser, RawSms } from '../common/interfaces/sms-parser.interface';
import { UnsupportedWalletException } from '../common/exceptions/engine.exceptions';

@Injectable()
export class ParserRegistryService {
  private readonly parsers: ISmsParser[] = [];

  register(parser: ISmsParser): void {
    this.parsers.push(parser);
  }

  detect(sms: RawSms): ISmsParser {
    for (const parser of this.parsers) {
      if (parser.canParse(sms)) return parser;
    }
    throw new UnsupportedWalletException(sms.sender);
  }

  getRegisteredParsers(): string[] {
    return this.parsers.map(p => p.walletName);
  }
}
