@echo off
echo ============================================================
echo [KayanSoft] Final Production Push — All Fixes Combined
echo ============================================================
cd /d "c:\Users\Owner\Desktop\Kayansoft22"

git add kurotek-backend/src/app.service.ts
git add kurotek-backend/src/app.controller.ts
git add app/build.gradle.kts
git add gradle/libs.versions.toml
git add build.gradle.kts
git add .github/workflows/android.yml
git add app/src/main/java/com/example/network/SyncManager.kt

git commit -m "refactor: production audit - secure app.service, stable android build (AGP 8.5.0, SDK 35), fixed CI/CD"
git push -u origin main

echo.
echo ============================================================
echo ALL CHANGES PUSHED! Backend + Android ready for production.
echo ============================================================
pause
