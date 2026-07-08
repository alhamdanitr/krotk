@echo off
cd /d "c:\Users\Owner\Desktop\Kayansoft22"
git add .github/workflows/android.yml
git commit -m "fix: manual gradle installation to bypass wrapper validation completely"
git push -u origin main
echo DONE!
pause
