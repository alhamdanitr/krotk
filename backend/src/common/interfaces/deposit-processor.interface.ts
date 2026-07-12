import { RawSms, ParsedDeposit } from './sms-parser.interface';

export interface IDepositProcessor {
  process(sms: RawSms, tenantId: string): Promise<DepositProcessingResult>;
}

export type DepositProcessingResult =
  | { status: 'delivered'; transactionId: string; cardCode: string }
  | { status: 'pending';   depositId: string }
  | { status: 'duplicate'; depositId: string }
  | { status: 'unknown' };
