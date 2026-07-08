@echo off
setlocal
echo ==============================================================
echo [Kurotek AI] Fixing Repository Structure for Railway...
echo ==============================================================

cd /d "c:\Users\Owner\Desktop\Kayansoft22"

echo.
echo [1] Moving all files from inner Kayansoft folder to the main root...
xcopy /E /H /Y /C "Kayansoft\*" .\ >nul

echo.
echo [2] Deleting the old empty Kayansoft folder...
rmdir /S /Q "Kayansoft"

echo.
echo [3] Updating Git Tracking...
git add . -A

echo.
echo [4] Committing the changes...
git commit -m "chore: flatten repository structure for Railway auto-detection"

echo.
echo [5] Uploading updates to GitHub...
git push origin main

echo.
echo ==============================================================
echo REPOSITORY RESTRUCTURED AND PUSHED SUCCESSFULLY!
echo ==============================================================
pause
