# PHASE_3.5_FOUNDATION.md
# Backend Contracts & Engine Foundation
**Date:** 2026-06-30 | **Status:** ✅ Complete

---

## 1. ملخص المرحلة
تم في هذه المرحلة بناء الهيكل الكامل (Skeleton) لمحرك الرسائل والكروت — بدون أي Business Logic — بحيث يكون تنفيذ Phase 4 الفعلي سريعاً، نظيفاً، وقابلاً للتوسع.

---

## 2. تحديثات قاعدة البيانات ✅

تم تطبيق Schema النهائية بنجاح عبر `prisma db push` على Railway.

### التعديلات المطبقة:
| الجدول | التعديل | السبب |
|---|---|---|
| `deposits` | إضافة `idempotency_key` (UNIQUE) | منع معالجة نفس الرسالة مرتين |
| `deposits` | إضافة `sender_code` | حفظ الكود الوهمي قبل التحويل |
| `deposits` | فهرس `[tenantId, createdAt]` | تسريع تقارير التواريخ |
| `deposits` | فهرس `[tenantId, walletType]` | تسريع الفلترة حسب المحفظة |
| `transactions` | إضافة `card_id` | ربط المعاملة بالكرت المصروف |
| `transactions` | فهرس `[tenantId, createdAt]` | تسريع التقارير اليومية والشهرية |

---

## 3. الملفات التي تم إنشاؤها (28 ملف)

### Interfaces (التعاقدات)
| الملف | الوصف |
|---|---|
| `common/interfaces/sms-parser.interface.ts` | `ISmsParser`, `RawSms`, `ParsedDeposit` |
| `common/interfaces/card-allocator.interface.ts` | `ICardAllocator`, `CardAllocationRequest/Result` |
| `common/interfaces/deposit-processor.interface.ts` | `IDepositProcessor`, `DepositProcessingResult` |
| `common/interfaces/notification-provider.interface.ts` | `INotificationProvider` |
| `common/interfaces/report-generator.interface.ts` | `IReportGenerator` |

### Exceptions (استثناءات المحرك)
| الملف | الاستثناءات |
|---|---|
| `common/exceptions/engine.exceptions.ts` | `DuplicateSmsException`, `CardNotFoundException`, `CardAlreadyReservedException`, `UnsupportedWalletException`, `SubscriptionExpiredException`, `InvalidDepositException`, `TenantSuspendedException` |

### Events (الأحداث)
| الملف | الأحداث |
|---|---|
| `common/events/engine.events.ts` | `SmsReceivedEvent`, `DepositCreatedEvent`, `CardReservedEvent`, `CardDeliveredEvent`, `CardDeliveryFailedEvent`, `TransactionCreatedEvent` |

### Modules (الوحدات - هياكل فارغة)
| الوحدة | Controller | Service | DTO |
|---|---|---|---|
| `sms-engine` | ✅ | ✅ (stub) | `ReceiveSmsDto` |
| `parser-registry` | - | ✅ (كامل) | - |
| `card-engine` | - | ✅ (stub) | `AllocateCardDto` |
| `deposits` | ✅ | ✅ (stub) | `CreateDepositDto` |
| `reports` | ✅ | ✅ (stub) | - |
| `notifications` | - | ✅ (stub) | - |

### Test Skeletons
| الملف | ما يختبره |
|---|---|
| `sms-engine/sms-engine.service.spec.ts` | 4 سيناريوهات موثقة لـ Phase 4 |
| `parser-registry/parser-registry.service.spec.ts` | ✅ اختبار حقيقي: Register + Detect + Unknown |
| `card-engine/card-engine.service.spec.ts` | 3 سيناريوهات موثقة لـ Phase 4 |

---

## 4. ما هو قابل للاستخدام الآن

**`ParserRegistryService`** جاهز بالكامل ومختبر:
```typescript
// تسجيل محفظة جديدة:
registry.register(new JaibParser());

// الكشف التلقائي:
const parser = registry.detect(rawSms); // أو throw UnsupportedWalletException
```

---

## 5. هيكل المشروع النهائي بعد هذه المرحلة
```
src/
├── prisma/                        ✅ Global
├── common/
│   ├── decorators/                ✅ @Public, @CurrentUser, @Roles
│   ├── guards/                    ✅ Jwt, Tenant, Trial, Subscription, Roles
│   ├── interfaces/                ✅ ISmsParser, ICardAllocator, IDepositProcessor ...
│   ├── exceptions/                ✅ 7 Engine Exceptions
│   └── events/                    ✅ 6 Engine Events
├── auth/                          ✅ Login, Wizard Register, Activate Serial
├── sms-engine/                    ✅ Skeleton (Phase 4 ready)
├── parser-registry/               ✅ Fully functional Registry
├── card-engine/                   ✅ Skeleton (Phase 4 ready)
├── deposits/                      ✅ Skeleton (Phase 4 ready)
├── reports/                       ✅ Skeleton (Phase 5 ready)
└── notifications/                 ✅ Skeleton (Phase 4 ready)
```

---

## 6. نسبة تقدم المشروع
| المرحلة | النسبة |
|---|---|
| Phase 1: قاعدة البيانات | 100% ✅ |
| Phase 2: Auth Module | 100% ✅ |
| Phase 2.5: Architecture Update | 100% ✅ |
| Phase 3: Engine Design | 100% ✅ |
| Phase 3.5: Engine Foundation | 100% ✅ |
| **Phase 4: Business Logic** | **0% — جاهز للبدء** 🔜 |

**النسبة الكلية للـ Backend:** ~55% (البنية التحتية مكتملة بالكامل)

---

## 7. ما تبقى في Phase 4
1. كتابة Parsers الحقيقية (Jaib, Flossy, كاش, الخ) تطبق `ISmsParser`.
2. بناء `DepositProcessingPipeline` الكامل.
3. تنفيذ `reserveCard` بـ `SELECT FOR UPDATE SKIP LOCKED`.
4. ربط `NotificationsService` بـ SMS Gateway.

**بانتظار موافقتك لبدء Phase 4.**
