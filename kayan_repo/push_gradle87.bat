@echo off
cd /d "c:\Users\Owner\Desktop\Kayansoft22"
git add codemagic.yaml gradle/wrapper/gradle-wrapper.properties
git commit -m "fix: upgrade Gradle to 8.7 to match AGP 8.5.0 requirements"
git push
echo.
echo DONE! Go to Codemagic and click "Start new build"
pause
