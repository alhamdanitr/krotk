# Kurotek / Dahshacards — نظام بيع كروت الإنترنت التلقائي

نظام لبيع وتوزيع كروت إنترنت/ميكروتك، يرصد تحويلات المحافظ اليمنية (جيب، جوالي، الكريمي، حسب، ون كاش، أم فلوس) عبر SMS، ويرسل الكرت تلقائيًا، مع نظام حاسبة موزع كامل (عملاء، ديون، مصاريف، رأس مال).

## هيكلة المستودع

```
app/            تطبيق الأندرويد الرئيسي (Jetpack Compose) — applicationId: com.aistudio.dahshacards
kayan_repo/     المنصة الكاملة:
  ├── mobile/     نفس تطبيق app/ + ميزات متقدمة إضافية (مزامنة WebSocket فورية،
  │               تحليل SMS بالذكاء الاصطناعي عبر Gemini، اختبارات Unit/UI)
  ├── backend/    الخلفية الرسمية (NestJS + Prisma) — نظام SaaS متعدد المستأجرين،
  │               يشمل serial-manager (إنشاء/تفعيل السيريالات)، محرك SMS، المحاسبة، إلخ
  └── app/        لوحة تحكم الويب (Next.js) — dashboard/cards/wallets/pending/reports/settings
server/         السيرفر القديم (Express) المستخدم حاليًا فعليًا للتفعيل/المزامنة
                على https://kayan-licensing-server.onrender.com — ⚠️ يحتاج إصلاح أمني عاجل
                (راجع قسم التحذيرات الأمنية أدناه) قبل الاعتماد عليه لفترة أطول
```

`kurotek/` (الوحدة القديمة الأولى) تم حذفها بعد التأكد من نقل كل ميزاتها بالكامل (30 ملف Kotlin،
الشاشات، الألوان، التصاميم) إلى `app/` و`kayan_repo/mobile` بشكل مطابق ومؤكد.

## ⚠️ تحذيرات أمنية عاجلة (لم تُعالج بعد)

1. `server/api_server.js`: يحتوي على باب خلفي صريح (`relaxed_access_authorized_by_owner`) ومفاتيح
   JWT/HMAC افتراضية مكتوبة بالكود. هذا هو السيرفر الفعلي الذي يعتمد عليه التطبيق حاليًا للتفعيل.
2. `kayan_repo/backend` (`serial-manager.controller.ts`): كل نقاط إدارة السيريالات
   (`create`, `delete`, `toggle`, `reset`, `list`, `logs`) مُعرّفة `@Public()` بدون أي مصادقة.
3. لا توجد بعد صفحة واجهة (UI) لإنشاء/إدارة السيريالات داخل `kayan_repo/app` — الأداة الوحيدة
   العاملة حاليًا هي `server/admin_panel.html` القديمة.

## التشغيل محليًا

**المتطلبات:** [Android Studio](https://developer.android.com/studio)، Node.js، pnpm (للـ backend)

1. افتح `kayan_repo/mobile` في Android Studio لتشغيل تطبيق الأندرويد الكامل بكل الميزات
2. لتشغيل الباك-إند: `cd kayan_repo/backend && pnpm install && pnpm run start:dev`
3. لتشغيل لوحة التحكم: `cd kayan_repo/app && npm install && npm run dev`

