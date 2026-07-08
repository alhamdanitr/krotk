export interface RawSms {
  sender: string;
  body: string;
  receivedAt: Date;
  tenantId: string;
}

export interface ParsedDeposit {
  amount: number;
  senderCode: string;
  walletName: string;
  rawSmsText: string;
}

export interface ISmsParser {
  readonly walletName: string;
  canParse(sms: RawSms): boolean;
  parse(sms: RawSms): ParsedDeposit;
}
