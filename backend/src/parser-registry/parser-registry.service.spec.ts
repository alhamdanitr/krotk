import { Test, TestingModule } from '@nestjs/testing';
import { ParserRegistryService } from './parser-registry.service';
import { ISmsParser, RawSms, ParsedDeposit } from '../common/interfaces/sms-parser.interface';
import { UnsupportedWalletException } from '../common/exceptions/engine.exceptions';

const mockSms: RawSms = { sender: 'Jaib', body: 'test body', receivedAt: new Date(), tenantId: 'tenant-1' };

class MockParser implements ISmsParser {
  walletName = 'MockWallet';
  canParse = (sms: RawSms) => sms.sender === 'Jaib';
  parse = (sms: RawSms): ParsedDeposit => ({ amount: 500, senderCode: 'X1', walletName: 'MockWallet', rawSmsText: sms.body });
}

describe('ParserRegistryService', () => {
  let service: ParserRegistryService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [ParserRegistryService],
    }).compile();
    service = module.get<ParserRegistryService>(ParserRegistryService);
  });

  it('should detect a registered parser', () => {
    service.register(new MockParser());
    const parser = service.detect(mockSms);
    expect(parser.walletName).toBe('MockWallet');
  });

  it('should throw UnsupportedWalletException for unknown sender', () => {
    const unknownSms = { ...mockSms, sender: 'Unknown' };
    expect(() => service.detect(unknownSms)).toThrow(UnsupportedWalletException);
  });
});
