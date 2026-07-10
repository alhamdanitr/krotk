# Kurotek Platform - Deployment Guide

## Prerequisites
1. A Linux VPS (Ubuntu 22.04 LTS recommended).
2. Docker and Docker Compose installed.
3. Domain names mapped to the VPS IP (`api.yourdomain.com`, `dashboard.yourdomain.com`).
4. SSL Certificates generated (e.g., via Let's Encrypt).

## Deployment Steps

### Step 1: Clone Repository & Setup Environments
1. Clone the master branch.
2. Rename `.env.production` to `.env` in the root folder.
3. Edit the `.env` file to replace placeholder values with actual strong passwords and keys.

### Step 2: SSL Certificates
1. Obtain certificates: `certbot certonly --standalone -d api.yourdomain.com -d dashboard.yourdomain.com`
2. Place the generated certificates (`fullchain.pem`, `privkey.pem`) into the `cert/` directory in the project root.

### Step 3: Build & Launch Docker Images
Run the following command in the project root:
```bash
docker-compose -f docker-compose.production.yml up -d --build
```
This will build the NestJS backend, Next.js frontend, configure PostgreSQL, and start Nginx as a reverse proxy.

### Step 4: Run Prisma Migrations
Once the containers are running, execute the DB schema deployment:
```bash
docker exec -it <backend_container_name> npx prisma migrate deploy
```

### Step 5: Verify Connectivity
- Visit `https://dashboard.yourdomain.com` and ensure the UI loads.
- Ensure WebSocket connection connects without SSL/WSS errors.
