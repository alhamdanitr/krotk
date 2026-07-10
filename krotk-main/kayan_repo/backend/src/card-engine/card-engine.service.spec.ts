import { Test, TestingModule } from '@nestjs/testing';
import { CardEngineService } from './card-engine.service';
import { PrismaService } from '../prisma/prisma.service';

describe('CardEngineService', () => {
  let service: CardEngineService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [CardEngineService, { provide: PrismaService, useValue: {} }],
    }).compile();
    service = module.get<CardEngineService>(CardEngineService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });

  // TODO Phase 4: reserve card successfully (atomic transaction)
  // TODO Phase 4: return null when no card available
  // TODO Phase 4: concurrent requests should not double-dispatch
});
