@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul

echo =======================================================
echo     تشغيل التطبيق على الهاتف مباشرة (النسخة النهائية)
echo =======================================================
echo.

cd /d "C:\Users\Owner\Desktop\Kayansoft22\mobile"

echo [1] جاري البناء والتحقق من الهاتف...
call gradlew.bat assembleDebug

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ حدث خطأ أثناء البناء! يرجى مراجعة الأخطاء في الأعلى.
    pause
    exit /b 1
)

echo.
echo ✅ تم البناء بنجاح!
echo [2] جاري تثبيت التطبيق على الهاتف المتصل...

adb install -r -t "app\build\outputs\apk\debug\app-debug.apk"
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ فشل التثبيت! تأكد من:
    echo 1. توصيل الهاتف عبر USB.
    echo 2. تفعيل خيارات المطور (Developer Options) وتصحيح USB (USB Debugging).
    echo 3. الموافقة على شاشة التثبيت التي قد تظهر في الهاتف.
    pause
    exit /b 1
)

echo.
echo ✅ تم التثبيت!
echo [3] جاري فتح التطبيق على الشاشة...
adb shell am start -n com.aistudio.dahshacards.uylxtb/com.example.MainActivity

echo.
echo =================================================
echo 🎉 التطبيق مفتوح الآن على هاتفك!
echo سيتم الآن عرض الأخطاء المباشرة (إن وجدت) هنا...
echo =================================================
echo.

adb logcat -c
adb logcat *:E

pause
