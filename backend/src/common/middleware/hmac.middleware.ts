import { Injectable, NestMiddleware, UnauthorizedException } from '@nestjs/common';
import { Request, Response, NextFunction } from 'express';
import * as crypto from 'crypto';

@Injectable()
export class HmacMiddleware implements NestMiddleware {
  use(req: Request, res: Response, next: NextFunction) {
    const signature = req.headers['x-hmac-signature'];

    if (!signature) {
      throw new UnauthorizedException('Missing HMAC signature');
    }

    const hmacSecret = process.env.HMAC_SECRET;
    
    if (!hmacSecret) {
      throw new UnauthorizedException('Server configuration error: HMAC_SECRET not set');
    }

    // For POST/PUT requests, we hash the body. For GET, it could just be the URL.
    // Assuming standard empty string if no body for simplicity, or JSON stringify.
    const payload = Object.keys(req.body || {}).length > 0 ? JSON.stringify(req.body) : '';

    const expectedSignature = crypto
      .createHmac('sha256', hmacSecret)
      .update(payload)
      .digest('hex');

    if (signature !== expectedSignature) {
      throw new UnauthorizedException('Invalid HMAC signature');
    }

    next();
  }
}
