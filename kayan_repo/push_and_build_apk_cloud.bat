@echo off
cd /d "c:\Users\Owner\Desktop\Kayansoft22"
git add app/src/main/java/com/example/network/SyncManager.kt
git add .github/workflows/android.yml
git commit -m "fix: update ws url and add github action for apk build"
git push -u origin main
echo DONE!
pause
