# Server — سيرفر التراخيص القديم (Express)

## 1. الغرض من المشروع

سيرفر Node.js/Express بسيط، يمثل **أول** نظام تراخيص للمشروع. مسؤول عن: تفعيل/التحقق من السيريالات لتطبيقات الكروت، ولوحة إدارة HTML بسيطة (`admin_panel.html`) لإنشاء/إدارة السيريالات يدويًا. هذا هو السيرفر الحي حاليًا على `https://kayan-licensing-server.onrender.com` الذي تتصل به [`cards-app`](../cards-app) و[`mobile-app`](../mobile-app) فعليًا.

> يوجد نظام مواز أنضج معماريًا بـ[`backend`](../backend) (NestJS) يطبّق نفس هذه الوظائف (موديول `serial-manager`) بشكل متوافق تمامًا من ناحية الـ API، بالإضافة لنظام SaaS متعدد المستأجرين الأشمل. القرار بشأن توحيدهما لسه معلّق (راجع القسم 6).

## 2. الأدوات والتقنيات المستخدمة

| الفئة | الأداة |
|---|---|
| إطار العمل | Express.js |
| قاعدة البيانات | PostgreSQL (عبر `pg`) — أو وضع "in-memory" مؤقت إن لم يوجد `DATABASE_URL` |
| المصادقة | JWT (`jsonwebtoken`) + توقيع HMAC يدوي للتحقق من طلبات التفعيل |
| الواجهة الإدارية | HTML/JS عادي بدون إطار عمل (`admin_panel.html`)، Basic Auth |
| الاتصال الفوري | WebSocket (`ws`) |

## 3. هيكلة الملفات

```
server/
├── .env.example              يوثّق المتغيرات المطلوبة (JWT_SECRET, HMAC_SECRET,
│                              ADMIN_USERNAME, ADMIN_PASSWORD, DATABASE_URL)
├── api_server.js              كل منطق السيرفر بملف واحد:
│                                - نقاط تفعيل السيريال (/api/v1/serial/validate)
│                                - نقاط إدارة (/api/v1/admin/*) محمية بـJWT
│                                - نقاط مزامنة (/api/v1/sync/upload|download|migrate)
│                                - تقديم admin_panel.html كصفحة ويب
├── admin_panel.html           لوحة إدارة بسيطة (تسجيل دخول + إنشاء/تعديل/حذف سيريالات)
├── database.sql                هيكلة الجداول (للاستخدام اليدوي إن لم تُستخدم migrations)
├── setup_db.js                 سكربت تهيئة أولي لقاعدة البيانات
└── test_backend.js / test_stress.js   سكربتات اختبار يدوية (تفترض تشغيل السيرفر محليًا على :3000)
```

## 4. طريقة سير العمل (Workflow)

1. **تفعيل تطبيق الكروت**: `POST /api/v1/serial/validate` — الطلب يجب أن يحمل توقيع HMAC صحيح (`verifyRequestSignature` middleware)، يتحقق من السيريال بقاعدة البيانات، يربطه بمعرّف الجهاز عند أول استخدام، ويرجّع JWT صالح لسنة.
2. **دخول المدير**: `POST /api/v1/admin/login` بـ`ADMIN_USERNAME`/`ADMIN_PASSWORD` (من متغيرات البيئة) → JWT بدور `super_admin`.
3. **إدارة السيريالات** (محمية بـ`authenticateAdmin` middleware يتحقق من JWT): إنشاء (`/serial/create`)، تفعيل/إيقاف (`/serial/toggle/:id`)، فك ربط الجهاز (`/serial/reset/:id`)، حذف (`/serial/delete/:id`).
4. **لوحة `admin_panel.html`**: صفحة واحدة تستهلك نفس نقاط الإدارة أعلاه، محمية أيضًا بـBasic Auth مستقل على مستوى الخادم (نفس بيانات `ADMIN_USERNAME`/`ADMIN_PASSWORD`).
5. **المزامنة**: نقاط `/api/v1/sync/*` تستقبل/ترسل نسخ من بيانات الكروت والعملاء بين التطبيقات المتصلة.

## 5. طريقة التشغيل

```bash
cd server
cp .env.example .env    # واملأ القيم الحقيقية
npm install             # يعتمد على package.json بجذر هذا المجلد إن وُجد، وإلا أنشئه:
                         # npm init -y && npm install express cors jsonwebtoken pg ws @nestjs/throttler
node api_server.js
```
يعمل افتراضيًا على المنفذ `3000` (أو `process.env.PORT`).

### النشر (Render)
هذا هو السيرفر المنشور فعليًا حاليًا على `kayan-licensing-server.onrender.com`. المتغيرات المطلوبة إجباريًا (السيرفر **يرفض الإقلاع** بدونها): `JWT_SECRET`, `HMAC_SECRET`, `ADMIN_USERNAME`, `ADMIN_PASSWORD`, وتلقائيًا `DATABASE_URL` إن رُبطت قاعدة بيانات Render.

## 6. القرار المعماري المعلّق
يوجد حاليًا **تطبيقان منفصلان لنفس الوظيفة**: هذا السيرفر (يعمل فعليًا) وموديول `serial-manager` بـ[`backend`](../backend) (نفس الشكل تمامًا، جاهز لكن غير مُستخدَم من تطبيقات الكروت بعد). القرارات الممكنة:
- **الإبقاء على الاثنين مؤقتًا** (الوضع الحالي) — هذا السيرفر يخدم تطبيقات الكروت، و`backend` يخدم `web-admin` ونظام SaaS الأشمل.
- **التوحيد الكامل**: تحويل `SecurityApiService.kt`/`CloudSyncEngine.kt` بتطبيقات الكروت للاتصال بـ`backend` بدل هذا السيرفر، ثم إيقاف هذا السيرفر نهائيًا.
