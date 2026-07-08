@echo off
cd /d "c:\Users\Owner\Desktop\Kayansoft22"

echo === 1. جاري حفظ ملف gradle.properties ===
git add gradle.properties
git commit -m "fix: add android.useAndroidX=true and suppress warning"

echo === 2. جاري الرفع للـ GitHub... انتظر... ===
git push origin main

echo.
echo ============================================
echo ✅ تم الرفع بنجاح! 
echo الآن ارجع إلى Codemagic واضغط Start new build
echo ============================================
pause
