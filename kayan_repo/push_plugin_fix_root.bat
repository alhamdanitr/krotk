@echo off
cd /d "c:\Users\Owner\Desktop\Kayansoft22"
git add build.gradle.kts
git add app/build.gradle.kts
git commit -m "fix: remove outdated secrets and roborazzi plugins from root to prevent AndroidPluginVersion crash"
git push -u origin main
echo DONE!
pause
