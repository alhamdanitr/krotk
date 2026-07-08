@echo off
echo ==============================================================
echo [Kurotek AI] Fixing Nested Git Repository Issue...
echo ==============================================================

cd /d "c:\Users\Owner\Desktop\Kayansoft22"

echo.
echo [1] Deleting the corrupted inner .git folder...
if exist "Kayansoft\.git\" (
    rmdir /s /q "Kayansoft\.git"
    echo Done.
) else (
    echo Inner .git folder not found. Skipping.
)

echo.
echo [2] Clearing git cache for the Kayansoft directory...
git rm --cached Kayansoft

echo.
echo [3] Adding all files properly to the main repository...
git add .

echo.
echo [4] Creating the Initial Commit...
git commit -m "Initial commit: Add KayanSoft platform application files"

echo.
echo [5] Setting branch to main...
git branch -M main

echo.
echo ==============================================================
echo REPAIR COMPLETED SUCCESSFULLY! 
echo ==============================================================
echo To complete your deployment to GitHub/Railway, run:
echo git push -u origin main
echo.
pause
