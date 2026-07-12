import { Injectable, Logger, InternalServerErrorException, NotFoundException } from '@nestjs/common';
import { exec } from 'child_process';
import { promisify } from 'util';
import * as fs from 'fs';
import * as path from 'path';

const execAsync = promisify(exec);

@Injectable()
export class BackupsService {
  private readonly logger = new Logger(BackupsService.name);
  private readonly backupDir = path.join(process.cwd(), 'backups');

  constructor() {
    if (!fs.existsSync(this.backupDir)) {
      fs.mkdirSync(this.backupDir, { recursive: true });
    }
  }

  // Returns database connection details based on the DATABASE_URL environment variable.
  // Warning: This is a simplistic parser. In production, ensure DATABASE_URL format is strictly handled.
  private getDbConfig() {
    const dbUrl = process.env.DATABASE_URL;
    if (!dbUrl) throw new InternalServerErrorException('DATABASE_URL is not set');

    // postgres://user:password@host:port/database
    const regex = /postgresql?:\/\/(.+):(.+)@(.+):(\d+)\/(.+)(\?.*)?/;
    const match = dbUrl.match(regex);
    
    if (!match) {
      // Return a raw URL config for environments where parsing fails, relying on pg_dump to parse the full URI
      return { url: dbUrl };
    }

    return {
      user: match[1],
      password: match[2],
      host: match[3],
      port: match[4],
      database: match[5],
      url: dbUrl,
    };
  }

  async createBackup(): Promise<{ message: string; filename: string; size: number }> {
    const timestamp = new Date().toISOString().replace(/[:.]/g, '-');
    const filename = `backup-${timestamp}.sql`;
    const filepath = path.join(this.backupDir, filename);

    const dbConfig = this.getDbConfig();

    try {
      this.logger.log(`Creating database backup: ${filename}`);
      
      // We use the full connection string format for pg_dump to bypass manual password prompts
      const command = `pg_dump "${dbConfig.url}" -F c -f "${filepath}"`;
      
      await execAsync(command);
      
      const stats = fs.statSync(filepath);
      this.logger.log(`Backup created successfully: ${filename} (${stats.size} bytes)`);

      return {
        message: 'تم إنشاء النسخة الاحتياطية بنجاح',
        filename,
        size: stats.size,
      };
    } catch (error) {
      this.logger.error('Failed to create backup', error.stack);
      throw new InternalServerErrorException('حدث خطأ أثناء إنشاء النسخة الاحتياطية');
    }
  }

  async restoreBackup(filename: string): Promise<{ message: string }> {
    const filepath = path.join(this.backupDir, filename);
    if (!fs.existsSync(filepath)) {
      throw new NotFoundException('النسخة الاحتياطية غير موجودة');
    }

    const dbConfig = this.getDbConfig();

    try {
      this.logger.log(`Restoring database from backup: ${filename}`);
      
      // pg_restore uses the custom format (-F c) and drops existing objects (-c) before recreating
      const command = `pg_restore "${dbConfig.url}" -c -F c -d "${dbConfig.database || dbConfig.url}" "${filepath}"`;
      
      await execAsync(command);
      this.logger.log(`Database restored successfully from: ${filename}`);

      return { message: 'تم استعادة قاعدة البيانات بنجاح' };
    } catch (error) {
      this.logger.error('Failed to restore backup', error.stack);
      throw new InternalServerErrorException('حدث خطأ أثناء استعادة النسخة الاحتياطية. يرجى التأكد من توافق ملف النسخة.');
    }
  }

  async listBackups() {
    try {
      const files = fs.readdirSync(this.backupDir);
      const backups = files
        .filter((file) => file.endsWith('.sql'))
        .map((file) => {
          const stats = fs.statSync(path.join(this.backupDir, file));
          return {
            filename: file,
            size: stats.size,
            createdAt: stats.birthtime,
          };
        })
        .sort((a, b) => b.createdAt.getTime() - a.createdAt.getTime());

      return backups;
    } catch (error) {
      this.logger.error('Failed to list backups', error.stack);
      throw new InternalServerErrorException('حدث خطأ أثناء جلب قائمة النسخ الاحتياطية');
    }
  }

  async deleteBackup(filename: string): Promise<{ message: string }> {
    const filepath = path.join(this.backupDir, filename);
    if (!fs.existsSync(filepath)) {
      throw new NotFoundException('النسخة الاحتياطية غير موجودة');
    }

    try {
      fs.unlinkSync(filepath);
      this.logger.log(`Backup deleted: ${filename}`);
      return { message: 'تم حذف النسخة الاحتياطية بنجاح' };
    } catch (error) {
      this.logger.error('Failed to delete backup', error.stack);
      throw new InternalServerErrorException('حدث خطأ أثناء حذف النسخة الاحتياطية');
    }
  }

  // Exports dummy settings (for demonstration; could map to a real settings table later)
  async exportSettings() {
    return {
      appName: 'Kurotek Platform',
      version: '1.0.0',
      autoBackup: true,
      backupFrequency: 'daily',
    };
  }

  // Imports dummy settings
  async importSettings(settings: any) {
    this.logger.log('Imported settings', settings);
    return { message: 'تم استيراد الإعدادات بنجاح' };
  }
}
