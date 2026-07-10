import { Injectable } from '@nestjs/common';

@Injectable()
export class AppService {
  getSystemInfo(): object {
    return {
      name: 'Kurotek SaaS API',
      version: '2.0.0',
      status: 'operational',
      timestamp: new Date().toISOString(),
      uptime: process.uptime(),
    };
  }
}
