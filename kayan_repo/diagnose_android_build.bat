@echo off
echo ==============================================================
echo [Kurotek AI] Building Android APK (Diagnostic Mode)...
echo ==============================================================

cd /d "c:\Users\Owner\Desktop\Kayansoft22"

echo Please wait, capturing build errors...
call gradlew.bat assembleDebug > build_output_log.txt 2>&1

echo.
echo ==============================================================
echo BUILD ATTEMPT COMPLETED!
echo ==============================================================
pause
