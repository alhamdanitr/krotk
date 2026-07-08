@echo off
cd /d "c:\Users\Owner\Desktop\Kayansoft22"

echo === 1. تنظيف Git ===
if exist ".git\index.lock" del /F /Q ".git\index.lock" 2>nul

echo === 2. رفع إعدادات AndroidX الإجبارية للـ CI ===
git add codemagic.yaml
git commit -m "fix: force AndroidX properties via command line parameters to bypass cache issues"
git push origin main

echo.
echo ============================================
echo ✅ تم الرفع! 
echo اذهب إلى Codemagic واضغط Start new build
echo ============================================
pause
