@echo off
echo ==============================================================
echo [Kurotek AI] Switching to NEW GitHub Repository (kayan.git)
echo ==============================================================

cd /d "c:\Users\Owner\Desktop\Kayansoft22"

echo.
echo [1] Removing old GitHub link...
git remote remove origin 2>nul

echo.
echo [2] Connecting to https://github.com/KAYANSTOR/kayan.git...
git remote add origin https://github.com/KAYANSTOR/kayan.git

echo.
echo [3] Uploading the clean project to the new repository...
git push -u origin main --force

echo.
echo ==============================================================
echo DONE!
echo Your project has been successfully uploaded to the new repository!
echo ==============================================================
pause
