@echo off
cd /d "c:\Users\Owner\Desktop\Kayansoft22"
git add .github/workflows/android.yml
git commit -m "fix: update github action versions to v4"
git push -u origin main
echo DONE!
pause
