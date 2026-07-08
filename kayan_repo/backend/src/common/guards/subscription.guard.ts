import { Injectable, CanActivate, ExecutionContext, ForbiddenException } from '@nestjs/common';

@Injectable()
export class SubscriptionGuard implements CanActivate {
  canActivate(context: ExecutionContext): boolean {
    const request = context.switchToHttp().getRequest();
    const tenant = request.tenant;
    if (!tenant) return true;

    if (tenant.status === 'active') {
      const now = new Date();
      if (!tenant.subscription || now > tenant.subscription.expiryDate) {
        throw new ForbiddenException({
          code: 'SUBSCRIPTION_EXPIRED',
          message: 'Your subscription has expired. Please renew to continue.',
        });
      }
    }
    return true;
  }
}
