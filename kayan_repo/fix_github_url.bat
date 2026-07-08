@echo off
setlocal enabledelayedexpansion
echo ==============================================================
echo [Kurotek AI] Fixing GitHub URL and Pushing
echo ==============================================================

cd /d "c:\Users\Owner\Desktop\Kayansoft22"

echo Currently, your GitHub URL is set to:
git remote -v
echo.

set /p github_user="Please type your REAL GitHub Username: "
set /p github_repo="Please type your GitHub Repository Name (e.g. KayanSoft): "

set new_url=https://github.com/!github_user!/!github_repo!.git

echo.
echo Setting your GitHub URL to: !new_url!
git remote set-url origin !new_url!

echo.
echo Pushing project to GitHub...
git push -u origin main --force

echo.
echo ==============================================================
echo DONE! Check the output above.
echo ==============================================================
pause
