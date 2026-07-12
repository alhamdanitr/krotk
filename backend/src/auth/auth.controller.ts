import { Controller, Post, Body, HttpCode, HttpStatus, UseGuards, Req } from '@nestjs/common';
import { AuthService } from './auth.service';
import { LoginDto } from './dto/login.dto';
import { RegisterTenantDto } from './dto/register-tenant.dto';
import { ActivateSerialDto } from './dto/activate-serial.dto';
import { ApiTags, ApiOperation, ApiResponse, ApiBearerAuth } from '@nestjs/swagger';
import { Public } from '../common/decorators/public.decorator';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { CurrentUser } from '../common/decorators/current-user.decorator';

@ApiTags('Authentication & Onboarding')
@Controller('auth')
export class AuthController {
  constructor(private readonly authService: AuthService) {}

  @Public()
  @Post('login')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Login user and get tokens & status' })
  @ApiResponse({ status: 200, description: 'Success' })
  @ApiResponse({ status: 401, description: 'Invalid credentials' })
  login(@Body() dto: LoginDto) {
    return this.authService.login(dto);
  }

  @Public()
  @Post('wizard/register')
  @ApiOperation({ summary: 'Step 2 & 3: Register Tenant and start 7-day trial' })
  @ApiResponse({ status: 201, description: 'Tenant created successfully' })
  registerWizard(@Body() dto: RegisterTenantDto) {
    return this.authService.registerWizard(dto);
  }

  @ApiBearerAuth()
  @UseGuards(JwtAuthGuard)
  @Post('activate')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Activate account using Serial Key' })
  @ApiResponse({ status: 200, description: 'Activated successfully' })
  activateSerial(@CurrentUser() user: any, @Body() dto: ActivateSerialDto) {
    return this.authService.activateSerial(user.tenantId, dto);
  }
}
