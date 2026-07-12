import { Module } from '@nestjs/common';
import { SerialManagerController } from './serial-manager.controller';
import { SerialManagerService } from './serial-manager.service';

@Module({
  controllers: [SerialManagerController],
  providers: [SerialManagerService],
  exports: [SerialManagerService],
})
export class SerialManagerModule {}
