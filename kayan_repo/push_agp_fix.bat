@echo off
cd /d "c:\Users\Owner\Desktop\Kayansoft22"
git add gradle/libs.versions.toml
git commit -m "fix: downgrade AGP version from hallucinatory 9.1.1 to 8.2.2"
git push -u origin main
echo DONE!
pause
