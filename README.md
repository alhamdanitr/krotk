# Kurotek / Dahshacards — نظام بيع كروت الإنترنت التلقائي

نظام لبيع وتوزيع كروت إنترنت/ميكروتك، يرصد تحويلات المحافظ اليمنية (جيب، جوالي، الكريمي، حسب، ون كاش، أم فلوس) عبر SMS، ويرسل الكرت تلقائيًا، مع نظام حاسبة موزع كامل (عملاء، ديون، مصاريف، رأس مال).

## هيكلة المستودع

المستودع مقسّم لـ**6 مشاريع مستقلة**، كل واحد بمجلده الخاص بالكامل (بدون تداخل أو مجلد أب مشترك)، ولكل مشروع وثيقة هندسية شاملة `ARCHITECTURE.md` بداخله:

| المجلد | النوع | الوصف |
|---|---|---|
| [`cards-app/`](cards-app/ARCHITECTURE.md) | Android (Kotlin) | تطبيق بيع الكروت التلقائي — النسخة الأولى |
| [`mobile-app/`](mobile-app/ARCHITECTURE.md) | Android (Kotlin) | نفس تطبيق الكروت + ميزات متقدمة (WebSocket فوري، تحليل SMS بالذكاء الاصطناعي، اختبارات آلية) |
| [`serial-admin/`](serial-admin/ARCHITECTURE.md) | Android (Kotlin) | تطبيق مستقل تمامًا لإدارة/إنشاء السيريالات |
| [`backend/`](backend/ARCHITECTURE.md) | NestJS + PostgreSQL | الخلفية الرسمية — نظام SaaS متعدد المستأجرين، محرك SMS، محاسبة الموزع، إدارة السيريالات |
| [`web-admin/`](web-admin/ARCHITECTURE.md) | Next.js | لوحة تحكم الويب، تتصل بـ`backend` |
| [`server/`](server/ARCHITECTURE.md) | Express | سيرفر التراخيص القديم — **الحي فعليًا حاليًا** على `kayan-licensing-server.onrender.com` |

افتح أي مجلد بأداته المناسبة مباشرة (Android Studio لمشاريع Kotlin، أو الطرفية لمشاريع Node) — كل مشروع يبني ويعمل بمعزل تام عن البقية.

## ⚠️ تحذيرات وقرارات معمارية معلّقة

1. **`mobile-app` و`cards-app` يشتركان بنفس `applicationId`** (`com.aistudio.dahshacards.uylxtb`) — يمنع تثبيتهما معًا أو نشرهما كتطبيقين منفصلين حاليًا. يجب تغيير أحدهما.
2. **يوجد نظاما تراخيص متوازيان**: `server/` (الحي فعليًا) و`backend/serial-manager` (جاهز، متوافق بالكامل، لكن غير متصل به تطبيقا الكروت بعد). راجع قسم "القرار المعماري المعلّق" بـ[`server/ARCHITECTURE.md`](server/ARCHITECTURE.md).
3. **حاسبة الموزع بالموبايل تعمل محليًا فقط** (Room DB)، بينما نسخة `web-admin`/`backend` متصلة بقاعدة بيانات مركزية — لا مزامنة بينهما حاليًا. راجع [`web-admin/ARCHITECTURE.md`](web-admin/ARCHITECTURE.md) قسم 6.
4. جميع الأسرار الحساسة (JWT/HMAC/بيانات المدير) أصبحت إجبارية عبر متغيرات بيئة فقط، بدون أي قيم افتراضية بالكود — كل من `server/.env.example` و`backend/.env.example` يوثّقان المتغيرات المطلوبة.

## التشغيل السريع

```bash
# تطبيق الموبايل الأكمل ميزات
cd mobile-app   # افتحه بـAndroid Studio مباشرة

# الباك-إند
cd backend && npm install --include=dev && npx prisma generate && npm run start:dev

# لوحة التحكم
cd web-admin && npm install -g pnpm && pnpm install && pnpm run dev

# تطبيق إدارة السيريالات
cd serial-admin   # افتحه بـAndroid Studio مباشرة

# السيرفر القديم (الحي فعليًا)
cd server && node api_server.js
```
