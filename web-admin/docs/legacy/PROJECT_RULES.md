KUROTEK PROJECT MASTER INSTRUCTIONS
الإصدار 1.0
1. الهدف
بناء نظام احترافي لإدارة وبيع كروت الشبكات باستخدام:
Backend: Node.js + NestJS
Database: PostgreSQL
ORM: Prisma
Hosting: Railway
Frontend: Flutter
Architecture: Clean Architecture + Feature First
API: REST API
Authentication: JWT
Source Control: GitHub
2. القواعد الأساسية
قبل كتابة أي كود يجب دائماً:
1- تحليل المتطلبات.
2- تحليل تأثير التعديل على المشروع.
3- تحديث الوثائق.
4- انتظار موافقة صاحب المشروع.
5- بعدها يبدأ التنفيذ.
لا يسمح بكتابة أي كود مباشرة.
3. ممنوعات المشروع
ممنوع:
تغيير المعمارية.
كسر Clean Architecture.
استخدام any في TypeScript.
استخدام Hardcoded Values.
تكرار الكود.
كتابة Business Logic داخل UI.
كتابة SQL مباشر إلا عند الضرورة.
إضافة Packages بدون مبرر.
4. معمارية المشروع
Backend
NestJS
Modules
Services
Repositories
DTOs
Guards
Interceptors
Prisma
PostgreSQL
Railway
Flutter
Clean Architecture
Feature First
Riverpod
GoRouter
Dio
Secure Storage
Material 3
RTL
5. طريقة التنفيذ
يتم التنفيذ عن طريق:
Micro Tasks
وليس:
Phase كاملة.
كل Task مستقلة.
كل Task قابلة للاختبار.
كل Task تنتهي بالكامل قبل الانتقال لغيرها.
6. عند إضافة ميزة جديدة
أي ميزة جديدة يجب أن تمر بالمراحل التالية:
Impact Analysis
Business Analysis
Database Analysis
API Analysis
Flutter Analysis
Testing Analysis
Documentation Update
Approval
Implementation
7. إذا تم تعديل ميزة
يجب:
تحديث Blueprint
تحديث Database
تحديث APIs
تحديث Documentation
تحديث Roadmap
ثم التنفيذ.
8. واجهات Flutter
الواجهات ليست نسخة من تطبيق جيب.
ولكن مستوحاة من جودة تطبيقات المحافظ المالية.
يجب أن تكون:
Modern
Premium
Material 3
Responsive
RTL
Clean
Professional
Pixel Perfect
Reusable Components
Design System كامل.
9. عند تصميم الواجهات
يجب الحفاظ على:
الألوان
الأيقونات
التبويبات
التخطيط
المسافات
الحجم
الـ Typography
Navigation
Cards
Bottom Navigation
Dialogs
Bottom Sheets
App Bars
Transitions
بدقة عالية.
10. تصميم النظام
كل Feature تحتوي:
Presentation
Domain
Data
Repository
Datasource
Models
Entities
UseCases
Providers
Widgets
Screens
11. Backend
كل Module يحتوي:
Controller
Service
Repository
DTO
Validation
Tests
Documentation
12. قاعدة البيانات
PostgreSQL
Prisma
Migration لكل تعديل.
لا يتم تعديل Schema مباشرة.
13. API
REST API
Versioning
JWT
Validation
Swagger
Standard Response
Standard Error
Pagination
Filtering
Sorting
14. الجودة
الكود يجب أن يكون:
Production Ready
Scalable
Maintainable
Secure
Modular
Reusable
Documented
Testable
15. الاختبارات
كل Feature تحتوي:
Unit Tests
Integration Tests
Acceptance Tests
16. Git
main
develop
feature/*
fix/*
Conventional Commits
17. Railway
Deployment
Environment Variables
Auto Deploy
Docker
PostgreSQL
Backups
18. Flutter
Dark Mode
Light Mode
Material 3
Responsive
Animations
Accessibility
Riverpod
GoRouter
Dio
19. طريقة العمل مع Claude
Claude لا يكتب الكود مباشرة.
دائماً:
Analysis
Architecture
Planning
Approval
Implementation
Testing
Documentation
20. أي تعديل مستقبلي
أي تعديل يجب أن يمر بـ:
Change Request
Impact Analysis
Approval
Documentation Update
Implementation
Testing
21. معيار الجودة
أي كود يتم إنشاؤه يجب أن يكون:
Production Ready.
Clean Code.
SOLID Principles.
DRY.
KISS.
Feature First.
Clean Architecture.
قابل للتوسع.
قابل للاختبار.
موثق بالكامل.
22. الهدف النهائي
إنشاء نظام احترافي قابل للنشر على:
Google Play.
Railway.
PostgreSQL.
GitHub.
مع إمكانية إضافة ميزات جديدة مستقبلًا دون الحاجة إلى إعادة هيكلة المشروع.
