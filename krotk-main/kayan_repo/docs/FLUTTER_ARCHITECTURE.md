# FLUTTER_ARCHITECTURE — معمارية تطبيق Flutter

| الحقل | القيمة |
|------|--------|
| الوثيقة | معمارية تطبيق الإدارة (Flutter) |
| الحالة | REVIEW |
| المرجع | [SYSTEM_ARCHITECTURE.md](./SYSTEM_ARCHITECTURE.md) · [STATE_MANAGEMENT.md](./STATE_MANAGEMENT.md) · [FOLDER_STRUCTURE.md](./FOLDER_STRUCTURE.md) |

---

## 1. مقدمة (Introduction)

تصف هذه الوثيقة معمارية تطبيق Kayansoft المبني على Flutter، والذي يمثّل واجهة الإدارة الكاملة للمشغّل (المخزون، التفويضات، التقارير، الإعدادات). يتبع التطبيق **Clean Architecture** بتنظيم **Feature‑First**.

## 2. الهدف (Purpose)

ضمان أن تطبيق Flutter قابل للاختبار والصيانة والتوسّع، عبر فصل صارم بين طبقات العرض والمجال والبيانات، وتجنّب الملف العملاق الذي عانى منه النظام القديم.

## 3. لماذا Flutter + Clean Architecture؟ (Rationale)

| القرار | السبب | البديل المرفوض |
|--------|-------|----------------|
| Flutter | تعدد منصات، أداء أصلي، دعم RTL عربي ممتاز | إبقاء Android الأصلي (منصة واحدة) |
| Clean Architecture | فصل قابل للاختبار ومستقل عن الأطر | معمارية مسطّحة (تشابك) |
| Feature‑First | تنظيم حسب الميزة → صيانة أسهل | Layer‑First (تشتّت الميزة) |
| Riverpod | حالة آمنة الأنواع وقابلة للاختبار | setState/Provider الكلاسيكي |
| Dio | Interceptors قوية (JWT/refresh/error) | http الأساسي |
| GoRouter | توجيه تصريحي + حُرّاس مصادقة | Navigator 1.0 يدوي |

## 4. الطبقات الثلاث (The Three Layers)

```
┌──────────────────────────────────────────────────────┐
│  Presentation (UI)                                    │
│  Screens · Widgets · Providers (Riverpod)             │
│  يستدعي UseCases فقط — لا منطق بيانات                  │
└───────────────────────┬──────────────────────────────┘
                        │ يعتمد على
                        ▼
┌──────────────────────────────────────────────────────┐
│  Domain (المنطق النقي)                                 │
│  Entities · Repository Interfaces · UseCases          │
│  لا تبعية لأي إطار خارجي                               │
└───────────────────────┬──────────────────────────────┘
                        │ يُنفَّذ بواسطة
                        ▼
┌──────────────────────────────────────────────────────┐
│  Data                                                 │
│  Models (freezed/json) · DataSources (Dio) ·          │
│  Repository Implementations                           │
└──────────────────────────────────────────────────────┘
```

**قاعدة التبعية (Dependency Rule):** الاتجاه دائماً نحو الداخل — Presentation → Domain ← Data. الـ Domain لا يعرف شيئاً عن الطبقات الأخرى.

## 5. تفصيل الطبقات (Layer Details)

### 5.1 Domain
- **Entities:** كائنات أعمال نقية (مثل `Card`, `Deposit`) بلا تبعيات.
- **Repository Interfaces:** عقود مجرّدة (`abstract class CardsRepository`).
- **UseCases:** عملية واحدة لكل صنف (`GetCards`, `AddCardsBulk`) — مدخل واضح للمنطق.

### 5.2 Data
- **Models:** ترث/تحوّل من Entities مع (de)serialization عبر `freezed` + `json_serializable`.
- **DataSources:** تتعامل مع Dio (`CardsRemoteDataSource`).
- **Repository Impl:** تحوّل DTO→Entity وتعالج الأخطاء→`Failure`.

### 5.3 Presentation
- **Screens/Widgets:** عرض فقط.
- **Providers (Riverpod):** تربط الـ UI بالـ UseCases وتدير الحالة (راجع [STATE_MANAGEMENT](./STATE_MANAGEMENT.md)).

## 6. الطبقة المشتركة (Core Layer)

| المجلد | المحتوى |
|--------|---------|
| `core/config` | env, base url, flavors |
| `core/theme` | ألوان/خطوط من [DESIGN_TOKENS](./DESIGN_TOKENS.md) |
| `core/router` | GoRouter + redirects للمصادقة |
| `core/network` | Dio client + interceptors (auth/refresh/error) |
| `core/error` | `Failure` و`Exception` types |
| `core/storage` | `flutter_secure_storage` للتوكنات |
| `core/usecase` | قاعدة `UseCase<Type, Params>` |
| `core/widgets` | مكوّنات عامة (أزرار، بطاقات، حالات فارغة) |

## 7. الميزات (Features)

| Feature | الشاشات | UseCases بارزة |
|---------|---------|-----------------|
| **auth** | دخول، تغيير كلمة المرور | Login, Refresh, Logout, GetMe |
| **dashboard** | الرئيسية (حالة الخدمة + عدّادات) | GetCardStats, ToggleService |
| **cards** | المخزون (إضافة/تصفية/حذف) | GetCards, AddCardsBulk, DeleteCard, ExtractCardsAI |
| **categories** | إدارة الفئات | GetCategories, AddCategory, DeleteCategory |
| **approvals** | التفويضات المعلّقة | GetApprovals, Approve, Reject, EditPhone |
| **customers** | مطابقات العملاء | GetMappings, UpsertMapping, DeleteMapping |
| **reports** | إيداعات/معاملات/تصدير | GetDeposits, GetTransactions, ExportCsv, GetSummary |
| **wallets** | تفعيل/تعطيل المحافظ | GetWallets, ToggleWallet |
| **settings** | القوالب/المظهر/كلمة المرور/الأجهزة | GetSettings, UpdateSettings, ManageTemplates, ManageDevices |

التفاصيل الوظيفية في [FEATURE_SPECIFICATIONS](./FEATURE_SPECIFICATIONS.md).

## 8. التواصل مع الـ Backend (Networking)

```
UseCase → Repository → RemoteDataSource → Dio
                                          │
                Interceptors: [AuthInterceptor] إرفاق JWT
                             [RefreshInterceptor] تجديد عند 401
                             [ErrorInterceptor] تحويل لـ Failure
                                          │
                                          ▼
                                    Backend API
```

- كل الاستجابات تتبع العقد الموحّد (راجع [API_CONTRACT](./API_CONTRACT.md)).
- الأخطاء تُحوَّل لرموز [ERROR_CODES](./ERROR_CODES.md) ثم لرسائل عربية ودّية.

## 9. التوجيه والملاحة (Routing)

- **GoRouter** تصريحي مع `redirect` يحرس المسارات حسب حالة المصادقة.
- Bottom Navigation بستة أقسام رئيسية (مطابقة لتجربة النظام القديم): الرئيسية، الكروت، التفويضات، العملاء، التقارير، الإعدادات.
- Deep Linking مدعوم للإشعارات.

## 10. التعريب وRTL (Localization)

- `intl` + ملفات ARB؛ العربية افتراضية مع دعم RTL كامل.
- كل النصوص خارج الكود (لا hardcoded strings).

## 11. حالات الاستخدام (Use Cases)

| UC | التدفق داخل التطبيق |
|----|---------------------|
| عرض المخزون | Screen → cardsProvider → GetCards → Repo → API |
| موافقة تفويض | Screen → approvalsProvider → Approve → API → تحديث القائمة |
| تبديل الخدمة | Dashboard → ToggleService → API → تحديث الحالة |

## 12. حالات الخطأ (Error Handling)

- كل UseCase يعيد `Either<Failure, T>` (نمط fpdart) أو يرمي `Failure` مُلتقَطة في الـ Provider.
- الـ UI يعرض حالات: تحميل / نجاح / خطأ / فارغ — عبر مكوّنات موحّدة في `core/widgets`.

## 13. الاختبار (Testing)

- **Unit:** UseCases و Repositories (mocktail).
- **Widget:** الشاشات الرئيسية.
- **Integration:** تدفقات أساسية (دخول، توزيع، تفويض).
- التفاصيل في [TESTING_STRATEGY](./TESTING_STRATEGY.md).

## 14. القيود (Constraints)

- لا منطق عمل في الـ Widgets.
- لا استدعاء Dio مباشرة من الـ Presentation.
- لا تخزين توكنات خارج `flutter_secure_storage`.

## 15. تحسينات مستقبلية (Future Improvements)

- دعم الويب (Flutter Web) كلوحة تحكم إضافية.
- وضع عدم اتصال (Offline cache) للقراءات.
- إشعارات فورية عبر FCM.
- نظام Theming ديناميكي قابل للتخصيص لكل مستأجر.

---

> التالي: [STATE_MANAGEMENT.md](./STATE_MANAGEMENT.md) · [UI_UX_GUIDELINES.md](./UI_UX_GUIDELINES.md)
