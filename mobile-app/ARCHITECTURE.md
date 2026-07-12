# Mobile App — النسخة المتقدمة من تطبيق بيع كروت الإنترنت

> ⚠️ `applicationId: com.aistudio.dahshacards.uylxtb` — **نفس معرّف [`cards-app`](../cards-app) بالضبط**. هذا سيمنع تثبيت التطبيقين معًا على نفس الجهاز، ويمنع رفعهما كتطبيقين منفصلين على Google Play. يجب تغيير أحدهما قبل أي نشر فعلي.

## 1. الغرض من المشروع

نفس فكرة [`cards-app`](../cards-app) (بيع كروت إنترنت تلقائي عبر مراقبة SMS + حاسبة موزع)، لكن بنسخة أكثر نضجًا تقنيًا — تحتوي كل ميزات `cards-app` بالإضافة لطبقة مزامنة فورية واختبارات آلية.

## 2. الأدوات والتقنيات المستخدمة

نفس تقنيات `cards-app` (Kotlin, Jetpack Compose, Room, Gradle Kotlin DSL) بالإضافة إلى:
- **WebSocket حقيقي** (`KurotekWebSocketClient.kt`) بدل الاعتماد فقط على HTTP polling
- **اختبارات آلية**: Robolectric (اختبارات وحدة تعمل على JVM بدون جهاز)، واختبارات Screenshot (`GreetingScreenshotTest`)
- ثيم (Theme/Color/Type) منفصل بوضوح عن باقي الكود

## 3. هيكلة الملفات

```
mobile-app/
├── settings.gradle.kts, build.gradle.kts, gradlew, gradle/
└── app/
    ├── build.gradle.kts
    └── src/
        ├── main/java/com/example/          (نفس بنية cards-app بالضبط: database/, models/,
        │                                     network/, receiver/, security/, ui/, utils/)
        ├── test/                            اختبارات وحدة (JVM، Robolectric)
        └── androidTest/                     اختبارات فعلية على جهاز/محاكي
```

الفرق الوحيد الحالي عن `cards-app` هو 5 ملفات لها نسخة أحدث هنا:
`database/AppDatabase.kt`, `database/CardRepository.kt`, `models/Deposit.kt`, `models/Transaction.kt`, `ui/MainViewModel.kt`.

## 4. طريقة سير العمل (Workflow)

مطابق تمامًا لما هو موثّق في [`cards-app/ARCHITECTURE.md`](../cards-app/ARCHITECTURE.md) قسم "طريقة سير العمل" — نفس دورة (استلام SMS → تحليل → مطابقة كرت → إرسال) ونفس نظام حاسبة الموزع. الفرق العملي الوحيد: المزامنة هنا تدعم قناة WebSocket فورية بجانب HTTP polling، مما يعني تحديثات شبه لحظية بين الأجهزة المتصلة بنفس السيرفر (بدل الاعتماد فقط على دورات مزامنة متباعدة).

## 5. طريقة التشغيل

1. افتح مجلد `mobile-app/` بـAndroid Studio (مستقل تمامًا، لا يحتاج أي شيء من مجلدات أخرى).
2. **قبل التشغيل على نفس جهاز فيه `cards-app`**: غيّر `applicationId` بـ`app/build.gradle.kts` (مثلًا لـ`com.aistudio.dahshacards.mobile`) لتفادي تعارض التثبيت.
3. Gradle Sync ثم شغّل.
4. لتشغيل الاختبارات: `./gradlew test` (Robolectric) أو `./gradlew connectedAndroidTest` (على جهاز/محاكي).

## 6. التوصية
بما إن هذا المشروع يحتوي كل ميزات `cards-app` + إضافات (WebSocket، اختبارات)، يُفضّل اعتماده كالنسخة الرسمية الوحيدة مستقبلًا، وأرشفة `cards-app`.
