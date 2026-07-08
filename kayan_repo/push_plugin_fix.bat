@echo off
cd /d "c:\Users\Owner\Desktop\Kayansoft22"
git add app/build.gradle.kts
git commit -m "fix: remove outdated secrets plugin causing AndroidPluginVersion crash"
git push -u origin main
echo DONE!
pause
