@echo off
echo ==============================================================
echo [Kurotek AI] Pushing all final modifications...
echo ==============================================================

cd /d "c:\Users\Owner\Desktop\Kayansoft22"

git add .
git commit -m "fix: enforce render dev dependencies and configuration"
git push -u origin main

echo ==============================================================
echo UPLOAD COMPLETED! NOW GO TO RENDER AND DEPLOY.
echo ==============================================================
pause
