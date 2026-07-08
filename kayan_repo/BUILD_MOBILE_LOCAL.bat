@echo off
chcp 65001 >nul
echo ============================================================
echo  KayanSoft - بناء الـ APK محلياً (البنية الجديدة)
echo ============================================================
echo.

cd /d "c:\Users\Owner\Desktop\Kayansoft22\mobile"

REM === فحص Java ===
echo [1] فحص إعدادات Java...
java -version >nul 2>&1
if %errorlevel% equ 0 (
    goto :download_jar
)

echo جاري البحث عن Java...
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

echo ❌ Java غير موجودة!
pause
exit /b 1

:checkjava
if exist "%~1\java.exe" (
    set "JAVA_HOME=%~1\.."
    set "PATH=%~1;%PATH%"
)
goto :eof

:download_jar
echo.
echo [2] فحص Gradle Wrapper...
if not exist "gradle\wrapper" mkdir "gradle\wrapper"

powershell -Command "& { try { Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/gradle/gradle/v8.7.0/gradle/wrapper/gradle-wrapper.jar' -OutFile 'gradle\wrapper\gradle-wrapper.jar' -UseBasicParsing; Write-Host 'Downloaded!' } catch { Write-Host 'Download failed but continuing...' } }"

echo.
echo [3] جاري بناء التطبيق... (قد يأخذ بعض الوقت)
echo.
call gradlew.bat assembleDebug -Pandroid.useAndroidX=true -Pandroid.enableJetifier=true --no-configuration-cache

if %errorlevel% neq 0 (
    echo.
    echo ❌ فشل البناء.
    pause
    exit /b 1
)

echo.
echo ============================================================
echo ✅ تم بناء التطبيق بنجاح!
echo ============================================================
echo جاري نسخ APK إلى سطح المكتب...
copy "app\build\outputs\apk\debug\app-debug.apk" "%USERPROFILE%\Desktop\KayanSoft-App-New.apk"
echo ✅ الملف موجود الآن على سطح المكتب: KayanSoft-App-New.apk
explorer "%USERPROFILE%\Desktop"
pause
