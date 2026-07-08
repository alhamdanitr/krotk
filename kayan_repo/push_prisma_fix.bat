@echo off
cd /d "c:\Users\Owner\Desktop\Kayansoft22"
git add kurotek-backend/prisma/schema.prisma
git commit -m "fix: resolve Prisma validation errors for Distributor module"
git push -u origin main
echo DONE!
pause
