# Project Rules & Guidelines (Kurotek v2.0)

## 1. مقدمة (Introduction)
تحدد هذه الوثيقة **القواعد الذهبية (Golden Rules)** للعمل على مشروع Kurotek v2.0. تم وضع هذه القواعد لضمان جودة الكود، سهولة الصيانة، التوافق بين أعضاء الفريق (أو بين المطور والذكاء الاصطناعي)، ومنع تراكم الديون التقنية (Technical Debt).

## 2. الهدف (Goal)
الهدف من هذا الملف هو وضع مرجعية ثابتة غير قابلة للتفاوض لأي قرار يتم اتخاذه أثناء عملية التطوير، سواء كان يخص كتابة الكود، هيكلة الملفات، أو آلية التعامل مع قاعدة البيانات.

## 3. القاعدة الأولى والأهم (The Golden Rule)
> **Documentation First (الوثائق أولاً):**
> الوثائق (الموجودة في مجلد `docs/`) هي مصدر الحقيقة الوحيد (Single Source of Truth). يُمنع منعاً باتاً كتابة أي ميزة جديدة، أو تعديل هيكلة قاعدة البيانات، أو تغيير استجابة API دون تحديث الوثيقة الخاصة بها أولاً وأخذ الموافقة.

## 4. قواعد الـ Backend (NestJS & Prisma)
1. **No Any:** يُمنع استخدام النوع `any` في TypeScript بتاتاً. يجب تحديد نوع البيانات بدقة (Interfaces أو Types أو Classes).
2. **Modular Architecture:** كل ميزة يجب أن تكون داخل Module مستقل خاص بها (`AuthModule`, `CardsModule`, إلخ). يُمنع خلط منطق ميزتين في خدمة واحدة.
3. **Data Transfer Objects (DTOs):** يجب استخدام DTOs مدعومة بـ `class-validator` للتحقق من جميع المدخلات (Inputs) القادمة عبر الـ Controllers.
4. **Thin Controllers, Fat Services:** يجب ألا يحتوي الـ Controller على أي Business Logic. وظيفته الوحيدة هي استقبال الطلب وتمريره للـ Service، ثم إرجاع الاستجابة.
5. **Database Transactions:** أي عملية تقوم بتعديل أكثر من جدول (مثلاً: تغيير حالة كرت + تسجيل إيداع) يجب أن تتم داخل Prisma `$transaction` لتجنب البيانات غير المكتملة في حالة حدوث خطأ.
6. **Error Handling:** لا تقم بإرجاع رسائل أخطاء عشوائية. استخدم الفلاتر العالمية (Global Exception Filters) لتوحيد شكل الخطأ.

## 5. قواعد الـ Frontend (Flutter)
1. **Clean Architecture:** يجب الالتزام بطبقات (Data, Domain, Presentation) وعدم تخطيها.
2. **State Management:** استخدام `Riverpod` حصراً لجميع حالات التطبيق. يُمنع استخدام `setState` للعمليات المعقدة أو جلب البيانات.
3. **No Logic in UI:** لا يُسمح بكتابة أي منطق رياضي أو معالجة نصوص معقدة داخل الـ Widgets. الـ Widget للرسم (Rendering) فقط.
4. **Hardcoded Strings:** يُمنع كتابة النصوص الثابتة (Hardcoded Strings) داخل الـ UI. يجب أن تكون جميع النصوص في ملف الترجمة (Localization) أو ملفات الـ Constants.
5. **Responsive UI:** التطبيق يجب أن يتكيف مع أحجام الشاشات المختلفة باستخدام (LayoutBuilders أو MediaQuery)، ولا يجوز افتراض حجم شاشة ثابت.

## 6. قواعد العمل بالـ Git (Git Workflow)
- **Conventional Commits:** رسائل الـ Commits يجب أن تتبع المعيار القياسي مثل:
  - `feat: add automatic sms sending`
  - `fix: resolve crash on login screen`
  - `docs: update system architecture`
- يُمنع الدفع المباشر (Direct Push) إلى فرع `main`. جميع التعديلات يجب أن تتم عبر (Pull Requests) من فروع الميزات (`feature/*`).

## 7. حالات الخطأ (Error Cases & Violations)
- **مخالفة المعمارية:** إذا اكتشف المراجع (Code Reviewer) تجاوزاً للـ Clean Architecture، يُرفض الكود فوراً (Reject).
- **الاستعلامات الثقيلة:** الاستعلامات غير المفهرسة (Unindexed Queries) في PostgreSQL تعتبر خطأ كارثياً (Critical Error) يجب حله قبل الدمج.

## 8. القيود التقنية (Constraints)
- الكود يجب أن يجتاز جميع فحوصات الـ Linter بدون أي تحذيرات (0 Warnings).
- نسبة تغطية الاختبارات (Test Coverage) لا يجب أن تقل عن 80% للخدمات الحساسة (Business Logic).

## 9. التحسينات المستقبلية (Future Improvements)
- تطبيق نظام فحص تلقائي (CI/CD Pipeline) يقوم برفض الـ Pull Requests التي تحتوي على `any` أو لا تستوفي معايير التغطية.
- إضافة Code Formatters إجبارية تعمل (Pre-commit hooks) عبر Husky.
