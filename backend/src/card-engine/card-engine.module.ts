import { Module } from '@nestjs/common';
import { CardEngineService } from './card-engine.service';

@Module({
  providers: [CardEngineService],
  exports: [CardEngineService],
})
export class CardEngineModule {}
