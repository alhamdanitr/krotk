@echo off
setlocal
cd /d "%~dp0"

echo =======================================================
echo     KayanSoft - تشغيل التطبيق على الهاتف / المحاكي
echo =======================================================
echo.

echo [1] التحقق من الأجهزة المتصلة...
adb devices
echo.

echo [2] بناء نسخة اختبارية (Debug APK)...
cd mobile
call gradlew.bat assembleDebug
if %ERRORLEVEL% NEQ 0 (
    echo ❌ فشل البناء! يرجى مراجعة الأخطاء أعلاه.
    pause
    exit /b 1
)
cd ..
echo ✅ تم بناء التطبيق بنجاح.
echo.

set APK_PATH=mobile\app\build\outputs\apk\debug\app-debug.apk

echo [3] تثبيت التطبيق على الجهاز المتصل...
adb install -r -t "%APK_PATH%"
if %ERRORLEVEL% NEQ 0 (
    echo ❌ فشل التثبيت! تأكد من توصيل الجهاز وتفعيل خيارات المطور.
    pause
    exit /b 1
)
echo ✅ تم التثبيت بنجاح.
echo.

echo [4] تشغيل التطبيق...
adb shell am start -n com.aistudio.dahshacards.uylxtb/com.example.MainActivity
echo ✅ التطبيق يعمل الآن على هاتفك!
echo.

echo [5] مراقبة السجلات (Logcat) للبحث عن أي انهيارات (Crashes)...
echo (اضغط Ctrl+C لإيقاف المراقبة)
echo.

REM مسح السجلات القديمة
adb logcat -c

REM فلترة السجلات لعرض المشاكل فقط أو الخاصة بالتطبيق
adb logcat *:E

pause
