@echo off
cd /d "c:\Users\Owner\Desktop\Kayansoft22"

echo === 1. تنظيف الـ Git ===
if exist ".git\index.lock" del /F /Q ".git\index.lock" 2>nul

echo === 2. رفع إصلاح مشكلة الإغلاق الفوري للتطبيق ===
git add mobile\app\src\main\AndroidManifest.xml
git add mobile\gradle\libs.versions.toml
git commit -m "fix: App instant crash on launch (Added POST_NOTIFICATIONS, fixed KSP/Kotlin versions for Room code generation)"
git push origin main

echo.
echo ============================================
echo ✅ تم رفع الإصلاحات النهائية التي تمنع إغلاق التطبيق!
echo يرجى الذهاب إلى Codemagic وبدء بناء أخير.
echo ============================================
pause
