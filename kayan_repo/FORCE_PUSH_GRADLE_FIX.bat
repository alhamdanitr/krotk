@echo off
cd /d "c:\Users\Owner\Desktop\Kayansoft22"

echo === 1. تنظيف Git (إزالة القفل) ===
if exist ".git\index.lock" (
    del /F /Q ".git\index.lock"
    echo تم إزالة القفل بنجاح!
)

echo === 2. جاري إضافة ملف gradle.properties ===
git add gradle.properties
git commit -m "fix: add android.useAndroidX=true and suppress warning"

echo === 3. جاري الرفع للـ GitHub... انتظر... ===
git push origin main

echo.
echo ============================================
echo ✅ تم الرفع بنجاح! 
echo الآن ارجع إلى Codemagic واضغط Start new build
echo ============================================
pause
