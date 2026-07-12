# Kurotek Platform - Backup & Disaster Recovery Guide

## 1. Automated Backups
The platform is configured to perform automated database backups based on settings configured inside the Admin Dashboard.
- Files are saved as `.sql` (Custom format) inside the `/backups` directory on the server.
- The `/backups` directory is mounted via Docker volumes so that backups survive container restarts.

## 2. Manual Backup Creation
System Administrators can take a manual snapshot before major updates:
1. Login to the Kurotek Admin Dashboard.
2. Navigate to **الإعدادات > النسخ الاحتياطي** (Settings > Backups).
3. Click "أخذ نسخة احتياطية الآن" (Create Backup Now).

## 3. Database Restoration
If data corruption occurs, follow these steps to restore:
1. **Via Dashboard:** Navigate to Settings > Backups and click **استعادة** (Restore) next to the desired backup file.
2. **Via CLI (If Dashboard is down):**
   ```bash
   docker exec -it kurotek-backend pg_restore -U kurotek_user -d kurotek_db -c -F c /app/backups/backup-filename.sql
   ```

## 4. Disaster Recovery (Total Server Loss)
To recover from a total VPS failure:
1. Ensure you have off-site copies of your `.env.production` file and the latest `.sql` file from `/backups`.
2. Provision a new VPS and execute the Deployment Guide.
3. Place the `.sql` backup file into the `/backups` folder on the new VPS.
4. Execute the DB Restoration CLI command.
5. Import your `kurotek_settings.json` via the Dashboard.
