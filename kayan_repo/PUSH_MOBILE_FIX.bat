@echo off
cd /d "c:\Users\Owner\Desktop\Kayansoft22"

echo === رفع التعديل النهائي لمسار Mobile ===
git add codemagic.yaml
git commit -m "fix: run gradle build inside mobile directory"
git push origin main

echo.
echo ============================================
echo ✅ تم الرفع! 
echo اذهب إلى Codemagic واضغط Start new build
echo ============================================
pause
