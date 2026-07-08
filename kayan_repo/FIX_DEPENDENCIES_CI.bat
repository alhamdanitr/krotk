@echo off
cd /d "c:\Users\Owner\Desktop\Kayansoft22"

echo === 1. تنظيف أي قفل Git ===
if exist ".git\index.lock" del /F /Q ".git\index.lock" 2>nul

echo === 2. إصلاح تعارض إصدارات AndroidX ===
git add mobile\gradle\libs.versions.toml
git commit -m "fix: downgrade core-ktx and room to stable versions compatible with AGP 8.5.0"
git push origin main

echo.
echo ============================================
echo ✅ تم الحل والرفع! 
echo عد إلى Codemagic واضغط Start new build
echo ============================================
pause
