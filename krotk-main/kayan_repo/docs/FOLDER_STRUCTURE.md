# FOLDER_STRUCTURE — هيكل المجلدات

| الحقل | القيمة |
|------|--------|
| الوثيقة | هيكل مجلدات كل المستودعات |
| الحالة | REVIEW |
| المرجع | [SDD.md](./SDD.md) · [BACKEND_ARCHITECTURE.md](./BACKEND_ARCHITECTURE.md) · [FLUTTER_ARCHITECTURE.md](./FLUTTER_ARCHITECTURE.md) |

---

## 1. مقدمة (Introduction)

تحدد هذه الوثيقة الهيكل المعياري للمجلدات في جميع مستودعات Kayansoft. الالتزام بهذا الهيكل إلزامي لضمان قابلية التنقّل والصيانة واتساق الفريق.

## 2. الهدف (Purpose)

توفير خريطة دقيقة لمكان كل نوع من الملفات، بحيث يعرف أي مطور أين يضع كوداً جديداً وأين يجد كوداً قائماً، ومنع الفوضى التنظيمية التي عانى منها النظام القديم (ملف داشبورد بـ 4184 سطراً).

## 3. استراتيجية المستودعات (Repository Strategy)

نتبنّى **Multi‑Repo** (ثلاثة مستودعات منفصلة) لفصل دورات الإصدار والصلاحيات:

| المستودع | المحتوى | اللغة |
|----------|---------|-------|
| `kayansoft-backend` | الـ API ومنطق العمل | TypeScript (NestJS) |
| `kayansoft-app` | تطبيق الإدارة | Dart (Flutter) |
| `kayansoft-agent` | عميل التقاط الرسائل | Kotlin (Android) |
| `kayansoft-docs` | مكتبة التوثيق (هذه) | Markdown |

> **قرار هندسي:** Multi‑Repo بدلاً من Mono‑Repo لأن الأطراف الثلاثة لها دورات إصدار وفِرَق صلاحيات وأدوات بناء مختلفة جذرياً. يُعاد النظر إذا كبر الفريق وزادت الحاجة لمشاركة الكود.

## 4. هيكل الـ Backend (NestJS)

```
kayansoft-backend/
├── docker-compose.yml          # postgres + api للتطوير المحلي
├── Dockerfile                  # بناء إنتاجي متعدد المراحل
├── .env.example                # قالب متغيرات البيئة
├── .eslintrc.js · .prettierrc  # جودة الكود
├── package.json · tsconfig.json
├── prisma/
│   ├── schema.prisma           # المخطط الكامل
│   ├── migrations/             # ترحيلات متعقَّبة (لا destructive)
│   └── seed.ts                 # بيانات أولية
├── src/
│   ├── main.ts                 # bootstrap + Swagger + ValidationPipe
│   ├── app.module.ts
│   ├── common/                 # عناصر مشتركة
│   │   ├── decorators/         # @CurrentUser @Roles @Tenant
│   │   ├── guards/             # JwtAuthGuard RolesGuard TenantGuard DeviceGuard
│   │   ├── interceptors/       # Logging Transform Timeout
│   │   ├── filters/            # AllExceptionsFilter
│   │   ├── pipes/              # تحقق إضافي
│   │   ├── dto/                # PaginationDto ApiResponse
│   │   └── utils/              # idempotency, money, phone-normalizer
│   ├── config/                 # config.module + تحقق متغيرات البيئة
│   ├── prisma/                 # PrismaModule + PrismaService
│   └── modules/                # موديولات الميزات (راجع BACKEND_ARCHITECTURE)
│       ├── auth/  users/  tenants/  devices/
│       ├── wallets/  categories/  cards/
│       ├── parsing/  ingestion/  distribution/
│       ├── approvals/  mappings/
│       ├── deposits/  transactions/  reports/
│       ├── notifications/  settings/
└── test/                       # e2e + unit
```

بنية الموديول الواحد (نموذجية):
```
modules/cards/
├── cards.module.ts
├── cards.controller.ts
├── cards.service.ts
├── dto/
│   ├── create-card.dto.ts
│   ├── bulk-create.dto.ts
│   └── query-cards.dto.ts
└── entities/                   # أنواع داخلية إن لزم
```

## 5. هيكل تطبيق Flutter

```
kayansoft-app/
├── pubspec.yaml
├── analysis_options.yaml       # قواعد الـ lint
└── lib/
    ├── main.dart               # ProviderScope + bootstrap
    ├── app.dart                # MaterialApp.router + theme (RTL/dark)
    ├── core/                   # مشترك عبر الميزات
    │   ├── config/             # env, api base url, flavors
    │   ├── theme/              # ألوان/خطوط/مكوّنات (من DESIGN_TOKENS)
    │   ├── router/             # app_router.dart (GoRouter + redirects)
    │   ├── network/            # dio_client.dart + interceptors
    │   ├── error/              # failures.dart · exceptions.dart
    │   ├── storage/            # secure_storage (tokens)
    │   ├── usecase/            # UseCase<Type, Params> base
    │   └── widgets/            # مكوّنات UI عامة
    └── features/               # راجع FLUTTER_ARCHITECTURE
        ├── auth/
        ├── dashboard/
        ├── cards/
        ├── categories/
        ├── approvals/
        ├── customers/
        ├── reports/
        ├── wallets/
        └── settings/
```

بنية الميزة الواحدة (Clean Architecture):
```
features/cards/
├── data/
│   ├── datasources/cards_remote_datasource.dart
│   ├── models/card_model.dart           # freezed + json
│   └── repositories/cards_repository_impl.dart
├── domain/
│   ├── entities/card.dart
│   ├── repositories/cards_repository.dart  # abstract
│   └── usecases/get_cards.dart · add_cards_bulk.dart
└── presentation/
    ├── screens/cards_screen.dart
    ├── widgets/card_tile.dart
    └── providers/cards_provider.dart    # Riverpod
```

## 6. هيكل SMS Agent (Android)

```
kayansoft-agent/
├── app/
│   └── src/main/
│       ├── AndroidManifest.xml          # صلاحيات SMS + receiver
│       └── java/com/kayansoft/agent/
│           ├── AgentApp.kt
│           ├── receiver/SmsReceiver.kt  # التقاط فقط
│           ├── network/ApiClient.kt     # POST /ingestion/sms
│           ├── service/OutboxWorker.kt  # (اختياري) إرسال الأوامر
│           ├── store/DeviceTokenStore.kt
│           └── ui/PairingActivity.kt    # ربط الجهاز + الحالة
└── build.gradle.kts
```

> الـ Agent **لا يحوي منطق عمل** — فقط التقاط وإعادة توجيه وحالة. هذا التزام بـ ADR‑01.

## 7. هيكل مكتبة التوثيق

```
kayansoft-docs/  (= docs/)
├── README.md                   # الفهرس ونقطة الدخول
├── PROJECT_RULES.md
├── MASTER_BLUEPRINT.md
├── SRS.md · SDD.md
├── SYSTEM_ARCHITECTURE.md
├── ... (بقية الوثائق الـ 32)
└── (أصول مساعدة إن لزمت مستقبلاً)
```

## 8. اصطلاحات التسمية (Naming Conventions)

| العنصر | الاصطلاح | مثال |
|--------|----------|------|
| ملفات TS | kebab-case | `cards.service.ts` |
| كلاسات TS | PascalCase | `CardsService` |
| ملفات Dart | snake_case | `cards_screen.dart` |
| كلاسات Dart | PascalCase | `CardsScreen` |
| مجلدات | kebab/snake حسب اللغة | `parsing/` |
| ملفات التوثيق | UPPER_SNAKE | `API_SPECIFICATION.md` |

التفاصيل في [CODING_STANDARDS.md](./CODING_STANDARDS.md).

## 9. حالات الاستخدام (Use Cases)

- مطور جديد يبحث عن مكان إضافة ميزة → يجد القالب جاهزاً.
- مراجع كود يتحقق من وضع الملف في طبقته الصحيحة.
- أداة CI تتحقق من بنية المجلدات.

## 10. حالات الخطأ الشائعة (Common Mistakes)

| الخطأ | الصحيح |
|------|--------|
| منطق في Controller/Widget | في Service/UseCase |
| ملف عملاق متعدد المسؤوليات | تقسيم حسب الميزة/الطبقة |
| مجلد خارج الهيكل المعتمد | اتبع القوالب أعلاه |

## 11. القيود (Constraints)

- لا يُضاف مجلد جذري جديد بلا تحديث هذه الوثيقة أولاً (Docs‑First).
- الهيكل موحّد عبر كل الميزات؛ لا استثناءات بلا اعتماد.

## 12. تحسينات مستقبلية (Future Improvements)

- مولّد سكافولد (generator) ينشئ بنية ميزة جاهزة وفق هذه القوالب.
- فحص آلي في CI لمطابقة الهيكل.
- احتمال الانتقال لـ Mono‑Repo مع أدوات مثل Nx إذا توسّع الفريق.

---

> التالي: [BACKEND_ARCHITECTURE.md](./BACKEND_ARCHITECTURE.md) · [FLUTTER_ARCHITECTURE.md](./FLUTTER_ARCHITECTURE.md)
