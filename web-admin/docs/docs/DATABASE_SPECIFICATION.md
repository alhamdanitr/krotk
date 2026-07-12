# Database Specification (SaaS Multi-Tenant Edition)

## 1. مقدمة (Introduction)
تُوثق هذه الورقة المواصفات التفصيلية لقاعدة بيانات نظام Kurotek SaaS. تم اعتماد نمط (Logical Isolation) لعزل بيانات كل عميل بشكل صارم.

## 2. الهدف (Goal)
توفير مرجع تقني نهائي لجداول SaaS الإدارية وجداول الـ Tenants، مع ضمان أمان البيانات وعدم تداخلها.

## 3. مخطط الكيانات والعلاقات (ERD Diagram - ASCII)
```text
[SaaS Management Layer]
+-------------------+       +-------------------+       +-------------------+
|      plans        |       | activation_keys   |       | subscriptions     |
+-------------------+       +-------------------+       +-------------------+
| id (PK)           |<----->| plan_id (FK)      |       | id (PK)           |
| name              |       | serial_key        |       | tenant_id (FK)    |
| duration_days     |       | activation_code   |       | plan_id (FK)      |
+-------------------+       +-------------------+       +-------------------+
                                      |
                                      v
[Tenant Layer]
+-------------------+       +-------------------+       +-------------------+
|      tenants      |       |      users        |       |     settings      |
+-------------------+       +-------------------+       +-------------------+
| id (PK)           |<----->| tenant_id (FK)    |       | tenant_id (FK, PK)|
| name              |       | username          |       | auto_send_sms     |
| activation_key_id |       | role              |       | ...               |
+-------------------+       +-------------------+       +-------------------+
        |
        | 1:N
        v
+-------------------+       +-------------------+       +-------------------+
|      cards        |       |    categories     |       |    deposits       |
+-------------------+       +-------------------+       +-------------------+
| id (PK)           |       | id (PK)           |       | id (PK)           |
| tenant_id (FK)    |<----->| tenant_id (FK)    |<----->| tenant_id (FK)    |
| category_id (FK)  |       | value             |       | amount            |
+-------------------+       +-------------------+       +-------------------+
```

## 4. قاموس البيانات التفصيلي (Data Dictionary)

### 4.1 طبقة الإدارة (SaaS Management)
- **`plans`:** خطط الاشتراك المتوفرة (شهري، سنوي، مدى الحياة)، يحتوي على السعر والمدة بالأيام.
- **`activation_keys`:** مفاتيح التفعيل المسبقة الدفع. تُربط بـ `plan_id`. عند استخدامها، يتغير `is_used` إلى true وتُربط بالـ `tenant`.
- **`subscriptions`:** عقد الاشتراك الفعلي لكل `tenant`، يحتوي على `start_date` و `expiry_date` وحالة الاشتراك.

### 4.2 طبقة العميل (Tenant Isolation)
- **`tenants`:** الكيان المرجعي (Parent) لأي حساب شبكة أو محل. (يحل محل جدول shops القديم).
- **`users`:** مستخدمو النظام. يحتوي على `tenant_id` (إلا إذا كان Super Admin فيكون null) و `role` لتحديد الصلاحيات.
- **`settings`:** إعدادات خاصة بكل `tenant`.

### 4.3 طبقة العمليات (Business Logic)
- **`cards`, `categories`, `deposits`, `transactions`, `pending_approvals`, `wallet_configs`, `sms_templates`:**
  جميع هذه الجداول تحتوي الآن على حقل إلزامي `tenant_id` كـ Foreign Key مربوط بـ `tenants`.
  *الفهارس الحيوية:* أي استعلام يجب أن يحتوي أولاً على `tenant_id` للاستفادة من الـ Composite Indexes (مثل `@@index([tenantId, categoryValue, isUsed])`).

## 5. المعاملات والقيود (Transactions & Constraints)
- **Row-Level Security / Isolation:** في الكود (Service Layer)، أي استعلام يُمرر له الـ `tenant_id` المستخرج من الـ JWT الخاص بالمستخدم، لضمان استحالة التعدي على بيانات Tenant آخر.
- **Cascading:** حذف الـ `tenant` (مثلاً من قِبل Super Admin) يؤدي إلى حذف جميع سجلاته تلقائياً لتوفير المساحة `ON DELETE CASCADE`.

## 6. التحسينات المستقبلية
- استخدام `Row-Level Security (RLS)` المدعوم أصلياً في PostgreSQL إذا احتجنا لطبقة أمان أقوى في مستوى قاعدة البيانات نفسها.
