@echo off
cd /d "c:\Users\Owner\Desktop\Kayansoft22"

echo === 1. backend ===
git mv kurotek-backend backend 2>nul

echo === 2. mobile ===
mkdir mobile\app 2>nul
git mv build.gradle.kts mobile\build.gradle.kts 2>nul
git mv settings.gradle.kts mobile\settings.gradle.kts 2>nul
git mv gradle.properties mobile\gradle.properties 2>nul
git mv gradlew mobile\gradlew 2>nul
git mv gradlew.bat mobile\gradlew.bat 2>nul
git mv app\build.gradle.kts mobile\app\build.gradle.kts 2>nul
git mv app\proguard-rules.pro mobile\app\proguard-rules.pro 2>nul
git mv app\src mobile\app\src 2>nul

REM === نقل gradle بالطريقة العادية (لا git mv) ===
robocopy gradle mobile\gradle /E /MOVE /NFL /NDL /NJH /NJS 2>nul
if exist gradle\wrapper\gradle-wrapper.properties (
    xcopy /E /Y /I gradle mobile\gradle 2>nul
    rmdir /S /Q gradle 2>nul
)

echo === 3. admin ===
mkdir admin\app 2>nul
git mv next.config.mjs admin\next.config.mjs 2>nul
git mv tsconfig.json admin\tsconfig.json 2>nul
git mv postcss.config.mjs admin\postcss.config.mjs 2>nul
git mv components.json admin\components.json 2>nul
git mv package.json admin\package.json 2>nul
git mv pnpm-lock.yaml admin\pnpm-lock.yaml 2>nul
git mv app\globals.css admin\app\globals.css 2>nul
git mv app\layout.tsx admin\app\layout.tsx 2>nul
git mv app\page.tsx admin\app\page.tsx 2>nul
git mv app\dashboard admin\app\dashboard 2>nul
git mv app\login admin\app\login 2>nul
git mv components admin\components 2>nul
git mv lib admin\lib 2>nul
git mv services admin\services 2>nul
git mv types admin\types 2>nul
git mv hooks admin\hooks 2>nul

echo === 4. docs ===
mkdir docs 2>nul
git mv Master_Blueprint.md docs\ 2>nul
git mv SAD_Architecture.md docs\ 2>nul
git mv SRS_Analysis.md docs\ 2>nul
git mv SDD.txt docs\ 2>nul
git mv SRS.txt docs\ 2>nul

echo === 5. رفع ===
git add -A
git commit -m "refactor: backend/ + admin/ + mobile/ + docs/"
git push -u origin main

echo.
echo === تم! ===
pause
