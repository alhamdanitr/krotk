@echo off
echo ==============================================================
echo [Kurotek AI] Forcing Nixpacks and Removing Docker
echo ==============================================================

cd /d "c:\Users\Owner\Desktop\Kayansoft22"

echo.
echo [1] Deleting Docker files to force Railway to use Nixpacks...
if exist "Dockerfile" del /F /Q "Dockerfile"
if exist "docker-compose.production.yml" del /F /Q "docker-compose.production.yml"

echo.
echo [2] Committing the fix...
git add .
git commit -m "fix: delete Dockerfile to enforce Railway Nixpacks native build"

echo.
echo [3] Pushing to GitHub...
git push origin main

echo.
echo ==============================================================
echo DONE! Railway will now restart the build without Docker errors.
echo ==============================================================
pause
