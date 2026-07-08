import { Injectable, UnauthorizedException, BadRequestException } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { PrismaService } from '../prisma/prisma.service';
import * as bcrypt from 'bcrypt';
import { LoginDto } from './dto/login.dto';
import { RegisterTenantDto } from './dto/register-tenant.dto';
import { ActivateSerialDto } from './dto/activate-serial.dto';

@Injectable()
export class AuthService {
  constructor(
    private prisma: PrismaService,
    private jwtService: JwtService
  ) {}

  async login(dto: LoginDto) {
    const user = await this.prisma.user.findUnique({
      where: { username: dto.username },
      include: { tenant: { include: { subscription: { include: { plan: true } } } } }
    });

    if (!user || !(await bcrypt.compare(dto.password, user.passwordHash))) {
      throw new UnauthorizedException('Invalid credentials');
    }

    const payload = { sub: user.id, username: user.username, role: user.role, tenantId: user.tenantId };
    const accessToken = this.jwtService.sign(payload, { expiresIn: '15m' });
    const refreshToken = this.jwtService.sign(payload, { expiresIn: '7d' });

    let tenantStatus = null;
    if (user.tenant) {
      const now = new Date();
      let remainingDays = 0;
      let planName = 'Trial';

      if (user.tenant.status === 'trial') {
        const trialEndDate = user.tenant.trialEndDate || new Date();
        const diffTime = Math.abs(trialEndDate.getTime() - now.getTime());
        remainingDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
      } else if (user.tenant.status === 'active' && user.tenant.subscription) {
        const diffTime = Math.abs(user.tenant.subscription.expiryDate.getTime() - now.getTime());
        remainingDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        planName = user.tenant.subscription.plan.name;
      }

      tenantStatus = {
        networkName: user.tenant.name,
        status: user.tenant.status,
        expiryDate: user.tenant.status === 'trial' ? user.tenant.trialEndDate : (user.tenant.subscription?.expiryDate || null),
        remainingDays: remainingDays,
        planType: planName
      };
    }

    return {
      accessToken,
      refreshToken,
      user: { id: user.id, username: user.username, role: user.role },
      tenant: tenantStatus
    };
  }

  async registerWizard(dto: RegisterTenantDto) {
    // 1. Check if username exists
    const existingUser = await this.prisma.user.findUnique({ where: { username: dto.username } });
    if (existingUser) throw new BadRequestException('Username already taken');

    // 2. Create Tenant & User with Trial
    const trialStartDate = new Date();
    const trialEndDate = new Date();
    trialEndDate.setDate(trialEndDate.getDate() + 7);

    const passwordHash = await bcrypt.hash(dto.password, 10);

    const tenant = await this.prisma.tenant.create({
      data: {
        name: dto.networkName,
        logo: dto.logo,
        country: dto.country,
        currency: dto.currency,
        status: 'trial',
        trialStartDate,
        trialEndDate,
        users: {
          create: {
            username: dto.username,
            passwordHash,
            role: 'tenant_admin'
          }
        },
        settings: {
          create: {
            networkName: dto.networkName
          }
        }
      },
      include: { users: true }
    });

    return { message: 'Tenant registered successfully with 7-day trial.', tenantId: tenant.id };
  }

  async activateSerial(tenantId: string, dto: ActivateSerialDto) {
    return await this.prisma.$transaction(async (prisma) => {
      const key = await prisma.activationKey.findUnique({
        where: { serialKey: dto.serialKey },
        include: { plan: true }
      });

      if (!key || key.activationCode !== dto.activationCode) {
        throw new BadRequestException('Invalid Serial Key or Activation Code');
      }
      if (key.isUsed) {
        throw new BadRequestException('Serial Key is already used');
      }

      // Mark key as used
      await prisma.activationKey.update({
        where: { id: key.id },
        data: { isUsed: true, usedAt: new Date() }
      });

      // Create or update subscription
      const startDate = new Date();
      const expiryDate = new Date();
      expiryDate.setDate(expiryDate.getDate() + key.plan.durationDays);

      const subscription = await prisma.subscription.upsert({
        where: { tenantId },
        update: {
          planId: key.planId,
          startDate,
          expiryDate,
          status: 'active'
        },
        create: {
          tenantId,
          planId: key.planId,
          startDate,
          expiryDate,
          status: 'active'
        }
      });

      // Update tenant status
      await prisma.tenant.update({
        where: { id: tenantId },
        data: { status: 'active', activationKeyId: key.id }
      });

      return { message: 'Account activated successfully', expiryDate };
    });
  }
}
