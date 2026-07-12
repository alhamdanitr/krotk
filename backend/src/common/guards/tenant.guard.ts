import { Injectable, CanActivate, ExecutionContext, ForbiddenException } from '@nestjs/common';
import { PrismaService } from '../../prisma/prisma.service';

@Injectable()
export class TenantGuard implements CanActivate {
  constructor(private prisma: PrismaService) {}

  async canActivate(context: ExecutionContext): Promise<boolean> {
    const request = context.switchToHttp().getRequest();
    const user = request.user;
    if (!user || !user.tenantId) return true; // Super admins or non-tenant users

    const tenant = await this.prisma.tenant.findUnique({
      where: { id: user.tenantId },
      include: { subscription: true }
    });

    if (!tenant) throw new ForbiddenException('Tenant not found.');
    if (!tenant.isActive) throw new ForbiddenException('Tenant account is disabled.');

    // Inject tenant object into request for other guards to use
    request.tenant = tenant;
    return true;
  }
}
