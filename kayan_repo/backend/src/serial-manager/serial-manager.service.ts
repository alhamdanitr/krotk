import { Injectable, UnauthorizedException, BadRequestException, NotFoundException } from '@nestjs/common';
import * as crypto from 'crypto';
import * as jwt from 'jsonwebtoken';
import { PrismaService } from '../prisma/prisma.service';

// ---------------------------------------------------------------------------
// شكل الاستجابة الخارجي (snake_case) يبقى كما هو تمامًا حتى تبقى متوافقة مع
// server/api_server.js وتطبيق serial-admin بدون أي تعديل عليهما. Prisma
// نفسها تستخدم camelCase داخليًا (الاتفاقية القياسية) ونحوّلها هنا فقط عند
// إعادة الإرسال للعميل.
// ---------------------------------------------------------------------------

interface ClientJson {
  id: number;
  name: string;
  network_name: string;
  phone: string;
  notes: string | null;
}

interface SerialJson {
  id: number;
  client_id: number;
  serial_key: string;
  device_id: string | null;
  duration_months: number;
  start_date: string;
  end_date: string;
  status: string;
  notes: string | null;
}

function toDateOnly(d: Date): string {
  return d.toISOString().split('T')[0];
}

function clientToJson(c: { id: number; name: string; networkName: string; phone: string; notes: string | null }): ClientJson {
  return { id: c.id, name: c.name, network_name: c.networkName, phone: c.phone, notes: c.notes };
}

function serialToJson(s: {
  id: number; clientId: number; serialKey: string; deviceId: string | null;
  durationMonths: number; startDate: Date; endDate: Date; status: string; notes: string | null;
}): SerialJson {
  return {
    id: s.id,
    client_id: s.clientId,
    serial_key: s.serialKey,
    device_id: s.deviceId,
    duration_months: s.durationMonths,
    start_date: toDateOnly(s.startDate),
    end_date: toDateOnly(s.endDate),
    status: s.status,
    notes: s.notes,
  };
}

@Injectable()
export class SerialManagerService {
  constructor(private prisma: PrismaService) {}

  // لا قيم افتراضية هنا: هذا المستودع عام على GitHub، وأي سر مكتوب صراحة بالكود
  // يصبح معروفًا لأي شخص. نفس القيم المستخدمة في server/api_server.js يجب أن
  // تُضبط هنا أيضًا عبر متغيرات البيئة إذا أردنا التوافق بين السيرفرين.
  private readonly HMAC_SECRET = SerialManagerService.requireEnv('HMAC_SECRET');
  private readonly JWT_SECRET = SerialManagerService.requireEnv('JWT_SECRET');
  private readonly OFFLINE_SALT = SerialManagerService.requireEnv('SERIAL_OFFLINE_SALT');

  private static requireEnv(name: string): string {
    const value = process.env[name];
    if (!value) {
      throw new Error(
        `${name} environment variable is not set. Refusing to start with a ` +
        `hardcoded fallback secret, since this repository is public.`
      );
    }
    return value;
  }

  async addLog(ip: string, endpoint: string, statusCode: number, message: string, payload: any = '') {
    await this.prisma.adminSecurityLog.create({
      data: {
        ip,
        endpoint,
        statusCode,
        message,
        payload: typeof payload === 'object' ? JSON.stringify(payload) : String(payload),
      },
    });
    console.log(`[LICENSING LOG] [${new Date().toISOString()}] [${ip}] [${endpoint}] [Status: ${statusCode}] - ${message}`);
  }

  // Verify HMAC signature of the payload to protect from MITM or tampering
  verifySignature(signature: string, timestamp: number, body: any): boolean {
    if (!signature || !timestamp) {
      return false;
    }

    // Anti-replay: refuse requests older than 5 minutes
    const now = Date.now();
    if (Math.abs(now - timestamp) > 300000) {
      return false;
    }

    const dataToSign = `${body.serial}:${body.deviceId}:${body.timestamp}:${body.nonce}`;
    const expectedSignature = crypto
      .createHmac('sha256', this.HMAC_SECRET)
      .update(dataToSign)
      .digest('hex')
      .toUpperCase();

    return signature.toUpperCase() === expectedSignature;
  }

  // -------------------------------------------------------------
  // 1. Client App validation endpoint logic
  // -------------------------------------------------------------
  async validateSerial(clientIp: string, signature: string, timestamp: number, body: any) {
    const { serial, deviceId } = body;

    if (!this.verifySignature(signature, timestamp, body)) {
      await this.addLog(clientIp, '/api/v1/serial/validate', 403, 'Signature verification failed (Potential tampering)', body);
      throw new UnauthorizedException('🛡️ فشل تأكيد أمان الطلب أو التوقيع الرقمي غير متطابق!');
    }

    // Input sanitization against injection
    if (!/^[A-Z0-9_-]{4,60}$/i.test(serial) || !/^[A-Z0-9_-]{4,40}$/i.test(deviceId)) {
      await this.addLog(clientIp, '/api/v1/serial/validate', 400, 'Potential injection block', { serial, deviceId });
      throw new BadRequestException('بيانات الإدخال تحتوي على رموز غير مسموح بها!');
    }

    const serialRecord = await this.prisma.adminSerial.findFirst({
      where: { serialKey: { equals: serial, mode: 'insensitive' } },
    });

    if (!serialRecord) {
      await this.addLog(clientIp, '/api/v1/serial/validate', 404, `Activation failed: Serial ${serial} not found.`);
      return {
        success: false,
        status: 'NOT_FOUND',
        message: 'السيريال غير صحيح! يرجى التواصل مع الدعم لطلب مفتاح تنشيط.'
      };
    }

    if (serialRecord.status === 'REVOKED') {
      await this.addLog(clientIp, '/api/v1/serial/validate', 403, `Activation rejected: Serial ${serial} is REVOKED.`);
      return {
        success: false,
        status: 'REVOKED',
        message: 'تم إلغاء أو تجميد هذا السيريال من لوحة التحكم! يرجى مراجعة الدعم.'
      };
    }

    const now = new Date();
    if (now > serialRecord.endDate || serialRecord.status === 'EXPIRED') {
      if (serialRecord.status !== 'EXPIRED') {
        await this.prisma.adminSerial.update({ where: { id: serialRecord.id }, data: { status: 'EXPIRED' } });
      }
      await this.addLog(clientIp, '/api/v1/serial/validate', 403, `Activation rejected: Serial ${serial} is EXPIRED.`);
      return {
        success: false,
        status: 'EXPIRED',
        message: 'هذا الترخيص منتهي الصلاحية! يرجى تجديد الاشتراك.'
      };
    }

    // First time activation & device-lock (Hardware locking)
    if (!serialRecord.deviceId) {
      await this.prisma.adminSerial.update({
        where: { id: serialRecord.id },
        data: { deviceId, status: 'ACTIVE' },
      });
      await this.addLog(clientIp, '/api/v1/serial/validate', 200, `Successfully bound device ${deviceId} to serial ${serial}`);

      const token = jwt.sign({ serial, deviceId }, this.JWT_SECRET, { expiresIn: '365d' });
      return {
        success: true,
        status: 'ACTIVE',
        message: '🔓 تم تفعيل وترخيص التطبيق وربطه بجهازك بنجاح للمرة الأولى!',
        token
      };
    }

    // Verify same device to prevent multi-device sharing
    if (serialRecord.deviceId !== deviceId) {
      await this.addLog(clientIp, '/api/v1/serial/validate', 403, `Device mismatch! Registered: ${serialRecord.deviceId}, Requesting: ${deviceId}`);
      return {
        success: false,
        status: 'REVOKED',
        message: '❌ فشل التفعيل! هذا الترخيص مخصص لجهاز هاتف آخر ومقفل أمنياً ضد الاستنساخ والمشاركة.'
      };
    }

    // Re-verify existing device
    await this.prisma.adminSerial.update({ where: { id: serialRecord.id }, data: { status: 'ACTIVE' } });
    await this.addLog(clientIp, '/api/v1/serial/validate', 200, `Re-verified active serial ${serial} for device ${deviceId}`);

    const token = jwt.sign({ serial, deviceId }, this.JWT_SECRET, { expiresIn: '365d' });
    return {
      success: true,
      status: 'ACTIVE',
      message: '✔ الترخيص ساري ومفعّل لجهازك بنجاح!',
      token
    };
  }

  // -------------------------------------------------------------
  // 2. Admin dashboard management endpoints logic
  // -------------------------------------------------------------
  async getStats() {
    const [total, active, unused, revoked, logsCount] = await Promise.all([
      this.prisma.adminSerial.count(),
      this.prisma.adminSerial.count({ where: { status: 'ACTIVE' } }),
      this.prisma.adminSerial.count({ where: { status: 'UNUSED' } }),
      this.prisma.adminSerial.count({ where: { status: 'REVOKED' } }),
      this.prisma.adminSecurityLog.count(),
    ]);
    return { total, active, unused, revoked, logsCount };
  }

  async getAllSerials() {
    const serials = await this.prisma.adminSerial.findMany({
      include: { client: true },
      orderBy: { id: 'desc' },
    });
    return serials.map(s => ({
      ...serialToJson(s),
      client_name: s.client?.name ?? 'Unknown',
      client_phone: s.client?.phone ?? '',
      client_network: s.client?.networkName ?? '',
    }));
  }

  async createSerial(body: any) {
    const { name, network_name, phone, duration_months, notes, device_id } = body;

    if (!name || !network_name || !phone) {
      throw new BadRequestException('يرجى تعبئة الحقول الإلزامية الاسم، الشبكة، والهاتف!');
    }

    // Generate online/offline verification hash from phone + salt (نفس منطق server/api_server.js)
    const rawData = phone.trim().toUpperCase() + this.OFFLINE_SALT;
    const hash = crypto.createHash('sha256').update(rawData).digest('hex').substring(0, 6).toUpperCase();
    const finalSerialKey = `${phone.trim().toUpperCase()}-KS${hash}`;

    // Prevent duplicate serial key
    const existing = await this.prisma.adminSerial.findUnique({ where: { serialKey: finalSerialKey } });
    if (existing) {
      throw new BadRequestException('السيريال الخاص بهذا الهاتف تم توليده مسبقاً!');
    }

    const start = new Date();
    const end = new Date();
    const months = parseInt(duration_months || 12, 10);
    end.setMonth(end.getMonth() + months);

    const isPreBound = !!(device_id && String(device_id).trim());

    const created = await this.prisma.$transaction(async (tx) => {
      const client = await tx.adminClient.create({
        data: { name, networkName: network_name, phone, notes: notes || '' },
      });
      const serial = await tx.adminSerial.create({
        data: {
          clientId: client.id,
          serialKey: finalSerialKey,
          deviceId: isPreBound ? String(device_id).trim().toUpperCase() : null,
          durationMonths: months,
          startDate: start,
          endDate: end,
          status: isPreBound ? 'ACTIVE' : 'UNUSED',
          notes: notes || '',
        },
      });
      return serial;
    });

    return { success: true, serial: serialToJson(created) };
  }

  async resetDeviceLock(id: number) {
    const serial = await this.prisma.adminSerial.findUnique({ where: { id } });
    if (!serial) {
      throw new NotFoundException('السيريال غير موجود!');
    }
    await this.prisma.adminSerial.update({
      where: { id },
      data: { deviceId: null, status: 'UNUSED' },
    });
    return { success: true, message: 'تم إلغاء قفل الجهاز بنجاح! السيريال جاهز للربط بهاتف جديد.' };
  }

  async toggleSerialStatus(id: number) {
    const serial = await this.prisma.adminSerial.findUnique({ where: { id } });
    if (!serial) {
      throw new NotFoundException('السيريال غير موجود!');
    }

    if (serial.status === 'REVOKED') {
      const newStatus = serial.deviceId ? 'ACTIVE' : 'UNUSED';
      await this.prisma.adminSerial.update({ where: { id }, data: { status: newStatus } });
      return { success: true, message: 'تمت إعادة تفعيل الترخيص بنجاح!', status: newStatus };
    } else {
      await this.prisma.adminSerial.update({ where: { id }, data: { status: 'REVOKED' } });
      return { success: true, message: 'تم إلغاء وتجميد ترخيص هذا السيريال بنجاح!', status: 'REVOKED' };
    }
  }

  async deleteSerial(id: number) {
    const serial = await this.prisma.adminSerial.findUnique({ where: { id } });
    if (!serial) {
      throw new NotFoundException('السيريال غير موجود!');
    }
    // حذف السيريال والعميل المرتبط به معًا (نفس سلوك server/api_server.js:
    // "تم حذف السيريال والعميل نهائياً من النظام")
    await this.prisma.adminClient.delete({ where: { id: serial.clientId } });
    return { success: true, message: 'تم حذف السيريال والعميل نهائياً من النظام!' };
  }

  async getLogs() {
    const logs = await this.prisma.adminSecurityLog.findMany({
      orderBy: { id: 'desc' },
      take: 100,
    });
    return logs.map(l => ({
      id: l.id,
      ip: l.ip,
      endpoint: l.endpoint,
      statusCode: l.statusCode,
      message: l.message,
      payload: l.payload,
      timestamp: l.timestamp.toISOString(),
    }));
  }
}
