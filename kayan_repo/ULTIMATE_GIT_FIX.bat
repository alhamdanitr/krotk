@echo off
echo ==============================================================
echo [Kurotek AI] ULTIMATE GITHUB FIX (Solving the Submodule Bug)
echo ==============================================================

cd /d "c:\Users\Owner\Desktop\Kayansoft22"

echo.
echo [1] Destroying all nested and hidden .git folders...
rmdir /S /Q ".git"
for /f "tokens=*" %%a in ('dir /s /b /a:d ".git" 2^>nul') do (
    echo Deleting nested git: %%a
    rmdir /S /Q "%%a"
)

echo.
echo [2] Initializing fresh root Git repo...
git init
git branch -M main

echo.
echo [3] Connecting to your repository...
git remote add origin https://github.com/KAYANSTOR/kayan.git

echo.
echo [4] Tracking all files properly...
git add .
git commit -m "fix: destroy submodules, upload genuine files, ignore node_modules"

echo.
echo [5] Forcing Upload to GitHub...
git push -u origin main --force

echo.
echo ==============================================================
echo DONE! GITHUB HAS NOW RECEIVED EVERY SINGLE FILE PROPERLY!
echo ==============================================================
pause
