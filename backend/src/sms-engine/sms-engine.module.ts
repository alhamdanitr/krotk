import { Module } from '@nestjs/common';
import { SmsEngineController } from './sms-engine.controller';
import { SmsEngineService } from './sms-engine.service';
import { ParserRegistryModule } from '../parser-registry/parser-registry.module';
import { CardEngineModule } from '../card-engine/card-engine.module';
import { NotificationsModule } from '../notifications/notifications.module';

@Module({
  imports: [
    ParserRegistryModule,
    CardEngineModule,
    NotificationsModule,
  ],
  controllers: [SmsEngineController],
  providers: [SmsEngineService],
  exports: [SmsEngineService],
})
export class SmsEngineModule {}
