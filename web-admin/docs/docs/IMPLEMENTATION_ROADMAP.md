# Implementation Roadmap & Task List (Micro Tasks)

## 1. مقدمة
تستعرض هذه الوثيقة خطة التنفيذ مقسمة إلى مهام صغيرة جداً ومستقلة (Micro Tasks) لضمان تسلسل العمل وعدم التداخل، مما يسهل المراجعة والاعتماد بعد كل مهمة.

## 2. قواعد التنفيذ (Execution Rules)
- لا يمكن البدء في مهمة قبل اكتمال المهمة التي تسبقها في الاعتمادية (Dependencies).
- كل مهمة يجب أن تحتوي على Unit Test (في الـ Backend).
- كل مهمة يجب أن تجتاز Definition of Done (DoD).

## 3. قائمة المهام الدقيقة (The Task List)

### Phase 1: Backend Foundations (البنية التحتية)
- [ ] **Task 1.1:** تهيئة مشروع NestJS فارغ + إضافة `Prisma` + إعداد ملف `.env`.
- [ ] **Task 1.2:** كتابة `schema.prisma` كاملاً وبناء الجداول وتوليد `PrismaClient`.
- [ ] **Task 1.3:** إعداد `ConfigModule` لقراءة متغيرات البيئة.
- [ ] **Task 1.4:** إنشاء `GlobalExceptionFilter` وتطبيق `ValidationPipe`.
- [ ] **Task 1.5:** إعداد `Swagger` Documentation على المسار `/api/docs`.

### Phase 2: Authentication & Security (نظام الأمان)
- [ ] **Task 2.1:** إنشاء `AuthModule` و `ShopsModule`.
- [ ] **Task 2.2:** إنشاء `jwt.strategy` لحماية الواجهات.
- [ ] **Task 2.3:** تنفيذ `POST /auth/login` (التحقق من كلمة المرور).
- [ ] **Task 2.4:** إنشاء Decorator `@CurrentUser()` لاستخراج بيانات المشرف من الطلب.

### Phase 3: Core Business Logic (الكروت والمحافظ)
- [ ] **Task 3.1:** إنشاء `CardsModule` وتنفيذ `GET /cards/stock`.
- [ ] **Task 3.2:** تنفيذ `POST /cards/bulk` لتحليل النص الوارد إلى مصفوفة كروت وحفظها في Prisma (مع معالجة الأخطاء).
- [ ] **Task 3.3:** إنشاء `WalletConfigsModule` للتحكم بقوالب الـ Regex.
- [ ] **Task 3.4:** إنشاء `CustomerMappingsModule`.

### Phase 4: The SMS Engine (المحرك الذكي)
- [ ] **Task 4.1:** كتابة الكلاس المساعد `RegexParserUtil` مدعوماً بـ Unit Tests قوية لكل المحافظ (جيب، جوالي، إلخ).
- [ ] **Task 4.2:** تنفيذ خدمة معالجة الإيداع وصرف الكرت `processSms()` باستخدام Prisma Transactions.
- [ ] **Task 4.3:** إنشاء `DepositsModule` و `TransactionsModule` لتخزين سجل العمليات (History).
- [ ] **Task 4.4:** إنشاء `PendingApprovalsModule` لمعالجة الطلبات المعلقة والموافقة اليدوية عليها.

### Phase 5: Flutter Foundations (تطبيق الموبايل)
- [ ] **Task 5.1:** تهيئة مشروع Flutter (إعداد pubspec، المجلدات حسب Clean Architecture).
- [ ] **Task 5.2:** بناء ملفات التصميم `AppColors`, `AppTextStyles`, `AppTheme`.
- [ ] **Task 5.3:** إعداد `DioClient` مع `AuthInterceptor`.
- [ ] **Task 5.4:** إعداد `GoRouter` وموجهات التنقل.

### Phase 6: Flutter UI & Integration
- [ ] **Task 6.1:** تنفيذ `LoginScreen` وربطها بالـ Backend وحفظ الـ Token في `SecureStorage`.
- [ ] **Task 6.2:** تنفيذ `HomeScreen` (لوحة القيادة والمؤشرات).
- [ ] **Task 6.3:** تنفيذ `CardsScreen` (عرض المخزون، نافذة لصق الكروت Bulk).
- [ ] **Task 6.4:** تنفيذ `PendingScreen` (شاشة المعلقات وأزرار الموافقة والرفض).

### Phase 7: Android Native Bridge (نواة الاتصال)
- [ ] **Task 7.1:** كتابة كود الـ Kotlin `SmsReceiver` في Android.
- [ ] **Task 7.2:** تنفيذ استدعاء API من داخل Android Background Service باستخدام `OkHttp` لإرسال الرسالة للـ Backend.
- [ ] **Task 7.3:** إضافة كود قراءة الرد (Response) وتمريره للـ `SmsManager` لإرسال رسالة SMS للعميل.

### Phase 8: Deployment & Launch (الإطلاق)
- [ ] **Task 8.1:** إعداد ملف `Dockerfile` للسيرفر والنشر على `Railway`.
- [ ] **Task 8.2:** عمل فحص أمني شامل (Security Audit) ومراجعة الأداء (Indexes).
- [ ] **Task 8.3:** بناء التطبيق `APK` النهائي ووضعه قيد الاستخدام الميداني.

---
*(الموافقة على هذه الوثيقة تعني أن مسار التطوير أصبح واضحاً ومحدداً بدقة 100% ولن يتم الحياد عنه).*
