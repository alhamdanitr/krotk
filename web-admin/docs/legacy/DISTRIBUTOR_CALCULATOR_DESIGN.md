# وثيقة تصميم موديول حاسبة الموزع (Distributor Calculator Module)
**التاريخ:** 2026-07-01 | **الحالة:** 📋 قيد المراجعة — بانتظار الموافقة

---

## 1. تحليل النظام الحالي (As-Is Analysis)

### 1.1 الملفات المصدرية
| الملف | الحجم | الوصف |
|---|---|---|
| `App.tsx` | 77KB / 1568 سطر | ملف واحد عملاق يحتوي كل شيء (Monolith Component) |
| `types.ts` | 889B | تعريفات TypeScript أساسية |
| `constants.ts` | 696B | أسعار الكروت الافتراضية |
| `utils/invoiceHelpers.ts` | 42KB | توليد PDF/PNG للفواتير + مشاركة WhatsApp/SMS |

### 1.2 الميزات الحالية المُستخرجة
1. **حاسبة مبيعات كروت** بنوعين: عادية (Regular) و برو (Pro)
2. **5 فئات كروت** لكل نوع (100, 200, 250, 300, 500) بأسعار مختلفة
3. **تسعير قابل للتخصيص** من الإعدادات مع إمكانية إعادة التعيين للافتراضي
4. **نظام حسابات البقالات (ShopAccount):** إنشاء/فتح حساب، تسجيل مبيعات تلقائي، تسجيل دفعات مسددة، حساب الرصيد المتبقي (الذمة)
5. **سجل مبيعات يومي** مع تقرير مالي يومي وعرض رسوم بيانية (أسبوعي/شهري)
6. **توليد فواتير PDF/PNG** بنمطين: ملون فاخر + حراري أبيض وأسود
7. **مشاركة عبر WhatsApp / SMS**
8. **نسخ احتياطي JSON** (تصدير/استيراد)
9. **صوت تنبيه** عند إتمام البيع
10. **وضع مظلم/فاتح**

### 1.3 آلية الحساب الحالية
```
سعر_الكرت_للموزع × الكمية = الإجمالي
المبلغ_المقبوض - الإجمالي = الفائض (المتبقي للزبون)
```
- لا يوجد حساب أرباح (الفرق بين سعر الشراء وسعر البيع)
- لا يوجد حساب مصروفات أو رأس مال
- لا يوجد تدفق نقدي أو تقارير مالية حقيقية

### 1.4 المشاكل والقيود المكتشفة

| # | المشكلة | الخطورة |
|---|---|---|
| 1 | **ملف واحد عملاق** (1568 سطر) — مستحيل الصيانة | 🔴 حرجة |
| 2 | **تخزين محلي فقط (localStorage)** — فقدان البيانات عند مسح المتصفح | 🔴 حرجة |
| 3 | **لا يوجد Multi-Tenancy** — لا يصلح لمنصة SaaS | 🔴 حرجة |
| 4 | **لا يوجد حساب أرباح حقيقي** — لا فرق بين سعر الشراء والبيع | 🟡 عالية |
| 5 | **لا يوجد نظام مصروفات أو رأس مال** | 🟡 عالية |
| 6 | **لا يوجد نظام ديون/ذمم متقدم** — فقط رصيد متبقي بسيط | 🟡 عالية |
| 7 | **IDs عشوائية** (`Math.random()`) بدلاً من UUID | 🟡 متوسطة |
| 8 | **لا يوجد تحقق من المدخلات** على مستوى الخادم | 🟡 متوسطة |
| 9 | **`@ts-ignore`** مستخدم في عدة أماكن | 🟢 منخفضة |
| 10 | **لا يوجد اختبارات** | 🟢 منخفضة |

---

## 2. التصميم الجديد (To-Be Architecture)

### 2.1 الرؤية
تحويل الحاسبة من تطبيق محلي بسيط إلى **مركز إدارة مالي شامل للموزع** داخل منصة Kurotek SaaS — يشمل: المبيعات، الأرباح، المصروفات، الديون، التحصيل، والتدفق النقدي.

### 2.2 المبادئ الهندسية
- **Tenant Isolation**: كل بيانات الموزع معزولة بـ `tenantId`
- **Server-First**: جميع البيانات في PostgreSQL عبر Prisma (لا localStorage)
- **Clean Architecture**: كل وظيفة في Module مستقل
- **API-Ready for Flutter**: جميع الـ APIs مصممة لتطبيق الموبايل

### 2.3 الهيكل المعماري الجديد
```
src/
├── distributor/
│   ├── distributor.module.ts
│   ├── distributor.controller.ts        # Sales Calculator APIs
│   ├── distributor.service.ts           # Business Logic
│   ├── dto/
│   │   ├── create-sale.dto.ts
│   │   ├── record-payment.dto.ts
│   │   ├── create-expense.dto.ts
│   │   ├── create-customer.dto.ts
│   │   └── update-pricing.dto.ts
│   ├── customers/
│   │   ├── customers.controller.ts      # Customer (Shop) Management
│   │   └── customers.service.ts
│   ├── finance/
│   │   ├── finance.controller.ts        # Profits, Expenses, Cash Flow
│   │   └── finance.service.ts
│   └── pricing/
│       ├── pricing.controller.ts        # Pricing Management
│       └── pricing.service.ts
```

---

## 3. قاعدة البيانات (Database Schema)

### 3.1 جداول جديدة مطلوبة

#### `distributor_pricing` — تسعيرة الموزع
```prisma
model DistributorPricing {
  id             String   @id @default(uuid())
  tenantId       String   @map("tenant_id")
  categoryValue  Int      @map("category_value")    // 100, 200, 250, 300, 500
  cardType       String   @map("card_type")          // REGULAR, PRO
  buyPrice       Int      @map("buy_price")          // سعر الشراء من المورد
  sellPrice      Int      @map("sell_price")         // سعر البيع للبقالة
  isActive       Boolean  @default(true) @map("is_active")
  
  tenant         Tenant   @relation(fields: [tenantId], references: [id], onDelete: Cascade)
  
  @@unique([tenantId, categoryValue, cardType])
  @@map("distributor_pricing")
}
```

#### `distributor_customers` — عملاء الموزع (البقالات)
```prisma
model DistributorCustomer {
  id             String   @id @default(uuid())
  tenantId       String   @map("tenant_id")
  name           String   @db.VarChar(150)
  phone          String?  @db.VarChar(50)
  address        String?  @db.Text
  creditLimit    Int      @default(0) @map("credit_limit")  // حد الائتمان
  isActive       Boolean  @default(true) @map("is_active")
  createdAt      DateTime @default(now()) @map("created_at")
  
  tenant         Tenant   @relation(fields: [tenantId], references: [id], onDelete: Cascade)
  sales          DistributorSale[]
  payments       DistributorPayment[]
  
  @@unique([tenantId, name])
  @@map("distributor_customers")
}
```

#### `distributor_sales` — مبيعات الموزع
```prisma
model DistributorSale {
  id             String   @id @default(uuid())
  tenantId       String   @map("tenant_id")
  customerId     String?  @map("customer_id")
  categoryValue  Int      @map("category_value")
  cardType       String   @map("card_type")
  quantity       Int
  buyPrice       Int      @map("buy_price")        // سعر الشراء وقت البيع
  sellPrice      Int      @map("sell_price")       // سعر البيع وقت البيع
  totalAmount    Int      @map("total_amount")     // sellPrice × quantity
  profit         Int                                // (sellPrice - buyPrice) × quantity
  receivedAmount Int      @default(0) @map("received_amount") // المبلغ المقبوض
  isCredit       Boolean  @default(false) @map("is_credit")   // هل هي ذمة؟
  notes          String?  @db.Text
  createdAt      DateTime @default(now()) @map("created_at")
  
  tenant         Tenant   @relation(fields: [tenantId], references: [id], onDelete: Cascade)
  customer       DistributorCustomer? @relation(fields: [customerId], references: [id])
  
  @@index([tenantId, createdAt])
  @@index([tenantId, customerId])
  @@map("distributor_sales")
}
```

#### `distributor_payments` — المدفوعات والتحصيل
```prisma
model DistributorPayment {
  id             String   @id @default(uuid())
  tenantId       String   @map("tenant_id")
  customerId     String   @map("customer_id")
  amount         Int
  paymentMethod  String   @default("cash") @map("payment_method") @db.VarChar(30)
  notes          String?  @db.Text
  createdAt      DateTime @default(now()) @map("created_at")
  
  tenant         Tenant   @relation(fields: [tenantId], references: [id], onDelete: Cascade)
  customer       DistributorCustomer @relation(fields: [customerId], references: [id])
  
  @@index([tenantId, createdAt])
  @@map("distributor_payments")
}
```

#### `distributor_expenses` — مصروفات الموزع
```prisma
model DistributorExpense {
  id             String   @id @default(uuid())
  tenantId       String   @map("tenant_id")
  category       String   @db.VarChar(50)  // وقود، إيجار، رواتب، صيانة، أخرى
  amount         Int
  description    String?  @db.Text
  expenseDate    DateTime @default(now()) @map("expense_date")
  createdAt      DateTime @default(now()) @map("created_at")
  
  tenant         Tenant   @relation(fields: [tenantId], references: [id], onDelete: Cascade)
  
  @@index([tenantId, expenseDate])
  @@map("distributor_expenses")
}
```

#### `distributor_capital` — رأس المال
```prisma
model DistributorCapital {
  id             String   @id @default(uuid())
  tenantId       String   @map("tenant_id")
  type           String   @db.VarChar(30) // injection (إيداع), withdrawal (سحب)
  amount         Int
  description    String?  @db.Text
  createdAt      DateTime @default(now()) @map("created_at")
  
  tenant         Tenant   @relation(fields: [tenantId], references: [id], onDelete: Cascade)
  
  @@index([tenantId, createdAt])
  @@map("distributor_capital")
}
```

---

## 4. الـ APIs المطلوبة

### 4.1 Sales — المبيعات (`/distributor/sales`)
| Method | Endpoint | الوصف |
|---|---|---|
| `POST` | `/distributor/sales` | تسجيل عملية بيع جديدة (فاتورة) |
| `GET` | `/distributor/sales` | قائمة المبيعات مع Pagination/Search/Filter |
| `GET` | `/distributor/sales/:id` | تفاصيل عملية بيع محددة |

### 4.2 Customers — العملاء/البقالات (`/distributor/customers`)
| Method | Endpoint | الوصف |
|---|---|---|
| `POST` | `/distributor/customers` | إنشاء عميل جديد (بقالة) |
| `GET` | `/distributor/customers` | قائمة العملاء مع البحث |
| `GET` | `/distributor/customers/:id` | تفاصيل عميل + كشف حساب |
| `PUT` | `/distributor/customers/:id` | تعديل بيانات عميل |
| `DELETE` | `/distributor/customers/:id` | حذف عميل |
| `GET` | `/distributor/customers/:id/statement` | كشف حساب تفصيلي |
| `POST` | `/distributor/customers/:id/payments` | تسجيل دفعة مسددة |

### 4.3 Finance — المالية (`/distributor/finance`)
| Method | Endpoint | الوصف |
|---|---|---|
| `GET` | `/distributor/finance/dashboard` | ملخص مالي شامل (اليوم + الشهر) |
| `GET` | `/distributor/finance/profits` | تقرير الأرباح (يومي/شهري/سنوي) |
| `GET` | `/distributor/finance/cash-flow` | التدفق النقدي |
| `GET` | `/distributor/finance/receivables` | إجمالي الذمم والديون المستحقة |
| `POST` | `/distributor/finance/expenses` | تسجيل مصروف |
| `GET` | `/distributor/finance/expenses` | قائمة المصروفات |
| `POST` | `/distributor/finance/capital` | تسجيل حركة رأس مال (إيداع/سحب) |
| `GET` | `/distributor/finance/capital` | سجل حركات رأس المال |

### 4.4 Pricing — التسعير (`/distributor/pricing`)
| Method | Endpoint | الوصف |
|---|---|---|
| `GET` | `/distributor/pricing` | جلب جميع الأسعار الحالية |
| `PUT` | `/distributor/pricing` | تحديث الأسعار (batch update) |
| `POST` | `/distributor/pricing/reset` | إعادة الأسعار للافتراضي |

---

## 5. شاشات Flutter المطلوبة

### 5.1 الشاشات الرئيسية (5 تبويبات)
| # | الشاشة | الوصف |
|---|---|---|
| 1 | **الحاسبة** | اختيار الفئات والكميات + المبلغ المقبوض + اعتماد البيع |
| 2 | **البقالات** | قائمة العملاء + البحث + فتح كشف حساب + تسجيل دفعة |
| 3 | **المالية** | لوحة مالية: أرباح، مصروفات، تدفق نقدي، رأس مال |
| 4 | **التقارير** | تقارير يومية/شهرية/سنوية + رسوم بيانية |
| 5 | **الإعدادات** | تعديل الأسعار + النسخ الاحتياطي + الصوت + الطابعة |

### 5.2 الشاشات الفرعية (Modals / Pages)
| الشاشة | الوصف |
|---|---|
| شاشة تأكيد البيع | ملخص الفاتورة + أزرار مشاركة (WhatsApp/SMS/PDF/PNG) |
| شاشة كشف حساب عميل | الحركات المالية + المبيعات + الدفعات + الرصيد |
| شاشة تسجيل دفعة | إدخال مبلغ + ملاحظات |
| شاشة تسجيل مصروف | الفئة + المبلغ + الوصف |
| شاشة حركة رأس المال | النوع (إيداع/سحب) + المبلغ + الوصف |

---

## 6. نظام الحسابات المالية (Business Logic)

### 6.1 حساب الأرباح
```
ربح_الكرت_الواحد = سعر_البيع - سعر_الشراء
ربح_العملية = ربح_الكرت_الواحد × الكمية

الأرباح_اليومية = مجموع أرباح جميع عمليات البيع في اليوم
الأرباح_الشهرية = مجموع أرباح جميع عمليات البيع في الشهر
الأرباح_السنوية = مجموع أرباح جميع عمليات البيع في السنة
```

### 6.2 صافي الأرباح
```
صافي_الربح = إجمالي_الأرباح - إجمالي_المصروفات
```

### 6.3 رأس المال
```
رأس_المال_الحالي = مجموع_الإيداعات - مجموع_السحوبات
```

### 6.4 الذمم والديون (Receivables)
```
ذمة_العميل = إجمالي_المبيعات_الآجلة - إجمالي_المدفوعات
إجمالي_الذمم = مجموع ذمم جميع العملاء
```

### 6.5 التحصيل (Collection Rate)
```
نسبة_التحصيل = (إجمالي_المدفوعات / إجمالي_المبيعات_الآجلة) × 100
```

### 6.6 التدفق النقدي (Cash Flow)
```
التدفق_الداخل = المبيعات_النقدية + المدفوعات_المحصلة + إيداعات_رأس_المال
التدفق_الخارج = المصروفات + سحوبات_رأس_المال
صافي_التدفق = التدفق_الداخل - التدفق_الخارج
```

---

## 7. أدوات سريعة للموزع (Quick Tools)

| الأداة | الوصف |
|---|---|
| 🧮 **حاسبة سريعة** | إدخال فئة + كمية = المجموع فوراً |
| 📊 **ربح اليوم** | بطاقة سريعة تظهر صافي ربح اليوم |
| 💰 **تحصيل سريع** | تسجيل دفعة لعميل من الشاشة الرئيسية |
| 📋 **آخر 5 فواتير** | عرض آخر 5 عمليات بيع للمراجعة السريعة |
| 🔔 **تنبيه الذمم** | تنبيه عند تجاوز عميل لحد الائتمان |
| 📤 **تقرير سريع** | توليد ومشاركة تقرير اليوم بضغطة واحدة |

---

## 8. ميزات احترافية مقترحة

| # | الميزة | الوصف |
|---|---|---|
| 1 | **تصنيف المصروفات** | وقود، إيجار، رواتب، صيانة، متنوع — مع رسوم بيانية |
| 2 | **حد ائتمان للعميل** | منع البيع الآجل عند تجاوز الحد (تنبيه أو حجب) |
| 3 | **تقارير مقارنة** | مقارنة أداء الشهر الحالي بالسابق |
| 4 | **أفضل عملاء** | ترتيب العملاء حسب حجم المبيعات/الالتزام بالسداد |
| 5 | **تسعير متعدد** | إمكانية تسعير مختلف لكل عميل (VIP Pricing) |
| 6 | **تصدير Excel** | تصدير التقارير كملفات Excel |
| 7 | **إشعارات ذكية** | تذكير بالذمم المتأخرة + تنبيه نفاد المخزون |
| 8 | **سجل التعديلات** | Audit Log لكل تعديل في الأسعار أو الحسابات |

---

## 9. خطة التنفيذ (Implementation Roadmap)

| المرحلة | المحتوى | التقدير |
|---|---|---|
| **9.1** | إضافة الجداول لـ `schema.prisma` + `prisma db push` | 30 دقيقة |
| **9.2** | بناء `DistributorModule` + Sales APIs + Pricing APIs | 1 ساعة |
| **9.3** | بناء `CustomersModule` + كشف الحساب + الدفعات | 45 دقيقة |
| **9.4** | بناء `FinanceModule` + الأرباح + المصروفات + التدفق النقدي | 1 ساعة |
| **9.5** | ربط الكل في `AppModule` + Build + Test | 30 دقيقة |
| **9.6** | توثيق `PHASE_6_DISTRIBUTOR_REPORT.md` | 15 دقيقة |

**الإجمالي المقدر:** ~4 ساعات عمل

---

## 10. ملاحظات هندسية

> [!IMPORTANT]
> - جميع البيانات معزولة بـ `tenantId` — لا يمكن لموزع رؤية بيانات موزع آخر
> - الأسعار تُحفظ مع كل عملية بيع (Snapshot) لضمان دقة التقارير التاريخية
> - حساب الأرباح يعتمد على الفرق بين `buyPrice` و `sellPrice` وليس نسبة ثابتة
> - نظام الذمم يدعم حد ائتمان قابل للتخصيص لكل عميل

> [!WARNING]
> **بانتظار موافقتك لبدء التنفيذ.** لن يتم كتابة أي كود قبل اعتمادك لهذه الوثيقة.
