@echo off
echo Deleting corrupted wrapper jar...
del /F /Q "c:\Users\Owner\Desktop\Kayansoft22\remix_-حاسبه-موزع-الدحشة\android\gradle\wrapper\gradle-wrapper.jar"
git rm -f "remix_-حاسبه-موزع-الدحشة/android/gradle/wrapper/gradle-wrapper.jar"
git commit -m "fix: remove corrupted gradle wrapper"
git push -u origin main
echo DONE!
pause
