# KayanSoft — Kurotek SaaS Platform

> **نظام إدارة شبكات المبيعات والتوزيع السحابي**

## 🏗️ هيكل المشروع

```
├── backend/        # NestJS REST API + WebSocket (Render)
├── admin/          # Next.js Admin Dashboard (Render)
├── mobile/         # Android Native App (Kotlin/Compose)
├── docs/           # وثائق المشروع
└── .github/        # CI/CD Workflows
```

## 🚀 النشر السحابي

| الخدمة | المنصة | الحالة |
|--------|--------|--------|
| Backend API | Render (`kayan-uzs5.onrender.com`) | ✅ Live |
| Admin Dashboard | Render | ⏳ |
| Android App | GitHub Actions → APK | ⏳ |
| Database | Railway PostgreSQL | ✅ Connected |

## 🛠️ تشغيل محلي

### Backend
```bash
cd backend
npm install
npx prisma generate
npm run start:dev
```

### Admin Dashboard  
```bash
cd admin
pnpm install
pnpm run dev
```

### Android
```bash
cd mobile
./gradlew assembleDebug
```