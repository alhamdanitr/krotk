@echo off
cd /d "c:\Users\Owner\Desktop\Kayansoft22"
git add render.yaml
git commit -m "fix: explicit prisma schema paths for render"
git push -u origin main
echo DONE!
pause
