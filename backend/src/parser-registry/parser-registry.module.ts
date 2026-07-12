import { Module } from '@nestjs/common';
import { ParserRegistryService } from './parser-registry.service';

@Module({
  providers: [ParserRegistryService],
  exports: [ParserRegistryService],
})
export class ParserRegistryModule {}
