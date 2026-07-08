@echo off
chcp 65001 >nul
echo ============================================================
echo  KayanSoft - فحص Java وبناء APK
echo ============================================================
echo.

cd /d "c:\Users\Owner\Desktop\Kayansoft22"

REM === فحص Java ===
echo [1] فحص Java...
java -version >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Java موجودة!
    java -version
    goto :download_jar
)

REM === محاولة Android Studio JDK ===
echo Java غير موجودة في PATH، جاري البحث عن Android Studio JDK...

set "JAVA_PATHS[0]=%LOCALAPPDATA%\Programs\Android\Android Studio\jbr\bin"
set "JAVA_PATHS[1]=C:\Program Files\Android\Android Studio\jbr\bin"
set "JAVA_PATHS[2]=C:\Program Files\Microsoft\jdk-17.0.9+8\bin"
set "JAVA_PATHS[3]=C:\Program Files\Java\jdk-17\bin"
set "JAVA_PATHS[4]=C:\Program Files\Java\jdk-17.0.0\bin"

for /L %%i in (0,1,4) do (
    call set "JPATH=%%JAVA_PATHS[%%i]%%"
    call :checkjava "%%JPATH%%"
    if defined JAVA_HOME goto :download_jar
)

echo.
echo ❌ Java غير موجودة في جهازك.
echo.
echo الحل: حمّل Java 17 من هنا (مجاني - حجم 175MB):
echo https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.11%%2B9/OpenJDK17U-jdk_x64_windows_hotspot_17.0.11_9.msi
echo.
echo أو اضغط هنا لفتح رابط التحميل:
start https://adoptium.net/temurin/releases/?version=17
echo.
echo بعد التثبيت، شغّل هذا الملف مجدداً.
pause
exit /b 1

:checkjava
if exist "%~1\java.exe" (
    set "JAVA_HOME=%~1\.."
    set "PATH=%~1;%PATH%"
    echo ✅ تم العثور على Java في: %~1
)
goto :eof

:download_jar
echo.
echo [2] تحميل Gradle Wrapper (60KB فقط)...
if not exist "gradle\wrapper" mkdir "gradle\wrapper"

powershell -Command "& { try { Invoke-WebRequest -Uri 'https://github.com/gradle/gradle/raw/v8.4.0/gradle/wrapper/gradle-wrapper.jar' -OutFile 'gradle\wrapper\gradle-wrapper.jar' -UseBasicParsing; Write-Host 'Downloaded!' } catch { Write-Host 'Trying mirror...'; Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/gradle/gradle/v8.4.0/gradle/wrapper/gradle-wrapper.jar' -OutFile 'gradle\wrapper\gradle-wrapper.jar' -UseBasicParsing } }"

if not exist "gradle\wrapper\gradle-wrapper.jar" (
    echo ❌ فشل تحميل Gradle. تحقق من الإنترنت.
    pause
    exit /b 1
)
echo ✅ تم التحميل!

echo.
echo [3] جاري بناء APK...
echo (قد يستغرق 3-5 دقائق في أول مرة)
echo.
call gradlew.bat assembleDebug

if %errorlevel% neq 0 (
    echo.
    echo ❌ فشل البناء. الخطأ أعلاه.
    pause
    exit /b 1
)

echo.
echo ============================================================
echo ✅ تم بناء التطبيق بنجاح!
echo ============================================================
echo.
echo جاري نسخ APK إلى سطح المكتب...
copy "app\build\outputs\apk\debug\app-debug.apk" "%USERPROFILE%\Desktop\KayanSoft-App.apk"
echo ✅ الملف موجود الآن على سطح المكتب: KayanSoft-App.apk
echo.
echo لتثبيته على هاتفك:
echo 1. وصّل هاتفك بالحاسوب عبر USB
echo 2. انقل ملف KayanSoft-App.apk إلى هاتفك
echo 3. ابحث عن الملف في هاتفك واضغط عليه لتثبيته
echo 4. إذا طلب منك "السماح بتثبيت تطبيقات غير معروفة" اضغط موافق
echo.
explorer "%USERPROFILE%\Desktop"
pause
