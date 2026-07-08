@echo off
echo ============================================================
echo [KayanSoft] تحميل وتنصيب Gradle + بناء APK محلياً
echo ============================================================
echo.

cd /d "c:\Users\Owner\Desktop\Kayansoft22"

echo [1/3] التحقق من وجود Java...
java -version
if errorlevel 1 (
  echo.
  echo ❌ Java غير موجودة! 
  echo من فضلك حمّل Java 17 من الرابط:
  echo https://adoptium.net/temurin/releases/?version=17
  echo ثم شغّل هذا الملف مرة أخرى.
  pause
  exit /b 1
)

echo.
echo [2/3] تحميل Gradle Wrapper JAR من الإنترنت...
powershell -Command "Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/gradle/gradle/v8.4.0/gradle/wrapper/gradle-wrapper.jar' -OutFile 'gradle\wrapper\gradle-wrapper.jar'"

if not exist "gradle\wrapper\gradle-wrapper.jar" (
  echo.
  echo ❌ فشل تحميل Gradle Wrapper. تحقق من اتصالك بالإنترنت.
  pause
  exit /b 1
)

echo.
echo [3/3] بناء APK...
call gradlew.bat assembleDebug

if errorlevel 1 (
  echo.
  echo ❌ فشل البناء. انظر الأخطاء أعلاه.
  pause
  exit /b 1
)

echo.
echo ============================================================
echo ✅ تم! ملف التطبيق جاهز!
echo ============================================================
explorer "app\build\outputs\apk\debug"
pause
