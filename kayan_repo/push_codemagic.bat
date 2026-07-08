@echo off
cd /d "c:\Users\Owner\Desktop\Kayansoft22"
git add codemagic.yaml
git commit -m "Add first workflow"
git push
echo.
echo DONE! Now go back to Codemagic and click Start Build
pause
