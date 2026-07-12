# Web Admin — لوحة تحكم الويب (Next.js)

## 1. الغرض من المشروع

لوحة تحكم ويب لصاحب النظام/الموزع، تعرض نفس البيانات والوظائف المتاحة بتطبيق الموبايل (كروت، عملاء، محافظ، تقارير، إعدادات، حاسبة الموزع) لكن بواجهة سطح مكتب/متصفح. تتصل مباشرة بـ[`backend`](../backend) (NestJS).

## 2. الأدوات والتقنيات المستخدمة

| الفئة | الأداة |
|---|---|
| إطار العمل | Next.js 15 (App Router) |
| اللغة | TypeScript |
| الواجهة | React 19 + Tailwind CSS + shadcn/ui (`components.json`) |
| الأيقونات | lucide-react |
| جلب البيانات وتخزينها مؤقتًا | TanStack React Query |
| الثيم | next-themes (دعم الوضع الداكن) |
| إدارة الحزم | pnpm |

## 3. هيكلة الملفات

```
web-admin/
├── app/                              Next.js App Router
│   ├── page.tsx                        الصفحة الرئيسية (Redirect حسب حالة الدخول)
│   ├── login/page.tsx                   تسجيل الدخول
│   └── dashboard/
│       ├── layout.tsx                    القالب العام للوحة (Sidebar + Header)
│       ├── page.tsx                      نظرة عامة/إحصائيات
│       ├── cards/page.tsx                إدارة الكروت
│       ├── wallets/page.tsx               إعدادات المحافظ الإلكترونية
│       ├── pending/page.tsx               الموافقات المعلّقة
│       ├── reports/page.tsx               التقارير
│       ├── settings/page.tsx              الإعدادات العامة
│       └── distributor/                   حاسبة الموزع (نفس مفهوم DistributorSystemScreen بالموبايل)
│           ├── layout.tsx, page.tsx
│           ├── customers/page.tsx          العملاء والديون
│           ├── sales/page.tsx               عمليات البيع
│           ├── finance/page.tsx              المصاريف وحركة رأس المال
│           └── pricing/page.tsx              تسعير مخصص لكل عميل موزع
├── components/
│   ├── ui/                             مكوّنات shadcn/ui الأساسية (Button, Card, Dialog...)
│   └── layout/                          Sidebar, Header, وعناصر القالب العام
├── hooks/                             use-auth, use-dashboard, use-distributor, use-backups, use-modules
├── services/                           طبقة نداءات API منظمة (auth/dashboard/distributor/modules.service.ts)
├── lib/
│   ├── api-client.ts                    عميل HTTP مركزي (Base URL + إرفاق التوكن تلقائيًا)
│   └── utils.ts
├── types/index.ts                     أنواع TypeScript المشتركة
└── docs/legacy/                       وثائق تصميم سابقة (SRS, SDD, Master Blueprint...) — مرجع تاريخي فقط
```

## 4. طريقة سير العمل (Workflow)

1. **تسجيل الدخول**: `app/login` يستخدم `services/auth.service.ts` → `lib/api-client.ts` يرسل الطلب لـ`backend` (`/auth/login`)، ويحفظ التوكن (عبر `use-auth.tsx`).
2. **كل الصفحات داخل `dashboard/`** محمية: تتحقق من وجود جلسة صالحة عبر hook `use-auth`، وتُعيد التوجيه لـ`/login` إن لم تكن موجودة.
3. **جلب البيانات**: كل صفحة تستخدم hook مخصص (مثل `use-distributor`) مبني فوق React Query، يستدعي دالة من `services/*.service.ts`، التي بدورها تمر عبر `api-client.ts` (يُرفق تلقائيًا `Authorization: Bearer <token>` ويُعرِّف `NEXT_PUBLIC_API_URL`).
4. **حاسبة الموزع بالويب** تعكس تمامًا نفس المفاهيم الموجودة بتطبيق الموبايل (`DistributorSystemScreen.kt`)، لكنها تتصل بموديول `distributor` الحقيقي بـ`backend` (قاعدة بيانات مركزية)، بعكس نسخة الموبايل الحالية التي تعمل محليًا فقط (Room) — راجع الملاحظة بالقسم 6.

## 5. طريقة التشغيل

```bash
cd web-admin
npm install -g pnpm
pnpm install
cp .env.example .env.local   # واضبط NEXT_PUBLIC_API_URL على رابط backend
pnpm run dev
```
يفتح على `http://localhost:3000` افتراضيًا (نفس منفذ `backend` — عدّل أحدهما محليًا لتفادي التعارض).

### النشر (Render)
خدمة Web Service منفصلة، `Root Directory: web-admin`، تحصل على رابط `backend` تلقائيًا عبر `NEXT_PUBLIC_API_URL`.

## 6. ملاحظة معمارية مهمة
هذه اللوحة **متصلة فعليًا** بموديول `distributor` بـ`backend` (بيانات مركزية بقاعدة PostgreSQL)، بينما حاسبة الموزع بتطبيقي الموبايل (`cards-app`/`mobile-app`) **لا تزال تعمل محليًا فقط** (Room DB بدون مزامنة سحابية لهذا الجزء تحديدًا). يعني حاليًا: بيانات الموزع المُدخلة من الموبايل **لا تظهر** بهذه اللوحة والعكس صحيح، إلى أن تُربط شاشات الموبايل بنفس نقاط `backend/src/distributor/*` بدل الاعتماد على `CardRepository.kt` المحلي فقط.
