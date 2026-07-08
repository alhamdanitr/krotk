@echo off
cd /d "c:\Users\Owner\Desktop\Kayansoft22"
git add app/build.gradle.kts gradle/libs.versions.toml
git commit -m "fix: stable compileSdk=35, AGP=8.5.0, remove experimental APIs and debug keystore crash"
git push -u origin main
echo DONE!
pause
