import { HmacMiddleware } from './common/middleware/hmac.middleware';
import { ThrottlerModule, ThrottlerGuard } from '@nestjs/throttler';
import { MiddlewareConsumer, RequestMethod } from '@nestjs/common';
import { Module } from '@nestjs/common';
import { PrismaModule } from './prisma/prisma.module';
import { AuthModule } from './auth/auth.module';
import { DashboardModule } from './dashboard/dashboard.module';
import { ReportsModule } from './reports/reports.module';
import { TransactionsModule } from './transactions/transactions.module';
import { DepositsModule } from './deposits/deposits.module';
import { CardsModule } from './cards/cards.module';
import { InventoryModule } from './inventory/inventory.module';
import { WalletsModule } from './wallets/wallets.module';
import { SmsEngineModule } from './sms-engine/sms-engine.module';
import { DistributorModule } from './distributor/distributor.module';
import { EventsModule } from './events/events.module';
import { BackupsModule } from './backups/backups.module';
import { SerialManagerModule } from './serial-manager/serial-manager.module';
import { APP_GUARD } from '@nestjs/core';
import { JwtAuthGuard } from './common/guards/jwt-auth.guard';
import { TenantGuard } from './common/guards/tenant.guard';
import { TrialGuard } from './common/guards/trial.guard';
import { SubscriptionGuard } from './common/guards/subscription.guard';
import { RolesGuard } from './common/guards/roles.guard';

@Module({
  imports: [
    ThrottlerModule.forRoot([{
      ttl: 60000,
      limit: 100,
    }]),
    PrismaModule,
    AuthModule,
    DashboardModule,
    ReportsModule,
    TransactionsModule,
    DepositsModule,
    CardsModule,
    InventoryModule,
    WalletsModule,
    SmsEngineModule,
    DistributorModule, // ◀ موديول حاسبة الموزع
    EventsModule,
    BackupsModule,
    SerialManagerModule, // ◀ موديول إدارة وتفعيل السيريالات والأمان
  ],
  providers: [
    { provide: APP_GUARD, useClass: ThrottlerGuard },
    { provide: APP_GUARD, useClass: JwtAuthGuard },   // Global JWT Guard
    { provide: APP_GUARD, useClass: TenantGuard },    // Global Tenant Guard
    { provide: APP_GUARD, useClass: TrialGuard },     // Global Trial Guard
    { provide: APP_GUARD, useClass: SubscriptionGuard }, // Global Sub Guard
    { provide: APP_GUARD, useClass: RolesGuard },     // Global Roles Guard
  ],
})
export class AppModule {
  configure(consumer: MiddlewareConsumer) {
    consumer
      .apply(HmacMiddleware)
      .exclude(
        { path: '', method: RequestMethod.ALL },
        { path: 'auth/(.*)', method: RequestMethod.ALL },
        { path: 'health', method: RequestMethod.ALL },
        { path: 'api/v1/serial/(.*)', method: RequestMethod.ALL },
        { path: 'api/v1/admin/serial/(.*)', method: RequestMethod.ALL }
      )
      .forRoutes('*');
  }
}
