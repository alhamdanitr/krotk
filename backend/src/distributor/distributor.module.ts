import { Module } from '@nestjs/common';
import { PrismaModule } from '../prisma/prisma.module';

// Pricing
import { DistributorPricingController } from './pricing/pricing.controller';
import { DistributorPricingService } from './pricing/pricing.service';

// Customers
import { DistributorCustomersController } from './customers/customers.controller';
import { DistributorCustomersService } from './customers/customers.service';

// Sales
import { DistributorSalesController } from './sales/sales.controller';
import { DistributorSalesService } from './sales/sales.service';

// Finance
import { DistributorFinanceController } from './finance/finance.controller';
import { DistributorFinanceService } from './finance/finance.service';

@Module({
  imports: [PrismaModule],
  controllers: [
    DistributorPricingController,
    DistributorCustomersController,
    DistributorSalesController,
    DistributorFinanceController,
  ],
  providers: [
    DistributorPricingService,
    DistributorCustomersService,
    DistributorSalesService,
    DistributorFinanceService,
  ],
  exports: [
    DistributorCustomersService, // مُصدَّر لأن DashboardService قد يحتاجه
  ],
})
export class DistributorModule {}
