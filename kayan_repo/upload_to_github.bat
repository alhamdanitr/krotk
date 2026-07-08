@echo off
echo ==============================================================
echo [Kurotek AI] Solving GitHub Push Problem (Force Upload)...
echo ==============================================================

cd /d "c:\Users\Owner\Desktop\Kayansoft22"

echo.
echo [1] Adding all files to Git...
git add .

echo.
echo [2] Forcing a fresh Commit...
git commit -m "Initial commit: KayanSoft platform files"

echo.
echo [3] Setting branch to main...
git branch -M main

echo.
echo [4] Uploading to GitHub (Force Push)...
echo NOTE: This will overwrite any empty files currently on GitHub.
git push -u origin main --force

echo.
echo ==============================================================
echo PROCESS COMPLETED!
echo If a window pops up, please sign in to your GitHub account.
echo ==============================================================
pause
