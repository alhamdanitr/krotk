# وثيقة التصميم الهندسي (Solution Architecture Document)
**المشروع:** كروت الدحشة (Dahsha Cards) - Enterprise Edition  
**الإصدار:** 2.0 (التصميم المعماري للنظام الجديد)  

---

## 1. نظرة عامة على المعمارية (Architecture Overview)
تم تصميم النظام الجديد ليتحول من "تطبيق محلي على هاتف واحد" إلى **نظام سحابي مركزي (Cloud-Based Centralized System)**. 
- **المركزية (Backend):** سيقوم الخادم السحابي بإدارة جميع العمليات المعقدة: التخزين، تحليل رسائل SMS، إدارة الكروت، وتوفير لوحة تحكم للإدارة.
- **الطرفيات (Frontend/Mobile):** سيعمل تطبيق Flutter كلوحة تحكم للمدير (Dashboard) من أي مكان، وفي نفس الوقت سيحتوي الهاتف الذي يمتلك شريحة الاتصال (SIM) على خدمة خلفية لالتقاط رسائل الإيداع وتمريرها للخادم، ثم استقبال أمر من الخادم بإرسال رسالة نصية (SMS) للعميل.

---

## 2. اختيار التقنيات (Tech Stack Justification)
### Backend
- **Node.js + NestJS:** إطار عمل مؤسسي مبني على TypeScript يضمن كتابة كود نظيف وقابل للتوسع بناءً على (Modular Architecture) ومناسب جداً للأنظمة المالية لتنظيمه العالي.
- **PostgreSQL:** قاعدة بيانات علائقية قوية، موثوقة جداً، وتدعم المعاملات المالية (ACID Compliance) لضمان عدم ضياع أي بيانات.
- **Prisma ORM:** أفضل أداة للتعامل مع قواعد البيانات في TypeScript توفر Type-Safety وتجعل تعديل الجداول (Migrations) آمناً وسريعاً.
- **Railway / Docker:** لنشر النظام بسهولة وكفاءة مع توفير بيئة معزولة (Containerized) تضمن عمل الكود بنفس الكفاءة في الإنتاج.
- **JWT:** لحماية الـ APIs وتوثيق دخول المستخدمين (Admins) بشكل آمن (Stateless).

### Frontend (Mobile App)
- **Flutter:** لتطوير تطبيق ذو أداء عالي وسلس لواجهات الإدارة ويعمل على Android/iOS بكود واحد.
- **Clean Architecture + Feature First:** لتقسيم الكود بحيث يكون كل قسم مستقلاً بذاته، مما يسهل الصيانة وإضافة الميزات.
- **Riverpod:** أفضل وأحدث أداة لإدارة الحالة (State Management) في Flutter، تدعم الـ Dependency Injection بأمان وتدعم الـ Caching.
- **Dio:** مكتبة قوية للتعامل مع الـ HTTP Requests تدعم الـ Interceptors (لإضافة الـ Token التلقائي).
- **GoRouter:** الأداة الرسمية والمدعومة للتنقل بين الشاشات، تدعم الـ Deep Linking والـ Auth Guards (حماية شاشات الإدارة).

---

## 3. هيكل مشروع Backend بالكامل (NestJS Structure)
```text
src/
 ├── app.module.ts
 ├── prisma/                 # إعدادات Prisma وخدمات قاعدة البيانات
 ├── common/                 # الأشياء المشتركة (Guards, Interceptors, Filters, Utils)
 ├── core/                   # الإعدادات الأساسية (Config, Logger)
 ├── modules/                # Modules المشروع (Feature Based)
 │    ├── auth/              # تسجيل الدخول وتوليد الـ JWT
 │    ├── users/             # إدارة المدراء
 │    ├── cards/             # إدارة الكروت والمخزون
 │    ├── deposits/          # إدارة الإيداعات الواردة
 │    ├── transactions/      # العمليات الناجحة
 │    ├── pending/           # الطلبات المعلقة (المراجعة اليدوية)
 │    ├── customers/         # مطابقة حسابات العملاء
 │    ├── sms/               # استقبال، تحليل الـ SMS واتخاذ القرار
 │    └── wallets/           # إدارة المحافظ وأنماطها (Regex)
```

---

## 4. هيكل مشروع Flutter بالكامل (Clean Architecture + Feature First)
```text
lib/
 ├── main.dart
 ├── core/                   # الأكواد المشتركة في التطبيق
 │    ├── network/           # إعدادات Dio و API Endpoints
 │    ├── theme/             # Material 3 Colors, Typography
 │    ├── router/            # GoRouter Configuration
 │    ├── errors/            # Failure models & Exceptions
 │    └── utils/             # Helpers (Date formatter, constants)
 ├── features/               # الميزات مقسمة
 │    ├── auth/
 │    ├── dashboard/         # الإحصائيات والواجهة الرئيسية
 │    ├── deposits/          # قائمة الإيداعات
 │    ├── cards/             # المخزون وإضافة الكروت
 │    ├── pending/           # واجهة الموافقة/الرفض
 │    ├── customers/         # إدارة مطابقة الأرقام
 │    └── sms_service/       # خدمة قراءة الـ SMS وإرسالها
 │         ├── data/         # Repositories & Data Sources
 │         ├── domain/       # Entities & UseCases
 │         └── presentation/ # Riverpod Providers & Widgets
```

---

## 5. تصميم قاعدة البيانات (Database Schema - Prisma)

1. **User (المدراء):**
   - `id`, `username`, `password` (Hashed), `role`, `createdAt`
2. **Card (الكروت):**
   - `id`, `category` (Int), `code` (String), `username` (String?), `password` (String?), `status` (Enum: AVAILABLE, USED), `createdAt`
3. **WalletConfig (المحافظ):**
   - `id`, `name`, `regexPattern`, `isActive`, `createdAt` *(ميزة جديدة: أنماط التحليل ديناميكية في السيرفر وليست ثابتة في التطبيق)*
4. **CustomerMapping (العملاء):**
   - `id`, `uniqueId`, `phone`, `name`, `walletId`, `createdAt`
5. **Deposit (الإيداعات):**
   - `id`, `amount`, `senderId` (String), `walletId`, `status` (Enum: PENDING, SHARED, REJECTED), `cardDetails`, `createdAt`
6. **Transaction (المعاملات):**
   - `id`, `depositId` (FK), `amount`, `phone`, `walletId`, `cardCode`, `createdAt`

---

## 6. تصميم REST APIs الأساسية

**Auth & Users:**
- `POST /api/auth/login`: تسجيل الدخول.

**Cards:**
- `GET /api/cards/stock`: جرد الكروت المتوفرة حسب الفئات.
- `POST /api/cards/bulk`: رفع مجموعة كروت جديدة.
- `DELETE /api/cards/:id`: حذف كارت.

**SMS Process (أهم API للخدمة الخلفية):**
- `POST /api/sms/process`: يستقبل الهاتف رسالة الـ SMS ويرسلها هنا. الخادم يحللها، يوزع الكارت، ويرد على التطبيق بـ:
  - `{ action: "SEND_SMS", phone: "777...", message: "كود الكارت..." }`
  - أو `{ action: "PENDING", message: "تم التعليق للمراجعة" }`

**Deposits & Pending:**
- `GET /api/deposits`: جلب جميع الإيداعات.
- `GET /api/pending`: جلب الطلبات المعلقة.
- `POST /api/pending/:id/approve`: الموافقة اليدوية (تقوم بتجهيز الكارت ورده للتطبيق لإرساله).
- `POST /api/pending/:id/reject`: الرفض اليدوي.

**Customers & Wallets:**
- `GET/POST /api/customers`
- `GET/PUT /api/wallets`

---

## 7. تدفق البيانات (Data Flow System)

**سيناريو استلام رسالة أوتوماتيكية:**
1. يستقبل تطبيق Flutter في (الهاتف الخادم) رسالة SMS بالخلفية.
2. يرسل التطبيق محتوى الرسالة عبر `POST /api/sms/process` للـ Backend.
3. يقوم الـ Backend بمطابقة الرسالة مع الـ Regex الخاص بالمحافظ (من الداتا بيز).
4. يجد الـ Backend المبلغ ورقم العميل، يتحقق من `CustomerMapping` إذا لزم الأمر.
5. يسحب كارت متوفر من جدول `Card` ويحدث حالته إلى `USED`.
6. يسجل العملية في `Deposit` و `Transaction`.
7. يرد الـ Backend استجابة سريعة لـ Flutter: قم بإرسال SMS للرقم X بالنص Y.
8. يقوم تطبيق Flutter بإرسال الرسالة باستخدام شريحة الجوال (لتقليل تكاليف الإرسال).

**النتيجة:** الهاتف المحمول أصبح مجرد "جسر تواصل"، والبيانات كلها مخزنة ومحللة بأمان في الخادم المركزي.

---

## 8. خطة التنفيذ (Implementation Plan)

- **المرحلة 1 (الأساسيات والـ Backend):** 
  - إعداد بيئة NestJS و PostgreSQL و Prisma.
  - تصميم الجداول (Migrations) ونظام تسجيل الدخول (JWT).
- **المرحلة 2 (نظام التحليل الأساسي):**
  - بناء Module الـ SMS والمحافظ (تطوير محلل الـ Regex المركزي).
  - بناء الـ CRUD للكروت والإيداعات.
- **المرحلة 3 (واجهات الإدارة - Flutter):**
  - إعداد هيكل Flutter (Clean Architecture).
  - بناء واجهات تسجيل الدخول، لوحة التحكم، إدارة الكروت، وإدارة العملاء.
- **المرحلة 4 (ربط خدمة الـ SMS في Flutter):**
  - إعداد خدمة التقاط الـ SMS (Background Service) في أندرويد عبر Flutter.
  - ربطها مع API الخادم وتطبيق الرد التلقائي.
- **المرحلة 5 (الاختبار والنشر - Production):**
  - اختبار توافق النظام بالكامل.
  - النشر على Railway ورفع تطبيق الأندرويد للاستخدام.

---

## 9. معايير الأمان، الأداء، وقابلية التوسع (Security & Performance)
- **الأمان:** تشفير كلمات المرور (Bcrypt)، استخدام (JWT) مع وقت انتهاء، التحقق من صحة البيانات باستخدام (class-validator) في NestJS. مفاتيح Gemini (إن وجدت) تخزن حصراً في بيئة الخادم (`.env`) ولن تكون مكشوفة في الكود أبداً.
- **الأداء:** استخدام Pagination في جميع الـ APIs لتسريع جلب البيانات، وعمل Indexing في PostgreSQL لحقول (رقم الهاتف، حالة الكارت) لتسريع البحث.
- **قابلية التوسع:** قواعد الـ Regex للمحافظ لم تعد مشفرة (Hardcoded) في التطبيق بل في الـ Database، مما يعني إمكانية إضافة محفظة جديدة أو تغيير صيغة الرسالة من لوحة التحكم دون تحديث التطبيق.

---

## 10. أفضل الممارسات التي سيتم الالتزام بها (Best Practices)
1. **SOLID Principles & DRY:** كود غير متكرر وقابل للاختبار.
2. **Git Workflow:** التزام برسائل (Conventional Commits).
3. **No Any:** تجنب استخدام `any` في TypeScript واستخدام Types صريحة.
4. **Standard API Response:** توحيد شكل الاستجابة من الخادم:
   `{ success: boolean, data: any, message: string }`
5. **Micro Tasks:** سيتم تنفيذ المشروع على شكل مهام مصغرة لضمان المراجعة السليمة.

---
**تم إعداد وثيقة التصميم الهندسي (SAD). النظام الآن جاهز للتنفيذ بأعلى معايير الـ Production Ready. يرجى المراجعة والموافقة لنبدأ فوراً بـ (المرحلة 1: بناء Backend الأساسي وقاعدة البيانات PostgreSQL).**
