@echo off
echo ==============================================================
echo [Kurotek AI] Forcing Git Commit and Push...
echo ==============================================================

cd /d "c:\Users\Owner\Desktop\Kayansoft22"

echo.
echo [1] Configuring Local Git User (in case it is missing)...
git config user.email "admin@kayansoft.com"
git config user.name "KayanSoft Admin"

echo.
echo [2] Adding files to staging...
git add .

echo.
echo [3] Creating commit...
git commit -m "Initial commit: KayanSoft platform files"

echo.
echo [4] Renaming branch to main...
git branch -M main

echo.
echo [5] Pushing to GitHub...
git push -u origin main

echo.
echo ==============================================================
echo DONE! Check the output above for any errors.
echo ==============================================================
pause
