@echo off
echo ==============================================================
echo [Kurotek AI] Wiping GitHub and Uploading Fresh Clean Copy
echo ==============================================================

cd /d "c:\Users\Owner\Desktop\Kayansoft22"

echo.
echo [1] Deleting old Git history to start 100%% fresh...
rmdir /S /Q ".git"

echo.
echo [2] Initializing a brand new, clean Git repository...
git init
git branch -M main

echo.
echo [3] Reconnecting to your GitHub repository...
git remote add origin https://github.com/KAYANSTOR/Kayansoft.git

echo.
echo [4] Tracking the new clean architecture files...
git add .
git commit -m "Initial commit: Production-Ready Architecture (Clean)"

echo.
echo [5] DANGER: Wiping GitHub and forcing the new files...
git push -u origin main --force

echo.
echo ==============================================================
echo GITHUB HAS BEEN COMPLETELY WIPED AND REFRESHED!
echo Your GitHub now has ONLY the clean, flattened, perfect code.
echo ==============================================================
pause
