@echo off
cd /d "c:\Users\Owner\Desktop\Kayansoft22"

echo === 1. تنظيف Git (إزالة القفل إذا وجد) ===
if exist ".git\index.lock" (
    del /F /Q ".git\index.lock" 2>nul
)

echo === 2. جاري إضافة التعديل النهائي ===
git add codemagic.yaml
git commit -m "fix: force inject androidX properties in CI"

echo === 3. جاري الرفع... ===
git push origin main

echo.
echo ============================================
echo ✅ تم الرفع بنجاح! 
echo الآن ارجع إلى Codemagic واضغط Start new build
echo ============================================
pause
