@echo off
cd /d "c:\Users\Owner\Desktop\Kayansoft22"
git add kurotek-backend/src/transactions/transactions.module.ts
git commit -m "fix: export TransactionsService for EventsGateway"
git push -u origin main
echo DONE!
pause
