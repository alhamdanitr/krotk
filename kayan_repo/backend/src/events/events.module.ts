import { Module } from '@nestjs/common';
import { EventsGateway } from './events.gateway';
import { TransactionsModule } from '../transactions/transactions.module';

@Module({
  imports: [TransactionsModule],
  providers: [EventsGateway],
  exports: [EventsGateway],
})
export class EventsModule {}
