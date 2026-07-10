# System Architecture (Kurotek v2.0 - SaaS Edition)

## 1. مقدمة (Introduction)
تُفصل هذه الوثيقة البنية التحتية المعمارية (System Architecture) لنظام Kurotek. وتشرح كيف تتفاعل المكونات المختلفة مع بعضها البعض لتحقيق أهداف النظام، بعد تحوله إلى منصة **SaaS (Software as a Service)** متعددة العملاء (Multi-Tenant).

## 2. الهدف (Goal)
الهدف هو توضيح "الصورة الكبرى" (Big Picture) باستخدام مفهوم (C4 Model). تم تصميم النظام الآن ليدعم آلاف العملاء المستقلين بشكل تام (Tenants)، مع لوحة تحكم عليا (Super Admin) مستقبلاً، ونظام اشتراكات وتفعيل آلي.

## 3. البنية المعمارية الكلية (High-Level Architecture)

### 3.1 النمط المعماري (Architectural Pattern)
يعتمد النظام على معمارية **Multi-Tenant Client-Server** باستخدام أسلوب **Logical Isolation**.
- **الموبايل (Tenant Client):** يتولى واجهة المستخدم لكل عميل، ويتعامل مع بيانات الـ Tenant الخاص به فقط، ويعمل كخادم SMS للإيداعات.
- **الخادم (SaaS Backend):** يتولى جميع العمليات التجارية المركزية، يفصل بيانات العملاء منطقياً عن طريق `tenant_id` في كل جدول.

### 3.2 رسم البنية التحتية (Infrastructure ASCII Diagram)
```text
                          +-------------------------+
                          |   External Wallets      |
                          +-----------+-------------+
                                      | SMS
                                      v
+-----------------------+     +-------------------------+
|   Tenant Flutter App  |     |   Android Service       |
| (Redesigned per Jaib) |     | (SMS Interceptor)       |
+-----------+-----------+     +-----------+-------------+
            |                             | HTTPS POST (JSON)
            | REST (JWT + Tenant ID Guard)|
            |                 +-------------------------+
            +---------------->|     Load Balancer       |
                              +-----------+-------------+
                                          |
                              +-----------v-------------+
                              |    Node.js (NestJS)     |
                              |  (Multi-Tenant Engine)  |
                              +-----------+-------------+
                                          | Prisma ORM
                              +-----------v-------------+
                              |   PostgreSQL Database   |
                              | (SaaS Isolated Tables)  |
                              +-------------------------+
```

## 4. المكونات الرئيسية الجديدة (New Core Components)

### 4.1 SaaS Management Layer
- **الاشتراكات (Subscriptions):** إدارة باقات العملاء (شهري، سنوي، مدى الحياة).
- **مفاتيح التفعيل (Activation Keys):** اكواد تصدر للموزعين لتباع للعملاء، عند استخدامها تُنشئ بيئة معزولة (Tenant) للعميل الجديد.

### 4.2 Data Isolation (فصل البيانات)
- كل طلب للـ API يمر عبر `TenantGuard` الذي يستخرج `tenant_id` من الـ JWT Token، ويتم حقنه تلقائياً في استعلامات `Prisma` لضمان استحالة وصول عميل لبيانات عميل آخر.

## 5. واجهات المستخدم (UI/UX based on Disgain)
- تم اعتماد التصميم الموجود في `Disgain` (Jaib Fintech) كمرجع بصري.
- **الألوان:** استخدام الألوان الاحترافية (Primary Red, Slate Backgrounds).
- **المكونات:** سيتم تحويل المكونات (Cards, BottomNav, Buttons) إلى Flutter Widgets تعكس نفس تجربة المستخدم (Dark/Light themes، حواف ناعمة، تفاعلات حركية).

## 6. التحسينات المستقبلية (Future Architectures)
- **Super Admin Dashboard:** تطبيق ويب (React/Next.js) للإدارة المركزية لمراقبة الاشتراكات، توليد كروت التفعيل، وإيقاف العملاء المخالفين.
