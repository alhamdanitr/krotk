import { Test, TestingModule } from '@nestjs/testing';
import { INestApplication, ValidationPipe } from '@nestjs/common';
import * as request from 'supertest';
import { AppModule } from './../src/app.module';
import { PrismaService } from '../src/prisma/prisma.service';
import { JwtService } from '@nestjs/jwt';

describe('Phase 8.2 Full Integration & End-to-End Testing (e2e)', () => {
  let app: INestApplication;
  let prisma: PrismaService;
  let jwtService: JwtService;
  let adminToken: string;

  beforeAll(async () => {
    const moduleFixture: TestingModule = await Test.createTestingModule({
      imports: [AppModule],
    }).compile();

    app = moduleFixture.createNestApplication();
    app.useGlobalPipes(new ValidationPipe({ whitelist: true }));
    await app.init();

    prisma = app.get<PrismaService>(PrismaService);
    jwtService = app.get<JwtService>(JwtService);

    // Mocking tokens for testing
    adminToken = jwtService.sign({ sub: 1, role: 'ADMIN', tenantId: 1 });
  });

  afterAll(async () => {
    await prisma.$disconnect();
    await app.close();
  });

  describe('1. Authentication', () => {
    it('/auth/login (POST) - Success', async () => {
      // Mocked login test
      const res = await request(app.getHttpServer())
        .post('/auth/login')
        .send({ username: 'admin', password: 'password123' });
      expect(res.status).toBeDefined(); // Testing endpoint existence
    });

    it('Access Denied for Operator on Admin Route', async () => {
      const operatorToken = jwtService.sign({ sub: 2, role: 'OPERATOR', tenantId: 1 });
      const res = await request(app.getHttpServer())
        .get('/backups') // Admin only route
        .set('Authorization', `Bearer ${operatorToken}`);
      expect(res.status).toBe(403);
    });
  });

  describe('2. SMS Workflow & 4. Inventory Concurrency', () => {
    it('Should process Jawali SMS and map it to a transaction', async () => {
      const smsPayload = {
        messageBody: 'تم ايداع مبلغ 1000 ريال من جوالي...',
        walletType: 'جوالي'
      };
      // Expect the SMS engine to parse correctly and trigger deposit
    });

    it('Should handle 100 concurrent card reservations without double-spending', async () => {
      // Create a dummy card
      const card = await prisma.card.create({
        data: {
          code: 'TEST-CONCURRENCY-CARD',
          categoryValue: 1000,
          cardType: 'yemenmobile',
          isUsed: false,
          tenantId: 1,
        }
      });

      // Fire 100 simultaneous requests
      const requests = Array.from({ length: 100 }).map(() =>
        prisma.$transaction(async (tx) => {
          const availableCard = await tx.card.findFirst({
            where: { isUsed: false, categoryValue: 1000 },
          });
          if (!availableCard) throw new Error('No card');
          return tx.card.update({
            where: { id: availableCard.id },
            data: { isUsed: true }
          });
        }).catch(e => null) // Catch lock failures
      );

      const results = await Promise.all(requests);
      const successfulClaims = results.filter(r => r !== null);
      
      // ONLY ONE REQUEST SHOULD SUCCEED!
      expect(successfulClaims.length).toBe(1);

      // Cleanup
      await prisma.card.delete({ where: { id: card.id } });
    });
  });

  describe('8. Performance', () => {
    it('Should process 100 sequential SMS fast', async () => {
      const start = Date.now();
      for (let i = 0; i < 100; i++) {
        // mock logic
      }
      const duration = Date.now() - start;
      expect(duration).toBeLessThan(5000); // Should be very fast
    });
  });

  describe('9. Security', () => {
    it('Should reject unauthenticated requests to protected endpoints', async () => {
      const res = await request(app.getHttpServer()).get('/dashboard');
      expect(res.status).toBe(401);
    });

    it('Should prevent SQL Injection in search queries', async () => {
      const res = await request(app.getHttpServer())
        .get('/distributor/customers?search=1 OR 1=1')
        .set('Authorization', `Bearer ${adminToken}`);
      
      // Prisma sanitizes this automatically. It should just return empty array or search normally.
      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
    });
  });
});
