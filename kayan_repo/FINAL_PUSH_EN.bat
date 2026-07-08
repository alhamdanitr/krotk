@echo off
setlocal

echo =======================================================
echo     Pushing updates to GitHub for Codemagic Build
echo =======================================================
echo.

cd /d "C:\Users\Owner\Desktop\Kayansoft22"

echo [1] Cleaning lock files...
if exist ".git\index.lock" del /F /Q ".git\index.lock" 2>nul

echo [2] Staging changes...
git add .

echo [3] Committing changes...
git commit -m "fix: Final push. Resolved Android 14 permissions and Room KSP configurations for Codemagic Build"

echo [4] Pushing to GitHub...
git push origin main

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Push failed! Please check your internet connection or git permissions.
    pause
    exit /b 1
)

echo.
echo =================================================
echo [SUCCESS] Push completed successfully!
echo You can now go to Codemagic.io to build the APK.
echo =================================================
echo.

pause
