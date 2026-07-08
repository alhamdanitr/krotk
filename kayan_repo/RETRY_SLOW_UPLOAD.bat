@echo off
echo ==============================================================
echo [Kurotek AI] Fixing Slow Internet Timeout and Retrying...
echo ==============================================================

cd /d "c:\Users\Owner\Desktop\Kayansoft22"

echo.
echo [1] Applying Git Network Fixes for Slow Connections...
git config --global http.postBuffer 524288000
git config --global http.lowSpeedLimit 1000
git config --global http.lowSpeedTime 600

echo.
echo [2] Retrying the Upload to GitHub...
echo Please wait patiently, this may take a few minutes if the internet is slow...
git push -u origin main --force

echo.
echo ==============================================================
echo UPLOAD COMPLETED SUCCESSFULLY!
echo ==============================================================
pause
