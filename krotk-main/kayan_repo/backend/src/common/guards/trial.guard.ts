import { Injectable, CanActivate, ExecutionContext, ForbiddenException } from '@nestjs/common';

@Injectable()
export class TrialGuard implements CanActivate {
  canActivate(context: ExecutionContext): boolean {
    const request = context.switchToHttp().getRequest();
    const tenant = request.tenant;
    if (!tenant) return true;

    if (tenant.status === 'trial') {
      const now = new Date();
      if (tenant.trialEndDate && now > tenant.trialEndDate) {
        throw new ForbiddenException({
          code: 'TRIAL_EXPIRED',
          message: 'Your 7-day free trial has expired. Please activate your account with a Serial Key.',
        });
      }
    }
    return true;
  }
}
