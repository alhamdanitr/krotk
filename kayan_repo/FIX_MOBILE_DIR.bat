@echo off
cd /d "c:\Users\Owner\Desktop\Kayansoft22"

echo === 1. Moving Android files from root 'app' to 'mobile/app' ===
if not exist "mobile\app" mkdir "mobile\app"

if exist "app\src" (
    move "app\src" "mobile\app\src"
)
if exist "app\build.gradle.kts" (
    move "app\build.gradle.kts" "mobile\app\build.gradle.kts"
)
if exist "app\proguard-rules.pro" (
    move "app\proguard-rules.pro" "mobile\app\proguard-rules.pro"
)

echo === 2. Moving root gradle wrapper to 'mobile/gradle' ===
if exist "gradle" (
    xcopy /E /Y /I "gradle" "mobile\gradle"
    rmdir /S /Q "gradle"
)

echo === 3. Moving other Android root files ===
if exist "local.properties" move "local.properties" "mobile\local.properties"
if exist "gradle.properties" move "gradle.properties" "mobile\gradle.properties"

echo === 4. Syncing changes with Git ===
git add -A
git commit -m "fix: complete the move of Android project to mobile/ directory"
git push origin main

echo === DONE ===
pause
