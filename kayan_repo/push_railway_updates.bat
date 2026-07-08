@echo off
echo ==============================================================
echo [Kurotek AI] Syncing Railway Configs to GitHub...
echo ==============================================================

cd /d "c:\Users\Owner\Desktop\Kayansoft22"

echo.
echo [1] Adding new Railway configuration files...
git add .

echo.
echo [2] Committing Railway Database and Backend links...
git commit -m "chore: Configure Railway Nixpacks, DB links, and API production URLs"

echo.
echo [3] Pushing to GitHub...
git push origin main

echo.
echo ==============================================================
echo SUCCESSFULLY PUSHED TO GITHUB! 
echo Railway will now automatically detect these changes and deploy.
echo ==============================================================
pause
