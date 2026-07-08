@echo off
echo ==============================================================
echo [Kurotek AI] Diagnosing Upload Issue...
echo ==============================================================

cd /d "c:\Users\Owner\Desktop\Kayansoft22"

echo.
echo [1] Forcing Git to track all files...
git add .

echo.
echo [2] Committing all files...
git commit -m "Upload all files"

echo.
echo [3] Pushing to GitHub (Recording the Error)...
echo Please wait...
git push -u origin main --force > push_error_log.txt 2>&1

echo.
echo ==============================================================
echo DONE! A file named "push_error_log.txt" has been created.
echo ==============================================================
pause
