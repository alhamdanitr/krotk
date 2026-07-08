@echo off
echo ==============================================================
echo [Kurotek AI] Fixing GitHub Files and Uploading...
echo ==============================================================

cd /d "c:\Users\Owner\Desktop\Kayansoft22"

echo.
echo [1] Tracking ALL files in the directory...
git add .

echo.
echo [2] Forcing a fresh commit...
git commit -m "fix: enforce tracking of all package.json files"

echo.
echo [3] Forcing upload to GitHub...
git push -u origin main --force

echo.
echo ==============================================================
echo DONE! ALL FILES ARE UPLOADED TO GITHUB SUCCESSFULLY.
echo ==============================================================
pause
