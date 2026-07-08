# Software Design Document (SDD)
# منصة Kurotek — التصميم التقني الشامل

**الإصدار:** 3.0 — تحديث Phase 6  
**التاريخ:** 2026-07-04

---

## 1. المعمارية العامة (Architecture Overview)

```
┌─────────────────────────────────────────┐
│              NestJS Backend              │
│  ┌───────────┐  ┌──────────────────────┐│
│  │  Modules  │  │    Global Providers  ││
│  │  Auth     │  │  JwtAuthGuard        ││
│  │  Cards    │  │  TenantGuard         ││
│  │  Deposits │  │  RolesGuard          ││
│  │  SMS Eng  │  │  SubscriptionGuard   ││
│  │  Dashboard│  │  TrialGuard          ││
│  │  Reports  │  └──────────────────────┘│
│  │Distributor│                           │
│  └─────┬─────┘                           │
│        │ Prisma ORM                      │
└────────┼────────────────────────────────┘
         │
┌────────▼────────────────────────────────┐
│         PostgreSQL Database              │
│  (Multi-Tenant via tenantId FK)          │
└─────────────────────────────────────────┘
```

---

## 2. هيكلة الموديولات (Module Structure)

```
src/
├── auth/                     # المصادقة والتسجيل والتفعيل
├── common/
│   ├── decorators/           # @CurrentUser, @Public, @Roles
│   ├── dto/                  # PaginationDto + DateRangeDto
│   ├── guards/               # JWT, Tenant, Roles, Trial, Subscription
│   └── services/
│       └── alerts.service.ts # خدمة التنبيهات المشتركة (مستقلة)
├── dashboard/                # لوحة التحكم الرئيسية
├── reports/                  # التقارير + التصدير
├── transactions/             # عمليات البيع الكلية (SMS)
├── deposits/                 # الإيداعات (SMS)
├── cards/                    # مخزون الكروت
├── inventory/                # تقارير المخزون
├── wallets/                  # إعدادات المحافظ
├── sms-engine/               # محرك SMS
├── distributor/              # ◀ حاسبة الموزع (الجديد)
│   ├── distributor.module.ts
│   ├── pricing/              # التسعيرة
│   ├── customers/            # العملاء + كشف الحساب
│   ├── sales/                # المبيعات + الفواتير
│   ├── finance/              # المالية (أرباح، مصروفات، رأس مال)
│   └── dto/                  # كل DTOs الموديول
└── prisma/                   # PrismaService + PrismaModule
```

---

## 3. قواعد البيانات (Schema Reference)

### جداول Distributor (الجديدة)

| الجدول | الوظيفة | Indexes |
|---|---|---|
| `distributor_pricing` | التسعيرة | `UNIQUE(tenantId, categoryValue, cardType)` |
| `distributor_customers` | البقالات والعملاء | `UNIQUE(tenantId, name)` |
| `distributor_sales` | فواتير المبيعات | `INDEX(tenantId, createdAt)`, `INDEX(tenantId, customerId)` |
| `distributor_payments` | تحصيل الذمم | `INDEX(tenantId, createdAt)` |
| `distributor_expenses` | المصروفات | `INDEX(tenantId, expenseDate)` |
| `distributor_capital` | حركات رأس المال | `INDEX(tenantId, createdAt)` |

---

## 4. Business Logic الحساسة

### 4.1 Credit Limit Enforcement
```typescript
// في DistributorSalesService.createSale()
async checkCreditLimit(tenantId, customerId, newCreditAmount, overrideByAdmin = false) {
  const customer = await prisma.distributorCustomer.findFirst({...});
  const currentDebt = await computeDebt(tenantId, customerId);
  
  if (currentDebt + newCreditAmount > customer.creditLimit && !overrideByAdmin) {
    throw new BadRequestException(
      `Credit limit exceeded. Current debt: ${currentDebt}, Limit: ${customer.creditLimit}`
    );
  }
  // إذا overrideByAdmin === true، يُسجَّل في Audit Log
}
```

### 4.2 Profit Calculation (Snapshot Pattern)
```typescript
// عند تسجيل البيعة، يُحفظ السعر الحالي مع السجل:
const { buyPrice, sellPrice } = await getPricing(tenantId, categoryValue, cardType);
const profit = (sellPrice - buyPrice) * quantity;
const totalAmount = sellPrice * quantity;
// هذا يضمن ثبات التقارير التاريخية حتى لو تغيرت التسعيرة لاحقاً
```

### 4.3 Cash Flow Formula
```
openingBalance  = SUM(capital.injections) - SUM(capital.withdrawals) [before period]
cashInflows     = cashSales + paymentsCollected + periodInjections
cashOutflows    = expenses + periodWithdrawals
netFlow         = cashInflows - cashOutflows
closingBalance  = openingBalance + netFlow
```

### 4.4 Receivables (الذمم)
```
customerDebt    = SUM(sales.totalAmount WHERE isCredit=true) - SUM(payments.amount)
totalReceivables= SUM(customerDebt) for all customers
```

### 4.5 Alert Thresholds
| التنبيه | المنطق |
|---|---|
| تجاوز الحد | `currentDebt > creditLimit` |
| اقتراب الحد | `currentDebt / creditLimit >= 0.8` |
| عميل متعثر | `isCredit=true AND lastPaymentDate < 30 days ago` |
| مخزون منخفض | `availableCards < 10 per category` |
| اشتراك قارب الانتهاء | `expiryDate < now + 7 days` |

---

## 5. DTOs الجديدة

### `CreateSaleDto`
```typescript
{
  customerId?: string;
  categoryValue: number;       // 100 | 200 | 250 | 300 | 500
  cardType: string;            // 'REGULAR' | 'PRO'
  quantity: number;
  receivedAmount: number;
  overrideCreditLimit?: boolean; // tenant_admin only
  overrideReason?: string;
  notes?: string;
}
```

### `DateRangeDto`
```typescript
{
  startDate?: string;  // ISO date
  endDate?: string;    // ISO date
  period?: 'today' | 'week' | 'month' | 'year' | 'custom';
}
```

### `CreateExpenseDto`
```typescript
{
  category: string;     // 'rent' | 'salary' | 'electricity' | ... | 'custom'
  customCategory?: string;
  amount: number;
  description?: string;
  expenseDate?: string;
}
```

### `RecordPaymentDto`
```typescript
{
  customerId: string;
  amount: number;
  paymentMethod?: 'cash' | 'wallet' | 'bank';
  notes?: string;
}
```

---

## 6. API Design (Distributor Module)

### Pricing APIs
| Method | Endpoint | Role | وصف |
|---|---|---|---|
| GET | `/distributor/pricing` | operator+ | جلب التسعيرة الحالية |
| PUT | `/distributor/pricing` | tenant_admin | تحديث سعر فئة معينة |
| POST | `/distributor/pricing/reset` | tenant_admin | إعادة التسعيرة للافتراضية |

### Customer APIs
| Method | Endpoint | Role | وصف |
|---|---|---|---|
| GET | `/distributor/customers` | operator+ | قائمة العملاء + بحث + Pagination |
| POST | `/distributor/customers` | tenant_admin | إنشاء عميل جديد |
| GET | `/distributor/customers/:id` | operator+ | تفاصيل عميل |
| PUT | `/distributor/customers/:id` | tenant_admin | تعديل عميل |
| DELETE | `/distributor/customers/:id` | tenant_admin | حذف عميل |
| GET | `/distributor/customers/:id/statement` | operator+ | كشف حساب تفصيلي |
| POST | `/distributor/customers/:id/payments` | operator+ | تسجيل دفعة |
| GET | `/distributor/customers/alerts` | operator+ | قائمة المتعثرين + تجاوزو الحد |

### Sales APIs
| Method | Endpoint | Role | وصف |
|---|---|---|---|
| POST | `/distributor/sales` | operator+ | تسجيل فاتورة بيع |
| GET | `/distributor/sales` | operator+ | قائمة الفواتير (Paginated + Filter) |
| GET | `/distributor/sales/:id` | operator+ | تفاصيل فاتورة |

### Finance APIs (Profits + Cash Flow + Expenses + Capital)
| Method | Endpoint | Role | وصف |
|---|---|---|---|
| GET | `/distributor/finance/dashboard` | operator+ | ملخص مالي شامل |
| GET | `/distributor/finance/profits` | operator+ | لوحة الأرباح (multi-period) |
| GET | `/distributor/finance/cash-flow` | operator+ | التدفق النقدي |
| GET | `/distributor/finance/receivables` | operator+ | الذمم الإجمالية |
| POST | `/distributor/finance/expenses` | operator+ | تسجيل مصروف |
| GET | `/distributor/finance/expenses` | operator+ | قائمة المصروفات |
| POST | `/distributor/finance/capital` | tenant_admin | حركة رأس مال |
| GET | `/distributor/finance/capital` | tenant_admin | سجل حركات رأس المال |
| GET | `/distributor/finance/top-customers` | operator+ | أفضل العملاء |
| GET | `/distributor/finance/top-products` | operator+ | أفضل الأصناف ربحاً |

### Dashboard APIs (System-Wide)
| Method | Endpoint | Role | وصف |
|---|---|---|---|
| GET | `/dashboard/overview` | operator+ | بطاقات إحصائية |
| GET | `/dashboard/charts` | operator+ | بيانات الرسوم البيانية |
| GET | `/dashboard/alerts` | operator+ | جميع التنبيهات المجمعة |
| GET | `/dashboard/recent-activity` | operator+ | آخر 10 عمليات |

---

## 7. خطة التقارير والتصدير

```
reports/
├── reports.controller.ts
├── reports.service.ts
├── exporters/
│   ├── pdf.exporter.ts      # باستخدام pdfmake أو puppeteer
│   └── excel.exporter.ts    # باستخدام exceljs
└── dto/
    └── report-filter.dto.ts
```

---

## 8. مبادئ البرمجة المعتمدة

1. **SOLID:** كل Service مسؤولة عن مجال واحد فقط.
2. **DRY:** منطق الحسابات (Date ranges, Aggregations) في helper methods مشتركة.
3. **Multi-Tenant:** كل `prisma.findMany()` يتضمن `where: { tenantId }` حتماً.
4. **Transactions:** أي عملية تكتب في جدولين أو أكثر تستخدم `prisma.$transaction()`.
5. **Swagger:** كل Controller يحتوي على `@ApiTags`, كل Method على `@ApiOperation` + `@ApiResponse`.
6. **Validation:** كل DTO يستخدم decorators من `class-validator`.
