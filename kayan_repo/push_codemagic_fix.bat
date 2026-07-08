@echo off
cd /d "c:\Users\Owner\Desktop\Kayansoft22"
git add codemagic.yaml
git commit -m "fix: use direct gradle instead of broken gradlew wrapper"
git push
echo.
echo DONE! Go to Codemagic and click "Start new build"
pause
