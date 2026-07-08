@echo off
cd /d "c:\Users\Owner\Desktop\Kayansoft22"

echo === 1. جاري تنظيف الكاش (لتفادي أي أخطاء قديمة) ===
if exist ".next" rmdir /S /Q ".next"

echo === 2. جاري تثبيت الحزم المطلوبة (يرجى الانتظار قليلاً...) ===
call npm install --legacy-peer-deps

echo === 3. جاري تشغيل المتصفح ===
start "" /B cmd /c "ping localhost -n 5 >nul && start chrome http://localhost:3000"

echo === 4. جاري تشغيل لوحة التحكم... ===
call npm run dev

pause
