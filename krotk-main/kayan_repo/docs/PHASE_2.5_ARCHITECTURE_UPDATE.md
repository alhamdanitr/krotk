# PHASE_2.5_ARCHITECTURE_UPDATE.md
**Architecture Change - Multi-Tenant SaaS Enhancement**
**Date:** 2026-06-30

---

## 1. نظرة عامة (Overview)
هذا المستند يوثق التحديثات المعمارية التي أُدخلت على النظام في مرحلة 2.5 قبل الشروع في بناء المحرك الأساسي (Phase 3). هذه التحديثات تضمن أن المنصة قابلة للتوسع وتدعم جميع المتطلبات المستقبلية دون الحاجة لإعادة هيكلة.

---

## 2. تحديثات قاعدة البيانات (Database Schema Changes)

### 2.1 جدول `tenants` - حقول جديدة
| الحقل | النوع | الوصف |
|-------|-------|-------|
| `logo` | `TEXT?` | رابط أو Base64 لشعار الشبكة (اختياري، من Wizard) |
| `country` | `VARCHAR(50)` | دولة العميل (إجباري، افتراضي: 'Yemen') |
| `currency` | `VARCHAR(10)` | رمز العملة المحلية (إجباري، افتراضي: 'YER') |

**السبب:** إضافة هذه الحقول تجعل التقارير والفواتير والرسائل الصادرة تعكس هوية العميل الكاملة (اسم + شعار + عملة)، وهي متطلب أساسي لمنتج SaaS قابل للتوسع الجغرافي.

### 2.2 جدول `plans` - حقول جديدة
| الحقل | النوع | الوصف |
|-------|-------|-------|
| `max_users` | `INT` | الحد الأقصى لعدد المستخدمين المسموح بهم في هذه الخطة |
| `features` | `TEXT?` | قائمة JSON للمميزات المتاحة في هذه الخطة |

**السبب:** السيريال الآن لا يحدد المدة فقط، بل يحدد صلاحيات العميل بالكامل. هذا يتيح إنشاء خطط متدرجة (Basic, Pro, Unlimited) مستقبلاً دون تعديل الكود.

### 2.3 Migration المنفذة
تم تنفيذ Migration ناجحة بنجاح وتطبيقها على قاعدة بيانات Railway:
```
20260629213638_phase2_5_update/migration.sql ✅
```

---

## 3. تحديثات الـ API Contracts

### 3.1 `POST /auth/wizard/register` - تحديث
**قبل التحديث (v2.0):**
```json
{ "networkName": "string", "username": "string", "password": "string" }
```
**بعد التحديث (v2.5):**
```json
{
  "networkName": "شبكة المستقبل",
  "logo": "https://...",
  "country": "Yemen",
  "currency": "YER",
  "username": "admin",
  "password": "password123"
}
```

### 3.2 تحديث Login Response
استجابة تسجيل الدخول تعود الآن بحقل `tenant` موسع يحتوي على:
```json
{
  "tenant": {
    "networkName": "شبكة المستقبل",
    "country": "Yemen",
    "currency": "YER",
    "status": "trial",
    "expiryDate": "...",
    "remainingDays": 7,
    "planType": "Trial"
  }
}
```

---

## 4. هيكل المشروع المحدث (Updated Project Structure)
```
kurotek-backend/
├── src/
│   ├── prisma/                     ✅ Global PrismaModule
│   ├── common/
│   │   ├── decorators/             ✅ @Public, @CurrentUser, @Roles
│   │   └── guards/                 ✅ Jwt, Tenant, Trial, Subscription, Roles
│   ├── auth/                       ✅ Login, Register Wizard, Activate Serial
│   │   ├── dto/
│   │   └── strategies/
│   │
│   │   ── مرحلة 3 (Phase 3) - قيد الانتظار ──
│   ├── cards/                      🔜 Card Inventory & Bulk Import
│   ├── sms/                        🔜 SMS Webhook & Smart Distribution Engine
│   ├── deposits/                   🔜 Deposit Records
│   ├── reports/                    🔜 Tenant Reports & Statistics
│   │
│   │   ── مستقبلي (Future Modules) ──
│   ├── subscriptions/              🕓 Manage & Renew subscriptions
│   ├── plans/                      🕓 Super Admin plans management
│   ├── licenses/                   🕓 Serial key generation
│   ├── distributor-calculator/     🕓 حاسبة موزع الدحشة (مستقبلي)
│   └── super-admin/                🕓 Super Admin Dashboard APIs
├── prisma/
│   ├── schema.prisma               ✅ v2.5 Schema (with Trial, Country, Currency, Plan limits)
│   └── migrations/
│       ├── 20260629212653_init/    ✅
│       └── 20260629213638_phase2_5_update/ ✅
```

---

## 5. التمهيد لوحدة حاسبة الموزع (Distributor Calculator - Architecture Prep)

### 5.1 القرار الهندسي
تم تصميم هيكل المشروع بحيث يمكن إضافة وحدة `distributor-calculator` كـ **NestJS Module مستقل** في أي وقت دون المساس بأي كود موجود.

### 5.2 المتطلبات المعمارية المسبقة
- ستكون الوحدة مرتبطة بـ `tenantId` (كل موزع ينتمي لشبكة عميل واحدة).
- ستستخدم الـ `currency` الخاصة بالـ Tenant لعرض الأرقام بالعملة الصحيحة.
- ستظهر في قائمة التنقل كتبويب منفصل (أسفل التطبيق).
- المدخلات مستقبلاً: أسماء الموزعين، المبيعات، المديونيات.
- المخرجات: تقرير أرباح وصافي مستحقات كل موزع.

### 5.3 لماذا لا نبرمجها الآن؟
بناءً على مبدأ **YAGNI (You Aren't Gonna Need It)** من هندسة البرمجيات، لن نبني ما لم تتضح متطلباته النهائية بالكامل. ما قمنا به هو **حجز المكان والتوثيق** لضمان أن إضافتها لاحقاً لا تتطلب أي Refactoring.

---

## 6. توجيهات تصميم الواجهات (UI/UX - Disgain Reference)
بناءً على تحليل مجلد `Disgain` (Jaib Fintech Design System):

### 6.1 ما يُحتفظ به من Disgain
- **نظام الألوان:** Primary Red `#E53935`، خلفية داكنة `#0F172A`، Surface `#1E293B`.
- **الخطوط:** خط Cairo (متوافق مع العربية) بنفس تدرج الأوزان.
- **المكونات:** BottomNavigation, Cards, ActionButtons, BottomSheets.
- **الحركة والتفاعل:** `active:scale-95` على الأزرار، انتقالات 200ms للتبويبات.

### 6.2 إعادة التسمية (Renaming for Kurotek Context)
| الاسم في Disgain | الاسم الجديد في Kurotek |
|---|---|
| الرئيسية (Home) | لوحة التحكم |
| الخدمات (Services) | المخزون |
| التقارير (Reports) | العمليات |
| الملف (Profile) | الإعدادات |
| نقل (Transfer) | توزيع كرت |
| حسابات | حاسبة الموزع *(مستقبلي)* |

### 6.3 عناصر تُضاف لـ Kurotek
- شاشة القفل (Expired Trial Screen) - بنفس أسلوب Disgain.
- شاشة Wizard الإعداد الأولي (5 خطوات).
- شاشة إدخال السيريال (Activation Screen).
- بطاقة حالة الاشتراك في صفحة الإعدادات.

---

## 7. الديون التقنية المعلقة (Pending Tech Debt)
| الرقم | الوصف | الأولوية |
|---|---|---|
| TD-01 | إنشاء `POST /auth/refresh` لاستخدام الـ Refresh Token | عالية - قبل الإنتاج |
| TD-02 | حل مشكلة `bcrypt` build scripts في pnpm | عالية - قبل الإنتاج |
| TD-03 | بناء Refresh Token Rotation | متوسطة |
| TD-04 | إضافة Rate Limiting على Auth Endpoints | عالية - أمان |
| TD-05 | Cron Job لإيقاف الـ Trials المنتهية تلقائياً | عالية - منطق عمل |

---

## 8. الحالة العامة وما تم
- ✅ قاعدة البيانات: Railway PostgreSQL، جميع الجداول مطبقة بنجاح.
- ✅ Auth Module: Login, Register Wizard, Activate Serial.
- ✅ Guards & Decorators: نظام حماية متكامل.
- ✅ Swagger: جاهز على `/api/docs`.
- ✅ Schema v2.5: country, currency, logo, plan limits.
- 🔜 Phase 3: Cards & SMS Engine.

**بانتظار موافقتك للبدء في Phase 3.**
