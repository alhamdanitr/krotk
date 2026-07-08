@echo off
chcp 65001 >nul
echo ============================================================
echo  KayanSoft - Professional Monorepo Restructure
echo ============================================================
echo.

cd /d "c:\Users\Owner\Desktop\Kayansoft22"

REM === نسخة احتياطية ===
echo [Step 1/8] Saving current state to git...
git add -A
git commit -m "backup: snapshot before professional restructure" --allow-empty

REM ============================================================
REM STEP 2: إنشاء مجلدات الهيكل الجديد
REM ============================================================
echo [Step 2/8] Creating new directory structure...
mkdir "mobile" 2>nul
mkdir "mobile\app" 2>nul
mkdir "admin" 2>nul
mkdir "docs" 2>nul

REM ============================================================
REM STEP 3: نقل Android App (mobile/)
REM ============================================================
echo [Step 3/8] Moving Android app to mobile/...

REM نقل ملفات Android من الجذر
git mv "build.gradle.kts" "mobile\build.gradle.kts"
git mv "settings.gradle.kts" "mobile\settings.gradle.kts"
git mv "gradle.properties" "mobile\gradle.properties"
git mv "gradlew" "mobile\gradlew"
git mv "gradlew.bat" "mobile\gradlew.bat"
git mv "gradle" "mobile\gradle"

REM نقل ملفات Android من مجلد app/
git mv "app\build.gradle.kts" "mobile\app\build.gradle.kts"
git mv "app\proguard-rules.pro" "mobile\app\proguard-rules.pro"
git mv "app\src" "mobile\app\src"

REM ============================================================
REM STEP 4: نقل Next.js Admin Dashboard (admin/)
REM ============================================================
echo [Step 4/8] Moving Next.js admin dashboard to admin/...

REM ملفات الجذر الخاصة بـ Next.js
git mv "next.config.mjs" "admin\next.config.mjs"
git mv "tsconfig.json" "admin\tsconfig.json"
git mv "postcss.config.mjs" "admin\postcss.config.mjs"
git mv "components.json" "admin\components.json"
git mv "pnpm-lock.yaml" "admin\pnpm-lock.yaml"
git mv "package.json" "admin\package.json"

REM نقل مجلدات Next.js من app/
git mv "app\globals.css" "admin\app\globals.css"
git mv "app\layout.tsx" "admin\app\layout.tsx"
git mv "app\page.tsx" "admin\app\page.tsx"
git mv "app\dashboard" "admin\app\dashboard"
git mv "app\login" "admin\app\login"

REM نقل المجلدات الأخرى
git mv "components" "admin\components"
git mv "lib" "admin\lib"
git mv "services" "admin\services"
git mv "types" "admin\types"

REM ============================================================
REM STEP 5: إعادة تسمية Backend
REM ============================================================
echo [Step 5/8] Renaming kurotek-backend to backend/...
git mv "kurotek-backend" "backend"

REM ============================================================
REM STEP 6: نقل الوثائق
REM ============================================================
echo [Step 6/8] Moving documentation to docs/...
git mv "Master_Blueprint.md" "docs\Master_Blueprint.md" 2>nul
git mv "SAD_Architecture.md" "docs\SAD_Architecture.md" 2>nul
git mv "SDD.txt" "docs\SDD.txt" 2>nul
git mv "SRS.txt" "docs\SRS.txt" 2>nul
git mv "SRS_Analysis.md" "docs\SRS_Analysis.md" 2>nul
git mv "DISTRIBUTOR_CALCULATOR_DESIGN.md" "docs\DISTRIBUTOR_CALCULATOR_DESIGN.md" 2>nul
git mv "implementation_plan.md" "docs\implementation_plan.md" 2>nul
git mv "progress_report.md" "docs\progress_report.md" 2>nul
git mv "وثيقة التصميم الهندسي الشاملة.md" "docs\system-design.md" 2>nul

REM ============================================================
REM STEP 7: حذف الملفات المؤقتة
REM ============================================================
echo [Step 7/8] Cleaning up temporary files...
git rm -f "RETRY_SLOW_UPLOAD.bat" 2>nul
git rm -f "Run_Kurotek_Local.bat" 2>nul
git rm -f "ULTIMATE_GIT_FIX.bat" 2>nul
git rm -f "diagnose_push.bat" 2>nul
git rm -f "execute_cleanup.bat" 2>nul
git rm -f "final_upload_fix.bat" 2>nul
git rm -f "fix_docker_error.bat" 2>nul
git rm -f "fix_git_repo.bat" 2>nul
git rm -f "fix_github_url.bat" 2>nul
git rm -f "flatten_and_push.bat" 2>nul
git rm -f "force_push.bat" 2>nul
git rm -f "force_push_ALL.bat" 2>nul
git rm -f "link_new_github.bat" 2>nul
git rm -f "push_error_log.txt" 2>nul
git rm -f "push_module_fix.bat" 2>nul
git rm -f "push_prisma_fix.bat" 2>nul
git rm -f "push_railway_updates.bat" 2>nul
git rm -f "push_render_fix.bat" 2>nul
git rm -f "switch_to_new_repo.bat" 2>nul
git rm -f "upload_to_github.bat" 2>nul
git rm -f "wipe_and_refresh_github.bat" 2>nul
git rm -f "diagnose_android_build.bat" 2>nul
git rm -f "build_android_apk.bat" 2>nul
git rm -f "build_output_log.txt" 2>nul
git rm -f "push_and_build_apk_cloud.bat" 2>nul
git rm -f "push_github_fix.bat" 2>nul
git rm -f "push_agp_fix.bat" 2>nul
git rm -f "push_manual_gradle.bat" 2>nul
git rm -f "push_plugin_fix.bat" 2>nul
git rm -f "push_plugin_fix_root.bat" 2>nul
git rm -f "fix_rogue_wrapper.bat" 2>nul
git rm -f "PUSH_ALL_PRODUCTION_FIXES.bat" 2>nul
git rm -f "push_final_apk_fix.bat" 2>nul
git rm -f "diagnose_push.bat" 2>nul
git rm -f "app.gitignore" 2>nul
git rm -f "PROJECT_RULES.md" 2>nul
git rm -f "push_error_log.txt" 2>nul
git rm -f "metadata.json" 2>nul

REM ============================================================
REM STEP 8: Commit و Push
REM ============================================================
echo [Step 8/8] Committing and pushing new structure...
git add -A
git commit -m "refactor: professional monorepo structure - backend/ + admin/ + mobile/ + docs/"
git push -u origin main

echo.
echo ============================================================
echo  DONE! Professional structure applied successfully.
echo ============================================================
echo.
echo  backend/   - NestJS API
echo  admin/     - Next.js Dashboard
echo  mobile/    - Android App
echo  docs/      - Documentation
echo.
pause
