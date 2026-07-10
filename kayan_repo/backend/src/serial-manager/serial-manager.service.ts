import { Injectable, OnModuleInit, UnauthorizedException, BadRequestException, NotFoundException } from '@nestjs/common';
import * as fs from 'fs';
import * as path from 'path';
import * as crypto from 'crypto';
import * as jwt from 'jsonwebtoken';

export interface Client {
  id: number;
  name: string;
  network_name: string;
  phone: string;
  notes: string;
}

export interface Serial {
  id: number;
  client_id: number;
  serial_key: string;
  device_id: string | null;
  duration_months: number;
  start_date: string;
  end_date: string;
  status: string; // UNUSED, ACTIVE, EXPIRED, REVOKED
  notes: string;
}

export interface SecurityLog {
  id: number;
  ip: string;
  endpoint: string;
  statusCode: number;
  message: string;
  payload: string;
  timestamp: string;
}

@Injectable()
export class SerialManagerService implements OnModuleInit {
  private dbPath = path.resolve(__dirname, 'serials_db.json');
  
  private clients: Client[] = [];
  private serials: Serial[] = [];
  private logs: SecurityLog[] = [];

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

  onModuleInit() {
    this.loadDatabase();
  }

  private loadDatabase() {
    try {
      if (fs.existsSync(this.dbPath)) {
        const data = JSON.parse(fs.readFileSync(this.dbPath, 'utf8'));
        this.clients = data.clients || [];
        this.serials = data.serials || [];
        this.logs = data.logs || [];
      } else {
        // Seed default records if file doesn't exist
        this.clients = [
          { id: 1, name: 'أحمد بن سعد', network_name: 'شبكة الدحشة', phone: '771112223', notes: 'العميل الافتراضي الأول' }
        ];
        
        // Generate a seed serial that works offline/online
        const seedPhone = '771112223';
        const rawData = seedPhone.trim().toUpperCase() + this.OFFLINE_SALT;
        const hash = crypto.createHash('sha256').update(rawData).digest('hex').substring(0, 6).toUpperCase();
        const seedSerialKey = `${seedPhone}-KS${hash}`;

        this.serials = [
          {
            id: 1,
            client_id: 1,
            serial_key: seedSerialKey,
            device_id: null, // Allow binding on first activation
            duration_months: 12,
            start_date: new Date().toISOString().split('T')[0],
            end_date: new Date(Date.now() + 365 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
            status: 'UNUSED',
            notes: 'سيريال تجريبي نشط تلقائي'
          }
        ];
        this.logs = [];
        this.saveDatabase();
      }
    } catch (error) {
      console.error('Failed to load serials database', error);
    }
  }

  private saveDatabase() {
    try {
      const data = {
        clients: this.clients,
        serials: this.serials,
        logs: this.logs
      };
      fs.writeFileSync(this.dbPath, JSON.stringify(data, null, 2), 'utf8');
    } catch (error) {
      console.error('Failed to save serials database', error);
    }
  }

  addLog(ip: string, endpoint: string, statusCode: number, message: string, payload: any = '') {
    const entry: SecurityLog = {
      id: this.logs.length + 1,
      ip,
      endpoint,
      statusCode,
      message,
      payload: typeof payload === 'object' ? JSON.stringify(payload) : String(payload),
      timestamp: new Date().toISOString()
    };
    this.logs.unshift(entry);
    if (this.logs.length > 500) {
      this.logs = this.logs.slice(0, 500); // Keep last 500 logs
    }
    this.saveDatabase();
    console.log(`[LICENSING LOG] [${entry.timestamp}] [${ip}] [${endpoint}] [Status: ${statusCode}] - ${message}`);
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
      this.addLog(clientIp, '/api/v1/serial/validate', 403, 'Signature verification failed (Potential tampering)', body);
      throw new UnauthorizedException('🛡️ فشل تأكيد أمان الطلب أو التوقيع الرقمي غير متطابق!');
    }

    // Input sanitization against injection
    if (!/^[A-Z0-9_-]{4,60}$/i.test(serial) || !/^[A-Z0-9_-]{4,40}$/i.test(deviceId)) {
      this.addLog(clientIp, '/api/v1/serial/validate', 400, 'Potential injection block', { serial, deviceId });
      throw new BadRequestException('بيانات الإدخال تحتوي على رموز غير مسموح بها!');
    }

    const serialRecord = this.serials.find(s => s.serial_key.toUpperCase() === serial.toUpperCase());

    if (!serialRecord) {
      this.addLog(clientIp, '/api/v1/serial/validate', 404, `Activation failed: Serial ${serial} not found.`);
      return {
        success: false,
        status: 'NOT_FOUND',
        message: 'السيريال غير صحيح! يرجى التواصل مع الدعم لطلب مفتاح تنشيط.'
      };
    }

    if (serialRecord.status === 'REVOKED') {
      this.addLog(clientIp, '/api/v1/serial/validate', 403, `Activation rejected: Serial ${serial} is REVOKED.`);
      return {
        success: false,
        status: 'REVOKED',
        message: 'تم إلغاء أو تجميد هذا السيريال من لوحة التحكم! يرجى مراجعة الدعم.'
      };
    }

    const now = new Date();
    const endDate = new Date(serialRecord.end_date);
    if (now > endDate || serialRecord.status === 'EXPIRED') {
      serialRecord.status = 'EXPIRED';
      this.saveDatabase();
      this.addLog(clientIp, '/api/v1/serial/validate', 403, `Activation rejected: Serial ${serial} is EXPIRED.`);
      return {
        success: false,
        status: 'EXPIRED',
        message: 'هذا الترخيص منتهي الصلاحية! يرجى تجديد الاشتراك.'
      };
    }

    // First time activation & device-lock (Hardware locking)
    if (!serialRecord.device_id) {
      serialRecord.device_id = deviceId;
      serialRecord.status = 'ACTIVE';
      this.saveDatabase();
      this.addLog(clientIp, '/api/v1/serial/validate', 200, `Successfully bound device ${deviceId} to serial ${serial}`);
      
      const token = jwt.sign({ serial, deviceId }, this.JWT_SECRET, { expiresIn: '365d' });
      return {
        success: true,
        status: 'ACTIVE',
        message: '🔓 تم تفعيل وترخيص التطبيق وربطه بجهازك بنجاح للمرة الأولى!',
        token
      };
    }

    // Verify same device to prevent multi-device sharing
    if (serialRecord.device_id !== deviceId) {
      this.addLog(clientIp, '/api/v1/serial/validate', 403, `Device mismatch! Registered: ${serialRecord.device_id}, Requesting: ${deviceId}`);
      return {
        success: false,
        status: 'REVOKED',
        message: '❌ فشل التفعيل! هذا الترخيص مخصص لجهاز هاتف آخر ومقفل أمنياً ضد الاستنساخ والمشاركة.'
      };
    }

    // Re-verify existing device
    serialRecord.status = 'ACTIVE';
    this.saveDatabase();
    this.addLog(clientIp, '/api/v1/serial/validate', 200, `Re-verified active serial ${serial} for device ${deviceId}`);
    
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
    const total = this.serials.length;
    const active = this.serials.filter(s => s.status === 'ACTIVE').length;
    const unused = this.serials.filter(s => s.status === 'UNUSED').length;
    const revoked = this.serials.filter(s => s.status === 'REVOKED').length;
    return { total, active, unused, revoked, logsCount: this.logs.length };
  }

  async getAllSerials() {
    return this.serials.map(serial => {
      const client = this.clients.find(c => c.id === serial.client_id);
      return {
        ...serial,
        client_name: client ? client.name : 'Unknown',
        client_phone: client ? client.phone : '',
        client_network: client ? client.network_name : ''
      };
    });
  }

  async createSerial(body: any) {
    const { name, network_name, phone, duration_months, notes } = body;

    if (!name || !network_name || !phone) {
      throw new BadRequestException('يرجى تعبئة الحقول الإلزامية الاسم، الشبكة، والهاتف!');
    }

    // 1. Create client
    const clientId = this.clients.length + 1;
    const newClient: Client = {
      id: clientId,
      name,
      network_name,
      phone,
      notes: notes || ''
    };
    this.clients.push(newClient);

    // 2. Generate online/offline verification hash from phone + salt
    const rawData = phone.trim().toUpperCase() + this.OFFLINE_SALT;
    const hash = crypto.createHash('sha256').update(rawData).digest('hex').substring(0, 6).toUpperCase();
    const finalSerialKey = `${phone.trim().toUpperCase()}-KS${hash}`;

    // Prevent duplicate serial key
    if (this.serials.some(s => s.serial_key === finalSerialKey)) {
      throw new BadRequestException('السيريال الخاص بهذا الهاتف تم توليده مسبقاً!');
    }

    // 3. Set duration dates
    const start = new Date();
    const end = new Date();
    const months = parseInt(duration_months || 12, 10);
    end.setMonth(end.getMonth() + months);

    const newSerial: Serial = {
      id: this.serials.length + 1,
      client_id: clientId,
      serial_key: finalSerialKey,
      device_id: null, // Open for first device to bind
      duration_months: months,
      start_date: start.toISOString().split('T')[0],
      end_date: end.toISOString().split('T')[0],
      status: 'UNUSED',
      notes: notes || ''
    };
    this.serials.push(newSerial);
    this.saveDatabase();

    return { success: true, serial: newSerial };
  }

  async resetDeviceLock(id: number) {
    const serial = this.serials.find(s => s.id === id);
    if (!serial) {
      throw new NotFoundException('السيريال غير موجود!');
    }

    serial.device_id = null;
    serial.status = 'UNUSED';
    this.saveDatabase();
    return { success: true, message: 'تم إلغاء قفل الجهاز بنجاح! السيريال جاهز للربط بهاتف جديد.' };
  }

  async toggleSerialStatus(id: number) {
    const serial = this.serials.find(s => s.id === id);
    if (!serial) {
      throw new NotFoundException('السيريال غير موجود!');
    }

    if (serial.status === 'REVOKED') {
      serial.status = serial.device_id ? 'ACTIVE' : 'UNUSED';
      this.saveDatabase();
      return { success: true, message: 'تمت إعادة تفعيل الترخيص بنجاح!', status: serial.status };
    } else {
      serial.status = 'REVOKED';
      this.saveDatabase();
      return { success: true, message: 'تم إلغاء وتجميد ترخيص هذا السيريال بنجاح!', status: 'REVOKED' };
    }
  }

  async deleteSerial(id: number) {
    const index = this.serials.findIndex(s => s.id === id);
    if (index === -1) {
      throw new NotFoundException('السيريال غير موجود!');
    }

    this.serials.splice(index, 1);
    this.saveDatabase();
    return { success: true, message: 'تم حذف السيريال والعميل نهائياً من النظام!' };
  }

  async getLogs() {
    return this.logs;
  }
}
