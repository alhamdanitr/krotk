# Backend — الخلفية الرسمية (Kurotek SaaS API)

## 1. الغرض من المشروع

باك-إند NestJS + PostgreSQL (عبر Prisma) يمثل المرجع المعماري الأنضج بالمشروع بالكامل. يطبّق نظام **SaaS متعدد المستأجرين (Multi-Tenant)**: كل موزع/عميل هو "Tenant" منفصل له اشتراكه وبياناته الخاصة، بدل النموذج المسطّح البسيط المستخدم بـ[`server`](../server). يغطي: مصادقة/تفعيل، محرك تحليل SMS معياري، محاسبة موزع كاملة، وإدارة سيريالات قديمة (Legacy) لدعم توافق [`serial-admin`](../serial-admin).

## 2. الأدوات والتقنيات المستخدمة

| الفئة | الأداة |
|---|---|
| إطار العمل | NestJS 11 (TypeScript) |
| قاعدة البيانات | PostgreSQL عبر Prisma ORM |
| المصادقة | Passport + JWT (`@nestjs/jwt`, `passport-jwt`) |
| التواصل الفوري | WebSocket (`@nestjs/websockets` + Socket.IO) |
| التوثيق التفاعلي | Swagger (`/api/docs`) |
| الحماية من الإغراق | `@nestjs/throttler` |
| إدارة الحزم | pnpm |

## 3. هيكلة الملفات (حسب الموديول)

```
backend/
├── prisma/schema.prisma      ← 23 نموذج: نظام SaaS (Tenant, Plan, ActivationKey, Subscription...)
│                                + محاسبة الموزع (DistributorCustomer/Sale/Payment/Expense/Capital)
│                                + نظام السيريالات القديم (AdminClient/AdminSerial/AdminSecurityLog)
├── src/
│   ├── main.ts                 نقطة الدخول (CORS, ValidationPipe, Swagger)
│   ├── app.module.ts             تسجيل كل الموديولات + الحُرّاس العامة (Guards) + HmacMiddleware
│   ├── auth/                     تسجيل الدخول، تفعيل المستأجرين، استراتيجية JWT
│   ├── common/
│   │   ├── decorators/            @Public(), @Roles(), @CurrentUser()
│   │   ├── guards/                JwtAuthGuard, RolesGuard, TenantGuard, TrialGuard, SubscriptionGuard
│   │   ├── middleware/             HmacMiddleware (توقيع الطلبات على كل المسارات ما عدا المستثناة)
│   │   └── interfaces/             عقود قابلة للتوسع: card-allocator, sms-parser, report-generator...
│   ├── sms-engine/                محرك تحليل SMS — parser مستقل لكل محفظة (جيب/جوالي/الكريمي/حسب/
│   │                                ون كاش/أم فلوس) عبر parsers/*.parser.ts، كلها تنفّذ نفس الواجهة
│   ├── card-engine/               تخصيص الكروت للعملاء
│   ├── distributor/               customers/, sales/, finance/, pricing/ — محاسبة الموزع الكاملة
│   ├── serial-manager/            نظام السيريالات القديم المتوافق مع server/api_server.js
│   │                                (نفس شكل الاستجابة تمامًا، لدعم serial-admin وتطبيقات الكروت)
│   ├── inventory/, wallets/, reports/, dashboard/, notifications/, backups/, deposits/,
│   │   transactions/, cards/       موديولات مساندة لكل جوانب النظام
│   ├── events/events.gateway.ts   قناة WebSocket للتحديثات الفورية
│   └── parser-registry/            سجل مركزي يربط كل محفظة بمُحلِّلها المناسب
└── test/                          اختبارات e2e
```

## 4. طريقة سير العمل (Workflow)

### مسار تفعيل تطبيق الكروت (نظام السيريالات القديم، `serial-manager`)
`POST /api/v1/serial/validate` (عام، لكن موقّع بـHMAC) → يتحقق من وجود السيريال، حالته (نشط/موقوف/منتهي)، ويربطه بمعرّف الجهاز عند أول استخدام (Device Locking) → يرجّع JWT صالح لسنة.

### مسار إدارة السيريالات (من تطبيق `serial-admin`)
`POST /api/v1/admin/login` (بالموديول `auth`، عام) → توكن JWT بدور `super_admin` → باقي مسارات `/api/v1/admin/serial/*` محمية بـ`JwtAuthGuard` + `@Roles('super_admin')`.

### مسار محرك SMS
رسالة SMS خام تصل لـ`sms-engine.controller.ts` → `parser-registry` يحدد أي parser يطابق نمط المرسل → الـparser المختص يستخرج (المبلغ، المرسل، رقم العملية) → `card-engine` يخصّص كرتًا مناسبًا.

### طبقات الحماية العامة (مُطبَّقة بترتيب محدد على كل طلب)
```
HmacMiddleware (توقيع الطلب) → ThrottlerGuard (حد المعدل) → JwtAuthGuard (تسجيل الدخول،
يتجاوزه @Public()) → TenantGuard → TrialGuard → SubscriptionGuard → RolesGuard (@Roles())
```
⚠️ الـMiddleware يعمل **قبل** كل الحرّاس، فأي مسار جديد يُضاف يجب استثناؤه صراحة من `HmacMiddleware`
بـ`app.module.ts` إذا كان يُفترض أن يكون عامًا بالكامل (وإلا سيتطلب توقيع HMAC رغم `@Public()`).

## 5. طريقة التشغيل

```bash
cd backend
cp .env.example .env    # واملأ القيم الحقيقية (JWT_SECRET, HMAC_SECRET, DATABASE_URL...)
npm install --include=dev
npx prisma generate --schema=prisma/schema.prisma
npx prisma migrate deploy --schema=prisma/schema.prisma
npm run start:dev
```
التوثيق التفاعلي (Swagger) متاح على `http://localhost:3000/api/docs` بعد التشغيل.

### النشر (Render)
مُهيّأ بالفعل عبر خدمة Web Service منفصلة على Render، مربوطة بـ`Root Directory: backend`. راجع متغيرات البيئة المطلوبة بـ`.env.example` — السيرفر **يرفض الإقلاع** إن لم تُضبط جميعها (`JWT_SECRET`, `HMAC_SECRET`, `SERIAL_OFFLINE_SALT`, `DATABASE_URL`, `ADMIN_USERNAME`, `ADMIN_PASSWORD`).
