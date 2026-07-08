# DATABASE_ERD — مخطط علاقات الكيانات

| الحقل | القيمة |
|------|--------|
| الوثيقة | مخطط العلاقات (Entity Relationship Diagram) |
| الحالة | REVIEW |
| المرجع | [DATABASE_SPECIFICATION.md](./DATABASE_SPECIFICATION.md) |

---

## 1. مقدمة (Introduction)

تقدّم هذه الوثيقة التمثيل البصري (نصّي/ASCII) لعلاقات قاعدة بيانات Kayansoft، مكمّلةً لـ [DATABASE_SPECIFICATION](./DATABASE_SPECIFICATION.md) التي تحوي تفاصيل الأعمدة.

## 2. الهدف (Purpose)

تمكين الفريق من فهم بنية البيانات والعلاقات بنظرة واحدة، وتسهيل مراجعة التصميم قبل أي migration.

## 3. مفتاح الرموز (Notation Key)

| الرمز | المعنى |
|------|--------|
| `1───*` | واحد إلى متعدد |
| `1───1` | واحد إلى واحد |
| `1───0..1` | واحد إلى صفر أو واحد |
| `PK` | مفتاح أساسي |
| `FK` | مفتاح أجنبي |
| `U` | قيد فريد |

## 4. المخطط الكلي (Full ERD)

```
                            ┌──────────────┐
                            │   Tenant     │ (الجذر — كل شيء معزول به)
                            │  PK id       │
                            └──────┬───────┘
        ┌──────────────┬──────────┼───────────┬──────────────┬─────────────┐
        │              │          │           │              │             │
        ▼              ▼          ▼           ▼              ▼             ▼
  ┌──────────┐  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────┐
  │  User    │  │ Device   │ │ Category │ │  Card    │ │ Customer │ │TenantSettings│
  │ PK id    │  │ PK id    │ │ PK id    │ │ PK id    │ │ Mapping  │ │ PK id  U     │
  │ FK tenant│  │ FK tenant│ │ FK tenant│ │ FK tenant│ │ PK id    │ │ FK tenant U  │
  │ role     │  │ tokenHash│ │ value  U │ │ FK categ │ │ FK tenant│ └──────────────┘
  └────┬─────┘  └────┬─────┘ └────┬─────┘ │ status   │ └──────────┘
       │             │            │       └────┬─────┘
       ▼             ▼            │            │
  ┌──────────┐  ┌─────────────┐   │            │
  │Refresh   │  │InboundMessage│  │            │
  │Token     │  │ PK id        │  │            │
  │ FK user  │  │ FK device    │  │   (Category 1───* Card)
  └──────────┘  │ dedupHash U  │  │            │
                │ status       │  │            │
                └──────┬───────┘  │            │
                       │ 1───0..1 │            │
                       ▼          │            │
                ┌──────────────┐  │            │
                │   Deposit    │  │            │
                │ PK id        │  │            │
                │ FK inbound   │  │            │
                │ FK wallet    │  │            │
                │ status       │  │            │
                └──┬────────┬──┘  │            │
          1───0..1 │        │ 1───0..1         │
                   ▼        ▼                  │
        ┌────────────────┐ ┌──────────────┐    │
        │ PendingApproval│ │ Transaction  │◄───┘ (Card 1───0..1 Transaction)
        │ PK id          │ │ PK id        │
        │ FK deposit  U  │ │ FK deposit   │
        │ status         │ │ FK card      │
        └────────────────┘ │ FK wallet    │
                           │ result       │
                           └──────────────┘

   ┌──────────┐         ┌─────────────────────┐      ┌─────────────────┐
   │  Wallet  │ 1───*   │ TenantWalletConfig  │      │ MessageTemplate │
   │ PK id    │────────►│ FK tenant + FK wallet│     │ PK id           │
   │ key   U  │         │ (tenant,wallet) U   │      │ FK tenant       │
   └────┬─────┘         └─────────────────────┘      │ type            │
        │ (Wallet مرجعي لـ Deposit/Transaction/Mapping)└─────────────────┘
        └──────────────────────────────────────────►
```

## 5. جدول العلاقات (Relationships Table)

| # | الأصل | النوع | الهدف | المفتاح |
|---|------|------|-------|---------|
| R1 | Tenant | 1—* | User | User.tenantId |
| R2 | Tenant | 1—* | Device | Device.tenantId |
| R3 | Tenant | 1—* | Category | Category.tenantId |
| R4 | Tenant | 1—* | Card | Card.tenantId |
| R5 | Tenant | 1—* | CustomerMapping | CustomerMapping.tenantId |
| R6 | Tenant | 1—1 | TenantSettings | TenantSettings.tenantId (U) |
| R7 | Tenant | 1—* | MessageTemplate | MessageTemplate.tenantId |
| R8 | Category | 1—* | Card | Card.categoryId |
| R9 | Device | 1—* | InboundMessage | InboundMessage.deviceId |
| R10 | InboundMessage | 1—0..1 | Deposit | Deposit.inboundMessageId |
| R11 | Deposit | 1—0..1 | PendingApproval | PendingApproval.depositId (U) |
| R12 | Deposit | 1—0..1 | Transaction | Transaction.depositId |
| R13 | Card | 1—0..1 | Transaction | Transaction.cardId |
| R14 | Wallet | 1—* | TenantWalletConfig | TenantWalletConfig.walletId |
| R15 | Wallet | 1—* | Deposit/Transaction/Mapping | .walletId |
| R16 | User | 1—* | RefreshToken | RefreshToken.userId |

## 6. دورة حياة الكيانات الأساسية (Entity Lifecycle)

### 6.1 دورة حياة الكارت (Card)
```
AVAILABLE ──(حجز عند التوزيع)──► RESERVED ──(تأكيد)──► USED
    │                                                    
    └──(حذف منطقي)──► VOID
```

### 6.2 دورة حياة الإيداع (Deposit)
```
PENDING ──(توزيع ناجح)──► COMPLETED
   │
   ├──(رفض تفويض)──► REJECTED
   └──(فشل إرسال/نفاد)──► FAILED
```

### 6.3 دورة حياة الرسالة (InboundMessage)
```
RECEIVED ──(تحليل ناجح)──► PARSED ──► (Deposit/Approval)
   │
   ├──(لا نمط مطابق)──► IGNORED
   └──(خطأ)──► FAILED
```

## 7. قواعد التكامل المرجعي (Referential Rules)

| العلاقة | ON DELETE | السبب |
|---------|-----------|-------|
| Deposit → InboundMessage | RESTRICT | حفظ التتبّع المالي |
| Transaction → Deposit | RESTRICT | لا تفقد سجلاً مالياً |
| Card → Category | RESTRICT | منع يُتم الكروت |
| RefreshToken → User | CASCADE | تنظيف الجلسات مع المستخدم |

## 8. حالات الاستخدام (Use Cases)

- مراجعة تأثير إضافة عمود/علاقة قبل migration.
- تتبّع مسار البيانات من الرسالة حتى المعاملة.
- تدريب الأعضاء الجدد على نموذج البيانات.

## 9. حالات الخطأ (Error Conditions)

| الحالة | السبب |
|--------|------|
| محاولة حذف فئة بها كروت | RESTRICT يمنع |
| إيداع بلا رسالة مصدر | FK يمنع |
| تفويضان لإيداع واحد | UNIQUE(depositId) يمنع |

## 10. القيود (Constraints)

- المخطط يتبع [DATABASE_SPECIFICATION](./DATABASE_SPECIFICATION.md) حرفياً؛ أي اختلاف يُحدَّث في كليهما.

## 11. تحسينات مستقبلية (Future Improvements)

- توليد ERD رسومي تلقائياً (prisma-erd-generator) ودمج الصورة هنا.
- إضافة كيان `AuditLog` للمخطط.

---

> التالي: [API_SPECIFICATION.md](./API_SPECIFICATION.md)
