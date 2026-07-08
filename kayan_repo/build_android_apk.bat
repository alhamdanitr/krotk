@echo off
echo ==============================================================
echo [Kurotek AI] Building Android APK for the Phone...
echo ==============================================================

cd /d "c:\Users\Owner\Desktop\Kayansoft22"

echo Fixing permissions and building...
call gradlew.bat assembleDebug

echo.
echo ==============================================================
echo BUILD COMPLETED!
echo Your APK file is ready to be transferred to your phone.
echo ==============================================================

explorer "c:\Users\Owner\Desktop\Kayansoft22\app\build\outputs\apk\debug"
pause
