@echo off
setlocal enabledelayedexpansion
echo ==============================================================
echo [Kurotek AI] Linking Project to a NEW GitHub Repository
echo ==============================================================

cd /d "c:\Users\Owner\Desktop\Kayansoft22"

echo.
echo يرجى لصق رابط المستودع الجديد الخاص بك هنا 
echo (مثال: https://github.com/USERNAME/NEW-REPO.git)
set /p new_github_url="Paste new GitHub URL here and press Enter: "

echo.
echo [1] Removing old GitHub connection...
git remote remove origin 2>nul

echo.
echo [2] Linking to the new repository...
git remote add origin !new_github_url!

echo.
echo [3] Uploading the clean project to the new repository...
git push -u origin main --force

echo.
echo ==============================================================
echo SUCCESSFULLY LINKED AND UPLOADED TO THE NEW REPOSITORY!
echo تم ربط مشروعك ورفعه إلى المستودع الجديد بنجاح تام!
echo ==============================================================
pause
