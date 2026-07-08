@echo off
echo ==============================================================
echo [Kurotek AI] Starting Local Development Environment
echo ==============================================================

cd /d "c:\Users\Owner\Desktop\Kayansoft22\Kayansoft"

echo.
echo [1/3] Starting Backend Server (Port 3000)...
start "Kurotek Backend" cmd /c "cd kurotek-backend && npm install && npx prisma generate && npm run start:dev"

echo.
echo [2/3] Starting Admin Dashboard (Port 3001)...
start "Kurotek Frontend" cmd /c "call pnpm install && call pnpm next dev -p 3001"

echo.
echo [3/3] Opening Google Chrome...
timeout /t 5 /nobreak >nul
start chrome http://localhost:3001

echo.
echo ==============================================================
echo All services are starting in the background!
echo You can close this window now.
echo ==============================================================
pause
