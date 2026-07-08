# Kurotek Platform - Railway Deployment Guide

## Prerequisites
1. A GitHub account with the project repository.
2. A Railway.app account.

## Step 1: Create the Project on Railway
1. Log in to [Railway](https://railway.app/).
2. Click **New Project** > **Deploy from GitHub repo**.
3. Select the `Kayansoft` repository.
4. Railway will automatically detect the Monorepo. Do NOT deploy yet; close the initial setup and go to your project dashboard.

## Step 2: Provision PostgreSQL Database
1. Inside your Railway Project, click **New** > **Database** > **Add PostgreSQL**.
2. Wait for the database to provision. Railway automatically injects the `DATABASE_URL` environment variable into your services.

## Step 3: Configure Backend Service
1. Click **New** > **GitHub Repo** and select the repository again.
2. Go to the new service settings -> **General**.
3. Set the **Root Directory** to `/kurotek-backend`.
4. Railway will detect `railway.json` automatically, which configures `npx prisma generate`, migrations, and the start command.
5. Go to **Variables** and add:
   - `NODE_ENV=production`
   - `JWT_SECRET=your_super_secret_key`
   - `JWT_EXPIRES_IN=7d`
   - `CORS_ORIGIN=https://kurotek-admin-production.up.railway.app`
6. Go to **Networking** and click **Generate Domain** (e.g., `kurotek-backend-production.up.railway.app`).

## Step 4: Configure Frontend Service (Next.js)
1. Click **New** > **GitHub Repo** and select the repository once more.
2. Go to Settings -> **General**. Leave the Root Directory as `/` (default).
3. Railway will read the root `railway.json` and build Next.js.
4. Go to **Variables** and add:
   - `NEXT_PUBLIC_API_URL=https://kurotek-backend-production.up.railway.app`
   - `NEXT_PUBLIC_WS_URL=wss://kurotek-backend-production.up.railway.app/ws`
5. Go to **Networking** and click **Generate Domain** (e.g., `kurotek-admin-production.up.railway.app`).

## Step 5: Android Build
1. Make sure your Android WebSocket URL in `SyncManager.kt` matches the Railway backend WebSocket domain.
2. Build the signed APK/AAB in Android Studio via `Build > Generate Signed Bundle / APK`.
3. Distribute the app to your agents.

## Health Checks & Monitoring
- Backend Health Check is available at `https://kurotek-backend-production.up.railway.app/health`.
- Railway provides integrated Logging and Metrics on the service dashboard.
