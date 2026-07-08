# API Specification & Contract

## 1. مقدمة (Introduction)
تحدد هذه الوثيقة العقد المبرم (API Contract) بين الخادم السحابي (Backend) والتطبيقات الطرفية (Flutter Mobile App). الالتزام بهذا العقد هو شرط أساسي لضمان تدفق البيانات دون أخطاء.

## 2. الهدف (Goal)
توفير مرجع تفصيلي للمطورين لآلية استدعاء الـ APIs، أشكال البيانات المطلوبة (Request Body)، وأشكال الاستجابات المتوقعة (Response) في حالات النجاح والفشل.

## 3. المعايير العامة (Global Standards)
- **Base URL:** `https://api.kurotek.com/api/v1`
- **Format:** `application/json`
- **Authentication:** جميع الـ Endpoints (عدا `/auth/login`) تتطلب إرسال `Authorization: Bearer <token>` في رأس الطلب (Headers).
- **Standard Response Envelope:** يُرسل الخادم البيانات دائماً مُغلفة (Wrapped) في كائن موحد لتسهيل التعامل معها في Flutter:
```json
{
  "success": true,
  "data": { ... },
  "message": "Operation successful",
  "timestamp": "2026-06-29T10:00:00Z"
}
```

## 4. قائمة الـ Endpoints التفصيلية (Detailed Endpoints)

### 4.1 مصادقة المستخدم (Authentication)
#### `POST /auth/login`
- **الهدف:** تسجيل الدخول للمحل والحصول على Token.
- **Request Body:**
```json
{
  "serialKey": "SHOP-1234-5678",
  "password": "mySecurePassword"
}
```
- **Success Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbG...",
    "shop": { "id": "uuid", "name": "كروتك شوب" }
  },
  "message": "تم تسجيل الدخول بنجاح"
}
```
- **Error Response (401 Unauthorized):**
```json
{
  "success": false,
  "error": { "code": "INVALID_CREDENTIALS", "message": "بيانات الدخول غير صحيحة" }
}
```

### 4.2 معالجة رسائل الـ SMS
#### `POST /sms/process`
- **الهدف:** إرسال محتوى الـ SMS من الهاتف إلى السيرفر ليتم تحليله واستخراج الكرت إن وجد.
- **Request Body:**
```json
{
  "rawMessage": "استلمت 500 YER من 771234567 رصيدك 1500",
  "receivedAt": "2026-06-29T10:05:00Z"
}
```
- **Success Response (Auto Distribution):**
```json
{
  "success": true,
  "data": {
    "action": "SEND_SMS",
    "targetPhone": "771234567",
    "replyMessage": "تم استلام 500 ريال. كود الكرت: 998877",
    "depositId": "uuid-deposit"
  }
}
```
- **Success Response (Pending/Manual Approval):**
```json
{
  "success": true,
  "data": {
    "action": "PENDING",
    "pendingId": "uuid-pending",
    "message": "نفد المخزون أو الحساب غير معروف، تم تحويل الطلب للمراجعة اليدوية"
  }
}
```

### 4.3 إدارة الكروت (Cards Management)
#### `POST /cards/bulk`
- **الهدف:** رفع الكروت بالجملة عن طريق لصق نص.
- **Request Body:**
```json
{
  "categoryValue": 500,
  "codesBlock": "user1,pass1\nuser2,pass2\nuser3,pass3",
  "formatMode": "user_pass"
}
```
- **Success Response (201 Created):**
```json
{
  "success": true,
  "data": { "insertedCount": 3 },
  "message": "تم إضافة الكروت بنجاح"
}
```

## 5. أكواد الأخطاء الشائعة (Error Codes Dictionary)
| الرمز (Error Code) | المعنى | الحل المقترح (Action) |
|---|---|---|
| `UNAUTHORIZED` | الـ Token غير صالح أو منتهي. | توجيه المستخدم لشاشة تسجيل الدخول. |
| `VALIDATION_ERROR` | البيانات المرسلة غير مكتملة أو خاطئة. | تصحيح البيانات بناءً على رسالة الخطأ المرفقة. |
| `INSUFFICIENT_STOCK` | المخزون لا يكفي للصرف. | إظهار تنبيه للمشرف لرفع كروت جديدة. |
| `UNSUPPORTED_WALLET` | رسالة الـ SMS لا تطابق أي محفظة مدعومة. | تجاهل الرسالة، فهي ليست رسالة مالية. |

## 6. أفضل الممارسات والأمان (Security & Best Practices)
- **Idempotency:** يجب أن تدعم واجهات الدفع ومعالجة الـ SMS مبدأ عدم التكرار. إذا أرسل تطبيق الموبايل نفس رسالة الـ SMS مرتين بسبب ضعف الإنترنت، يجب على السيرفر التعرف عليها (عبر التوقيع أو الطابع الزمني) وعدم صرف كرتين لنفس الإيداع.
- **Pagination:** أي واجهة ترجع قائمة بيانات (مثل الإيداعات أو الكروت) يجب أن تحتوي على معاملات `page` و `limit` لتجنب سحب قاعدة البيانات بالكامل.

## 7. التحسينات المستقبلية (Future Improvements)
- إضافة توثيق Swagger / OpenAPI توليد تلقائي (Auto-generated) لجميع الـ Endpoints ليتمكن فريق الـ Frontend من استيراد النماذج (Types) دون كتابتها يدوياً.
