@echo off
setlocal

echo =======================================================
echo     Running Application on Physical Device
echo =======================================================
echo.

:: Add Android SDK to PATH if it exists
set "PATH=%LOCALAPPDATA%\Android\Sdk\platform-tools;%PATH%"

cd /d "C:\Users\Owner\Desktop\Kayansoft22\mobile"

echo [1] Building Debug APK...
call gradlew.bat assembleDebug

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Build failed! Please check the output above.
    pause
    exit /b 1
)

echo.
echo [SUCCESS] Build completed.
echo [2] Checking for adb...

adb version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] ADB is not recognized! 
    echo Android SDK Platform-Tools is missing from your PATH.
    echo Please install Android Studio or connect your phone correctly.
    pause
    exit /b 1
)

echo.
echo [3] Installing APK on connected device...
adb install -r -t "app\build\outputs\apk\debug\app-debug.apk"
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Installation failed! Make sure your phone is connected and USB Debugging is ON.
    pause
    exit /b 1
)

echo.
echo [SUCCESS] Installed!
echo [4] Launching Application on screen...
adb shell am start -n com.aistudio.dahshacards.uylxtb/com.example.MainActivity

echo.
echo =================================================
echo  App launched! Monitoring Logcat for Crashes...
echo =================================================
echo.

adb logcat -c
adb logcat *:E

pause
