# STATE_MANAGEMENT — إدارة الحالة في Flutter

| الحقل | القيمة |
|------|--------|
| الوثيقة | إدارة الحالة (Riverpod) |
| الحالة | REVIEW |
| المرجع | [FLUTTER_ARCHITECTURE.md](./FLUTTER_ARCHITECTURE.md) |

---

## 1. مقدمة (Introduction)

تحدد هذه الوثيقة كيفية إدارة الحالة في تطبيق Kayansoft باستخدام **Riverpod**، بما يشمل أنواع الـ Providers، أنماط الحالة، التعامل مع غير المتزامن (async)، ومعالجة الأخطاء.

## 2. الهدف (Purpose)

توحيد طريقة إدارة الحالة عبر كل الميزات لضمان قابلية الاختبار، تجنّب إعادة البناء غير الضرورية، وفصل منطق العرض عن منطق العمل.

## 3. لماذا Riverpod؟ (Rationale)

| الميزة | الفائدة |
|--------|---------|
| آمن الأنواع بالكامل (compile‑safe) | أخطاء أقل وقت التشغيل |
| مستقل عن شجرة `BuildContext` | اختبار أسهل، لا أخطاء context |
| دعم async/caching مدمج | `FutureProvider`/`AsyncNotifier` |
| auto‑dispose | إدارة ذاكرة تلقائية |
| قابلية الاستبدال (override) | اختبار وحقن تبعيات سهل |

البديل المرفوض: `setState` (لا يتوسّع) و`Provider` الكلاسيكي (أقل أماناً للأنواع).

## 4. أنواع الـ Providers المعتمدة (Provider Types)

| النوع | الاستخدام |
|------|-----------|
| `Provider` | قيم/خدمات ثابتة (Dio client, Repositories) |
| `FutureProvider` | جلب بيانات لمرة واحدة (قراءة بسيطة) |
| `StreamProvider` | تدفّقات (إشعارات/تحديثات فورية لاحقاً) |
| `NotifierProvider` | حالة قابلة للتعديل متزامنة |
| `AsyncNotifierProvider` | حالة async قابلة للتعديل (الأكثر استخداماً للقوائم) |

## 5. نمط الحالة الموحّد (Unified State Pattern)

كل ميزة ذات بيانات async تستخدم `AsyncValue<T>` الذي يغطّي الحالات الأربع:

```
AsyncValue<T>
├── AsyncLoading   → عرض مؤشر تحميل
├── AsyncData(T)   → عرض البيانات (أو حالة فارغة إن كانت T فارغة)
└── AsyncError     → عرض خطأ + إعادة محاولة
```

```dart
// نموذج توضيحي (ليس كوداً للتنفيذ الآن)
final cardsProvider =
    AsyncNotifierProvider<CardsNotifier, List<Card>>(CardsNotifier.new);

class CardsNotifier extends AsyncNotifier<List<Card>> {
  @override
  Future<List<Card>> build() => ref.read(getCardsProvider)();

  Future<void> addBulk(String raw) async {
    state = const AsyncLoading();
    state = await AsyncValue.guard(() async {
      await ref.read(addCardsBulkProvider)(raw);
      return ref.read(getCardsProvider)();
    });
  }
}
```

## 6. تدفّق الحالة (State Flow)

```
UI (ConsumerWidget)
   │ ref.watch(provider)
   ▼
Notifier/Provider ── يستدعي ──► UseCase ──► Repository ──► API
   │
   └─ يحدّث state (AsyncLoading → AsyncData/AsyncError)
   ▼
UI يُعاد بناؤه تلقائياً
```

## 7. تنظيم الـ Providers (Organization)

- كل ميزة تحوي `presentation/providers/` خاصاً بها.
- Providers البنية التحتية (Dio, storage, repositories) في `core/`.
- لا Provider عالمي ضخم؛ التقسيم حسب الميزة.

## 8. حقن التبعيات (Dependency Injection)

- Riverpod نفسه آلية الـ DI: Repositories وUseCases تُعرّف كـ `Provider` وتُحقن عبر `ref.read`.
- في الاختبارات تُستبدل عبر `ProviderScope(overrides: [...])`.

## 9. أفضل الممارسات (Best Practices)

- استخدم `ref.watch` في `build` و`ref.read` في معالجات الأحداث.
- استخدم `AsyncValue.guard` لالتقاط الأخطاء تلقائياً.
- فعّل `autoDispose` للحالات المؤقتة (شاشات تُفتح وتُغلق).
- استخدم `select` لتقليل إعادة البناء عند مراقبة جزء من الحالة.
- لا تضع منطق عمل في الـ Notifier — فوّضه للـ UseCases.

## 10. حالات الاستخدام (Use Cases)

| UC | Provider | السلوك |
|----|----------|--------|
| عرض المخزون | `cardsProvider` (AsyncNotifier) | تحميل → عرض/فارغ |
| تبديل الخدمة | `settingsProvider` (Notifier) | تحديث تفاؤلي + تأكيد من API |
| التفويضات الحيّة | `approvalsProvider` | إعادة جلب دوري/Stream |
| المصادقة | `authProvider` | يحرس التوجيه عبر GoRouter |

## 11. حالات الخطأ (Error Handling)

- `AsyncError` يحمل الخطأ والـ stack؛ يُحوَّل لرسالة عربية عبر mapper موحّد.
- عمليات الكتابة الفاشلة تُرجع الحالة لقيمتها السابقة (rollback تفاؤلي).
- أخطاء المصادقة (401) تُعالَج مركزياً في `RefreshInterceptor`؛ الفشل النهائي يسجّل الخروج.

## 12. الاختبار (Testing)

- اختبر الـ Notifiers بعزل عبر استبدال الـ UseCases بـ mocks.
- تحقق من تتابع الحالات (Loading → Data/Error).
- راجع [TESTING_STRATEGY](./TESTING_STRATEGY.md).

## 13. القيود (Constraints)

- لا استخدام `setState` لإدارة حالة الأعمال (مسموح فقط لحالة UI محلية بحتة كـ animation).
- لا حالة عامة قابلة للتعديل خارج Riverpod.

## 14. تحسينات مستقبلية (Future Improvements)

- `StreamProvider` متصل بـ WebSocket للتفويضات الفورية.
- طبقة caching/persistence للحالة (offline).
- مولّد Providers عبر `riverpod_generator` لتقليل النمطية.

---

> التالي: [DATABASE_SPECIFICATION.md](./DATABASE_SPECIFICATION.md) · [UI_UX_GUIDELINES.md](./UI_UX_GUIDELINES.md)
