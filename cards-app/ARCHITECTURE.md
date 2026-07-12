# Cards App (Dahshacards) — تطبيق بيع كروت الإنترنت التلقائي

> `applicationId: com.aistudio.dahshacards.uylxtb`

## 1. الغرض من المشروع

تطبيق أندرويد يعمل كنظام بيع وتوزيع كروت إنترنت/ميكروتك تلقائي. يراقب رسائل SMS الواردة من المحافظ الإلكترونية اليمنية (جيب، جوالي، الكريمي، حسب، ون كاش، أم فلوس)، يتحقق من صحة المبلغ المُحوَّل، ثم يرسل كرت ميكروتك تلقائيًا للعميل عبر SMS. يشمل أيضًا نظام محاسبة موزع كامل (عملاء، ديون، مبيعات، مصاريف، رأس مال).

هذا هو **النسخة التاريخية الأولى** من التطبيق (الاسم الداخلي للحزمة `com.example`، من مشروع AI Studio الأصلي). توجد نسخة موازية أحدث بميزات إضافية في مشروع [`mobile-app`](../mobile-app).

## 2. الأدوات والتقنيات المستخدمة

| الفئة | الأداة |
|---|---|
| اللغة | Kotlin |
| واجهة المستخدم | Jetpack Compose + Material 3 |
| قاعدة البيانات المحلية | Room (SQLite) |
| الشبكة | OkHttp + Retrofit (يدوي، بدون مكتبة موحّدة بكل الملفات) |
| الذكاء الاصطناعي | Gemini API (تحليل نصوص SMS غير الاعتيادية عبر `GeminiSmsAnalyzer.kt`) |
| نظام البناء | Gradle (Kotlin DSL), AGP 9.1.1 |

## 3. هيكلة الملفات

```
cards-app/
├── settings.gradle.kts       ← include(":app")
├── build.gradle.kts           ← جذر Gradle (plugins apply false)
├── gradlew / gradlew.bat
├── gradle/                    ← Gradle Wrapper + Version Catalog
├── assets/
├── .env.example                ← GEMINI_API_KEY
├── package.json, metadata.json ← بقايا إعداد AI Studio الأصلي (لا تؤثر على بناء Android)
└── app/                        ← الموديول الفعلي (:app)
    ├── build.gradle.kts
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml
        └── java/com/example/
            ├── MainActivity.kt              نقطة الدخول، شاشة تسجيل الدخول/التفعيل
            ├── database/
            │   ├── AppDatabase.kt            تعريف Room Database
            │   └── CardRepository.kt         طبقة الوصول للبيانات (كل عمليات CRUD)
            ├── models/                       Card, Deposit, Transaction, PendingApproval,
            │                                 CustomerMapping, GeneratedMikrotikCard,
            │                                 DistributorModels (عميل موزع، بيع، مصروف، رأس مال)
            ├── network/
            │   ├── CloudSyncEngine.kt         مزامنة HTTP دورية مع السيرفر
            │   ├── KurotekWebSocketClient.kt   اتصال WebSocket فوري (Real-time)
            │   ├── SyncManager.kt / SyncService.kt  إدارة دورة حياة المزامنة بالخلفية
            ├── receiver/
            │   ├── SmsReceiver.kt              التقاط رسائل SMS الواردة وتحليلها
            │   └── PendingApprovalReceiver.kt  معالجة حالات الموافقة اليدوية
            ├── security/
            │   ├── DeviceSecurity.kt           كشف Root/محاكي، بصمة الجهاز
            │   └── SecurityApiService.kt        التفعيل + التحقق من السيريال (HMAC + JWT)
            ├── ui/
            │   ├── LoginScreen.kt               شاشة إدخال السيريال والتفعيل
            │   ├── MainDashboardScreen.kt        الشاشة الرئيسية (Bottom Navigation + كل التبويبات)
            │   ├── DistributorSystemScreen.kt    نظام حاسبة الموزع (عملاء/بيع/مصاريف/رأس مال)
            │   ├── MikrotikGeneratorScreen.kt     توليد كروت ميكروتك يدويًا
            │   ├── MainViewModel.kt               حالة التطبيق وربط الشاشات بقاعدة البيانات
            │   └── theme/                          Color.kt, Theme.kt, Type.kt, Shape.kt, Components.kt
            └── utils/
                ├── SmsParser.kt / SmsSender.kt      استخراج بيانات التحويل من نص SMS + إرسال الرد
                ├── GeminiSmsAnalyzer.kt              تحليل SMS بالذكاء الاصطناعي للحالات الغامضة
                ├── DocumentExporter.kt               تصدير التقارير (CSV)
                └── NotificationBus.kt                ناقل أحداث داخلي بين الخدمات والواجهة
```

## 4. طريقة سير العمل (Workflow)

1. **التفعيل**: عند أول تشغيل، `LoginScreen` يطلب سيريال. `SecurityApiService` يرسل طلب موقّع بـHMAC إلى السيرفر (`/api/v1/serial/validate`)، ويحفظ الجهاز مقفلًا على هذا السيريال بعد أول تفعيل (Device Locking).
2. **الرصد التلقائي**: `SmsReceiver` (BroadcastReceiver) يلتقط كل SMS وارد، `SmsParser` يحاول استخراج (المبلغ، المحفظة، رقم المرسل) بقواعد Regex محددة لكل محفظة؛ إذا فشل التحليل التقليدي، تُستخدم `GeminiSmsAnalyzer` كخط دفاع ثانٍ بالذكاء الاصطناعي.
3. **المطابقة والإرسال**: عند نجاح التحليل، `CardRepository` يبحث عن كرت غير مستخدم بنفس الفئة (المبلغ)، يخصّصه للعميل، و`SmsSender` يرسل بيانات الكرت تلقائيًا كرد SMS للعميل.
4. **الموافقات المعلّقة**: إذا كان المبلغ غير مطابق تمامًا أو الرسالة غامضة، تُحفظ كـ`PendingApproval` بانتظار مراجعة يدوية من الشاشة الرئيسية.
5. **حاسبة الموزع**: نظام منفصل تمامًا (شاشة كاملة تستبدل الواجهة الرئيسية) لتتبع ديون العملاء الموزّعين، تسجيل عمليات البيع، المصاريف، وحركة رأس المال — بيانات محلية بالكامل (Room)، لا ترتبط حاليًا بأي مزامنة سحابية.
6. **المزامنة**: `CloudSyncEngine`/`SyncManager` يرفعان نسخة من البيانات المحلية للسيرفر بشكل دوري (HTTP polling)، بينما `KurotekWebSocketClient` (إن فُعِّل) يوفر تحديثات فورية.

## 5. طريقة التشغيل

1. افتح مجلد `cards-app/` (وليس المستودع كاملًا) بـ**Android Studio**.
2. أنشئ ملف `.env` بنفس المجلد وضع فيه `GEMINI_API_KEY=<مفتاحك>` (انظر `.env.example`).
3. اضبط عنوان السيرفر إن لزم داخل `SecurityApiService.kt`/`CloudSyncEngine.kt` (حاليًا يشير إلى `kayan-licensing-server.onrender.com`، وهو نفس سيرفر مشروع [`server`](../server)).
4. Gradle Sync ثم شغّل على جهاز/محاكي.

## 6. ملاحظة مهمة
هذا المشروع و[`mobile-app`](../mobile-app) متطابقان بمعظم الملفات (فرق بـ5 ملفات فقط حاليًا: `AppDatabase.kt`, `CardRepository.kt`, `Deposit.kt`, `Transaction.kt`, `MainViewModel.kt` — النسخة بـ`mobile-app` أحدث). يُفضّل لاحقًا اعتماد نسخة واحدة رسمية بدل الصيانة المزدوجة.
