@echo off
setlocal
chcp 65001 >nul

echo =======================================================
echo     رفع أحدث التعديلات إلى GitHub لبناء التطبيق
echo =======================================================
echo.

cd /d "C:\Users\Owner\Desktop\Kayansoft22"

echo [1] جاري تنظيف ملفات القفل (إن وجدت)...
if exist ".git\index.lock" del /F /Q ".git\index.lock" 2>nul

echo [2] جاري إضافة جميع الملفات والتعديلات...
git add .

echo [3] جاري تسجيل التعديلات (Commit)...
git commit -m "fix: Final production push. Resolved Android 14 permissions and Room KSP configurations for Codemagic Build"

echo [4] جاري رفع الكود إلى خوادم GitHub...
git push origin main

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ فشل الرفع! قد يكون هناك مشكلة في اتصال الإنترنت أو صلاحيات الـ Git.
    pause
    exit /b 1
)

echo.
echo =================================================
echo ✅ تم رفع الكود بنجاح!
echo ✅ يمكنك الآن التوجه إلى Codemagic.io 
echo ✅ سيقوم Codemagic الآن ببدء بناء التطبيق تلقائياً!
echo =================================================
echo.

pause
