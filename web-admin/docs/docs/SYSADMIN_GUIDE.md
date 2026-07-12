# Kurotek Platform - System Administrator Guide

## Overview
This guide provides system administrators with the necessary instructions to manage, monitor, and troubleshoot the Kurotek Platform in a production environment.

## 1. System Architecture
The platform runs on a modern stack:
- **Proxy:** Nginx (Handles SSL, Gzip, Caching)
- **Backend:** Node.js (NestJS) running on port 3000
- **Frontend:** Node.js (Next.js) running on port 3001
- **Database:** PostgreSQL 15
- **Mobile Agent:** Android Native App

## 2. Managing Services via Docker
All services are orchestrated via Docker Compose.
- **Start all services:** `docker-compose -f docker-compose.production.yml up -d`
- **Stop all services:** `docker-compose -f docker-compose.production.yml down`
- **View Backend Logs:** `docker logs kurotek-backend -f`
- **Restart Nginx:** `docker restart kurotek-nginx`

## 3. Database Management
The PostgreSQL database is mapped to a persistent volume `pgdata`.
- **Access DB Console:** `docker exec -it kurotek-db psql -U kurotek_user -d kurotek_db`
- **Run Migrations:** `docker exec -it kurotek-backend npx prisma migrate deploy`

## 4. Monitoring & Health
- Nginx access logs are located at: `/var/log/nginx/access.log` inside the nginx container.
- Backend errors are automatically logged to the console output and handled by the Docker daemon.

## 5. Security Protocols
- Keep your `.env.production` file completely secure. Never commit it to version control.
- `JWT_SECRET` must be a complex, cryptographically secure string.
- Nginx handles strict rate limiting and XSS protection headers. Do not alter `nginx.conf` without security review.
