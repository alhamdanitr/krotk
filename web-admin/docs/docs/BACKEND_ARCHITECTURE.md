# BACKEND_ARCHITECTURE — معمارية الـ Backend (NestJS)

| الحقل | القيمة |
|------|--------|
| الوثيقة | معمارية الـ Backend |
| الحالة | REVIEW |
| المرجع | [SYSTEM_ARCHITECTURE.md](./SYSTEM_ARCHITECTURE.md) · [SDD.md](./SDD.md) · [FOLDER_STRUCTURE.md](./FOLDER_STRUCTURE.md) |

---

## 1. مقدمة (Introduction)

تصف هذه الوثيقة البنية الداخلية لخادم Kayansoft المبني على NestJS: تقسيم الموديولات، طبقات المسؤولية، البنية التحتية المشتركة، وتدفّق الطلب من المدخل حتى قاعدة البيانات.

## 2. الهدف (Purpose)

توفير مرجع دقيق لمطوري الـ Backend حول كيفية تنظيم الكود، أين يُوضع كل نوع من المنطق، وكيف تُطبَّق الأمان والتحقق والمعاملات بشكل منهجي.

## 3. لماذا NestJS؟ (Rationale)

| السبب | التفصيل |
|------|---------|
| إطار رأي‑موجّه | يفرض بنية Modules/Providers تمنع الفوضى |
| حقن التبعيات (DI) | اختبار أسهل، اقتران أضعف |
| Guards/Pipes/Interceptors/Filters | أمان وتحقق ومعالجة أخطاء منهجية |
| دعم TypeScript أصلي | أمان أنواع كامل |
| نظام بيئي ناضج | Prisma, Swagger, Passport, BullMQ |

البديل المرفوض: Express خام (يتطلب بناء كل البنية يدوياً → ديون تقنية).

## 4. طبقات المسؤولية (Layered Responsibilities)

```
HTTP Request
    │
    ▼
┌─────────────┐  المسؤولية: استقبال/توجيه فقط — لا منطق عمل
│ Controller  │  يستدعي Service، يعيد ApiResponse
└──────┬──────┘
       ▼
┌─────────────┐  المسؤولية: منطق العمل بالكامل (Business Logic)
│  Service    │  يطبّق القواعد، ينسّق المعاملات
└──────┬──────┘
       ▼
┌─────────────┐  المسؤولية: الوصول للبيانات فقط
│PrismaService│  استعلامات/معاملات
└──────┬──────┘
       ▼
   PostgreSQL
```

**قاعدة ملزمة:** لا منطق عمل في الـ Controller، ولا SQL خام متناثر — كل وصول عبر Prisma.

## 5. البنية التحتية المشتركة (Common Infrastructure)

| العنصر | الموقع | الوظيفة |
|--------|--------|---------|
| `JwtAuthGuard` | common/guards | التحقق من توكن JWT |
| `RolesGuard` | common/guards | فرض الأدوار (RBAC) |
| `TenantGuard` | common/guards | حقن وفرض `tenantId` |
| `DeviceGuard` | common/guards | مصادقة الـ SMS Agent |
| `AllExceptionsFilter` | common/filters | شكل خطأ موحّد (راجع [ERROR_CODES](./ERROR_CODES.md)) |
| `TransformInterceptor` | common/interceptors | تغليف الاستجابة `{success,data,meta}` |
| `LoggingInterceptor` | common/interceptors | تسجيل الطلبات (Pino) |
| `ValidationPipe` (عام) | main.ts | تحقق DTOs عبر class-validator |
| `@CurrentUser` `@Roles` `@Tenant` | common/decorators | اختصارات سياق الطلب |

## 6. تقسيم الموديولات (Module Breakdown)

| Module | المسؤولية | يعتمد على |
|--------|-----------|-----------|
| **AuthModule** | تسجيل/دخول/تجديد توكن/كلمة مرور | Users, Prisma |
| **UsersModule** | إدارة المستخدمين والأدوار | Prisma |
| **TenantsModule** | المستأجرون وعزلهم | Prisma |
| **DevicesModule** | أجهزة الـ Agent + Device Tokens | Prisma |
| **WalletsModule** | كتالوج المحافظ + تهيئتها لكل مستأجر | Prisma |
| **CategoriesModule** | الفئات الديناميكية | Prisma |
| **CardsModule** | المخزون + إضافة جماعية + عدّادات | Categories, Parsing(AI) |
| **ParsingModule** | تحليل نص SMS (أنماط/قوالب/AI) | Wallets, Templates |
| **IngestionModule** | استقبال الرسائل + Idempotency + الجدولة | Parsing, Distribution, Devices |
| **DistributionModule** | اختيار كارت ذرّي + إرسال + تسجيل | Cards, Deposits, Transactions, Mappings, Settings |
| **ApprovalsModule** | التفويضات المعلّقة | Distribution, Deposits |
| **MappingsModule** | مطابقات العملاء | Prisma |
| **DepositsModule** | سجل الإيداعات | Prisma |
| **TransactionsModule** | المعاملات + CSV | Prisma |
| **ReportsModule** | تجميعات وإحصاءات | Prisma |
| **NotificationsModule** | دفع الأحداث (FCM) — لاحقاً | — |
| **SettingsModule / TemplatesModule** | إعدادات وقوالب المستأجر | Prisma |
| **PrismaModule / ConfigModule / CommonModule** | بنية تحتية | — |

## 7. تدفّق طلب نموذجي (Request Lifecycle)

```
Request
  → Middleware (CORS, helmet)
  → Guards (JwtAuthGuard → RolesGuard → TenantGuard)
  → Pipes (ValidationPipe على DTO)
  → Interceptor (Logging) [before]
  → Controller method
  → Service (business logic + Prisma transaction)
  → Interceptor (Transform) [after] → {success, data, meta}
  → (عند الخطأ) AllExceptionsFilter → {success:false, error}
Response
```

## 8. محرك التحليل والتوزيع (Parsing & Distribution Core)

أهم جزأين تشغيلياً ومعزولان عن HTTP ليكونا قابلين للاختبار:

- **ParsingModule:** يحوّل نص SMS إلى نتيجة منظمة عبر قواعد بيانات‑موجّهة (data-driven rules) لكل محفظة + قوالب المستأجر. لا حالة، نقي قدر الإمكان.
- **DistributionModule:** يقرر المستلم، يحجز الكارت ضمن Transaction ذرّية، يسجّل Deposit + Transaction، ويطلق الإرسال. يطبّق `TenantSettings.autoSend`.

> التفصيل الكامل في [SMS_ENGINE.md](./SMS_ENGINE.md).

## 9. إدارة المعاملات (Transaction Management)

- كل عملية توزيع تُغلَّف في `prisma.$transaction`:
  1. اختيار أول كارت `AVAILABLE` بالفئة مع قفل.
  2. تحديثه إلى `RESERVED` ثم `USED`.
  3. إنشاء Deposit + Transaction.
- في حال أي فشل → تراجع كامل (Rollback) → لا كارت محجوز خطأً.

## 10. الإعداد وإدارة البيئة (Configuration)

- `@nestjs/config` + تحقق مخطط (Joi/zod) لمتغيرات البيئة عند الإقلاع.
- لا قيمة سرية في الكود (راجع [SECURITY_GUIDELINES](./SECURITY_GUIDELINES.md)).
- متغيرات رئيسية: `DATABASE_URL`, `JWT_SECRET`, `JWT_REFRESH_SECRET`, `AI_API_KEY`(مشفّر لكل مستأجر), `PORT`.

## 11. التوثيق التلقائي (API Documentation)

- `@nestjs/swagger` يولّد OpenAPI على `/api/docs`.
- كل DTO وكل endpoint موثّق بـ decorators.
- يتزامن مع [API_SPECIFICATION](./API_SPECIFICATION.md) و[API_CONTRACT](./API_CONTRACT.md).

## 12. التسجيل والمراقبة (Logging & Observability)

- **Pino** للتسجيل المنظّم (JSON).
- مستويات: error/warn/info/debug؛ لا تسجيل لبيانات حساسة (أكواد كروت/أسرار).
- معرّف ارتباط (correlationId) لكل طلب لتتبّع التدفق.

## 13. حالات الاستخدام (Use Cases)

| UC | الموديولات المعنية |
|----|--------------------|
| دخول مستخدم | Auth → Users |
| استقبال رسالة | Ingestion → Parsing → Distribution/Approvals |
| إضافة كروت جماعية | Cards → Categories |
| موافقة تفويض | Approvals → Distribution |
| تقرير | Reports |

## 14. حالات الخطأ (Error Handling)

- كل الأخطاء تمرّ عبر `AllExceptionsFilter` وتُحوَّل لرموز موحّدة ([ERROR_CODES](./ERROR_CODES.md)).
- أخطاء التحقق (400) تُجمَّع من class-validator.
- أخطاء التفويض (401/403) من Guards.
- أخطاء العمل (409 توزيع مزدوج/نفاد) برموز مخصصة.

## 15. القيود (Constraints)

- لا منطق عمل خارج طبقة Service.
- لا استعلام بلا `tenantId` (عدا الجداول المرجعية العامة كـ Wallet catalog).
- لا تغيير مخطط بلا migration.

## 16. تحسينات مستقبلية (Future Improvements)

- طوابير BullMQ + Redis للإرسال وإعادة المحاولة.
- CQRS لفصل القراءة (تقارير) عن الكتابة عند الحاجة.
- Caching (Redis) للبيانات المرجعية والتقارير المتكررة.
- WebSocket/SSE للإشعارات الفورية.

---

> التالي: [FLUTTER_ARCHITECTURE.md](./FLUTTER_ARCHITECTURE.md) · [API_SPECIFICATION.md](./API_SPECIFICATION.md)
