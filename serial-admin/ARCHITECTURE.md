# Serial Admin — تطبيق إدارة السيريالات المستقل

> `applicationId: com.kayansoft.serialadmin`

## 1. الغرض من المشروع

تطبيق أندرويد **مستقل تمامًا** (لا يشارك أي كود أو Gradle project مع باقي التطبيقات) مخصص لصاحب النظام/الإدارة، لإنشاء وإدارة سيريالات التفعيل التي تستخدمها [`cards-app`](../cards-app) و[`mobile-app`](../mobile-app). يتصل بنفس سيرفر الترخيص الذي تتصل به تطبيقات الكروت، لضمان مصدر حقيقة واحد للسيريالات.

## 2. الأدوات والتقنيات المستخدمة

| الفئة | الأداة |
|---|---|
| اللغة | Kotlin |
| الواجهة | Jetpack Compose + Material 3 |
| الشبكة | Retrofit + Moshi (تحويل JSON بالانعكاس `KotlinJsonAdapterFactory`، بدون Codegen/KSP) + OkHttp |
| حفظ الجلسة | Jetpack DataStore Preferences |
| نظام البناء | Gradle (Kotlin DSL) — مشروع منفصل بالكامل بـ`gradlew` خاص به |

## 3. هيكلة الملفات

```
serial-admin/
├── settings.gradle.kts, build.gradle.kts, gradlew, gradle/
└── app/
    ├── build.gradle.kts        ← بدون توقيع إصدار حقيقي بعد (Debug فقط، انظر القسم 6)
    └── src/main/
        ├── AndroidManifest.xml   ← يمنع Cleartext Traffic (HTTPS فقط)
        ├── res/                  أيقونة تطبيق مميزة (كهرماني/Slate) لتمييزه بصريًا عن تطبيقات الكروت
        └── java/com/kayansoft/serialadmin/
            ├── MainActivity.kt        نقطة الدخول، يقرر عرض تسجيل الدخول أو اللوحة حسب وجود Token
            ├── AdminViewModel.kt      كل منطق الحالة: تسجيل دخول، جلب البيانات، إنشاء/تعديل/حذف سيريال
            ├── network/
            │   ├── AdminModels.kt       نماذج البيانات (Client, Serial, LoginRequest/Response...)
            │   ├── AdminApiService.kt    واجهة Retrofit (login, dashboard, create/reset/toggle/delete)
            │   ├── RetrofitClient.kt      إعداد Retrofit/OkHttp/Moshi + BASE_URL
            │   └── TokenManager.kt        حفظ/قراءة/حذف توكن الأدمن عبر DataStore
            └── ui/
                ├── LoginScreen.kt          تسجيل دخول (اسم مستخدم/كلمة مرور)
                ├── DashboardScreen.kt       إحصائيات + قائمة السيريالات + إجراءات لكل سيريال
                └── CreateSerialDialog.kt    نافذة إنشاء سيريال جديد
```

## 4. طريقة سير العمل (Workflow)

1. `MainActivity` يتحقق عبر `TokenManager` إذا يوجد توكن محفوظ مسبقًا:
   - **لا يوجد** → يعرض `LoginScreen`.
   - **يوجد** → يحاول تحميل `DashboardScreen` مباشرة؛ إذا رجّع السيرفر 401/403 (جلسة منتهية)، يُعاد المستخدم تلقائيًا لشاشة تسجيل الدخول.
2. **تسجيل الدخول**: `AdminViewModel.login()` يرسل `POST /api/v1/admin/login`، يحفظ التوكن الناتج، وينتقل للوحة.
3. **اللوحة**: تعرض بطاقات إحصائية (الكل/فعّال/غير مُفعّل/موقوف) محسوبة من قائمة السيريالات المجلوبة عبر `GET /api/v1/admin/dashboard`.
4. **إنشاء سيريال**: نافذة تجمع (اسم العميل، الشبكة، الهاتف، مدة الاشتراك، معرّف جهاز اختياري للربط المسبق، ملاحظات) وترسلها لـ`POST /api/v1/admin/serial/create`.
5. **إجراءات كل سيريال**: تفعيل/إيقاف (`POST .../toggle/:id`)، فك ربط الجهاز (`POST .../reset/:id`)، حذف نهائي (`DELETE .../delete/:id`) — كل إجراء يُحدّث القائمة تلقائيًا بعد النجاح.

## 5. طريقة التشغيل

1. افتح مجلد `serial-admin/` بـAndroid Studio (مستقل تمامًا عن باقي المشروع).
2. Gradle Sync ثم شغّل على جهاز/محاكي.
3. سجّل الدخول بنفس `ADMIN_USERNAME`/`ADMIN_PASSWORD` المضبوطة كمتغيرات بيئة على السيرفر (انظر [`server/ARCHITECTURE.md`](../server/ARCHITECTURE.md)).

**عنوان السيرفر** مضبوط بـ`RetrofitClient.kt`:
```kotlin
const val BASE_URL = "https://kayan-licensing-server.onrender.com/"
```

## 6. متبقٍّ قبل النشر الفعلي
- **لا يوجد توقيع إصدار (Release Signing)** بعد — التطبيق حاليًا بإصدار Debug فقط. لازم إنشاء Keystore حقيقي وربطه بـ`app/build.gradle.kts` (المتغيرات `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD` جاهزة بالفعل بالكود، فقط تحتاج قيم حقيقية).
