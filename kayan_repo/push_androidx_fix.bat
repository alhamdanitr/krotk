@echo off
cd /d "c:\Users\Owner\Desktop\Kayansoft22"
git add gradle.properties
git commit -m "fix: add android.useAndroidX=true to gradle.properties"
git push
echo DONE! Go to Codemagic and click Start new build
pause
