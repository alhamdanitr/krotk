# PowerShell - إعادة هيكلة مشروع KayanSoft
Set-Location "c:\Users\Owner\Desktop\Kayansoft22"

Write-Host "============================================" -ForegroundColor Cyan
Write-Host " إعادة هيكلة مشروع KayanSoft" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

# --- نسخة احتياطية ---
Write-Host "`n[1] حفظ نسخة احتياطية..." -ForegroundColor Yellow
git add -A
git commit -m "backup: before restructure" --allow-empty

# ============================
# إنشاء مجلد backend/
# ============================
Write-Host "`n[2] إنشاء backend/ من kurotek-backend/..." -ForegroundColor Yellow
git mv kurotek-backend backend
Write-Host "✅ تم" -ForegroundColor Green

# ============================
# إنشاء مجلد mobile/
# ============================
Write-Host "`n[3] إنشاء mobile/ للتطبيق الأندرويد..." -ForegroundColor Yellow
New-Item -ItemType Directory -Path "mobile\app" -Force | Out-Null

# نقل ملفات Android من الجذر
$androidRootFiles = @(
    "build.gradle.kts",
    "settings.gradle.kts", 
    "gradle.properties",
    "gradlew",
    "gradlew.bat"
)
foreach ($f in $androidRootFiles) {
    if (Test-Path $f) {
        git mv $f "mobile\$f"
        Write-Host "  → نقل $f" -ForegroundColor Gray
    }
}

# نقل مجلد gradle
if (Test-Path "gradle") {
    git mv gradle mobile\gradle
    Write-Host "  → نقل gradle/" -ForegroundColor Gray
}

# نقل ملفات Android من app/
$androidAppFiles = @("build.gradle.kts", "proguard-rules.pro")
foreach ($f in $androidAppFiles) {
    if (Test-Path "app\$f") {
        git mv "app\$f" "mobile\app\$f"
        Write-Host "  → نقل app/$f" -ForegroundColor Gray
    }
}

# نقل src (Android source code)
if (Test-Path "app\src") {
    git mv "app\src" "mobile\app\src"
    Write-Host "  → نقل app/src/" -ForegroundColor Gray
}

Write-Host "✅ تم" -ForegroundColor Green

# ============================
# إنشاء مجلد admin/
# ============================
Write-Host "`n[4] إنشاء admin/ للوحة التحكم..." -ForegroundColor Yellow
New-Item -ItemType Directory -Path "admin\app" -Force | Out-Null

# نقل ملفات Next.js من الجذر
$nextRootFiles = @(
    "next.config.mjs",
    "tsconfig.json",
    "postcss.config.mjs",
    "components.json",
    "pnpm-lock.yaml",
    "package.json"
)
foreach ($f in $nextRootFiles) {
    if (Test-Path $f) {
        git mv $f "admin\$f"
        Write-Host "  → نقل $f" -ForegroundColor Gray
    }
}

# نقل ملفات Next.js من app/
$nextAppFiles = @("globals.css", "layout.tsx", "page.tsx")
foreach ($f in $nextAppFiles) {
    if (Test-Path "app\$f") {
        git mv "app\$f" "admin\app\$f"
        Write-Host "  → نقل app/$f" -ForegroundColor Gray
    }
}

# نقل مجلدات Next.js من app/
$nextAppDirs = @("dashboard", "login")
foreach ($d in $nextAppDirs) {
    if (Test-Path "app\$d") {
        git mv "app\$d" "admin\app\$d"
        Write-Host "  → نقل app/$d/" -ForegroundColor Gray
    }
}

# نقل المجلدات الجذرية لـ Next.js
$nextDirs = @("components", "lib", "services", "types", "hooks")
foreach ($d in $nextDirs) {
    if (Test-Path $d) {
        git mv $d "admin\$d"
        Write-Host "  → نقل $d/" -ForegroundColor Gray
    }
}

Write-Host "✅ تم" -ForegroundColor Green

# ============================
# إنشاء مجلد docs/
# ============================
Write-Host "`n[5] إنشاء docs/..." -ForegroundColor Yellow
New-Item -ItemType Directory -Path "docs" -Force | Out-Null

$docFiles = @(
    "Master_Blueprint.md",
    "SAD_Architecture.md",
    "SRS_Analysis.md",
    "implementation_plan.md",
    "progress_report.md"
)
foreach ($f in $docFiles) {
    if (Test-Path $f) {
        git mv $f "docs\$f"
        Write-Host "  → نقل $f" -ForegroundColor Gray
    }
}
Write-Host "✅ تم" -ForegroundColor Green

# ============================
# حذف الملفات المؤقتة
# ============================
Write-Host "`n[6] حذف الملفات المؤقتة..." -ForegroundColor Yellow
$junkFiles = @(
    "RETRY_SLOW_UPLOAD.bat", "Run_Kurotek_Local.bat", "ULTIMATE_GIT_FIX.bat",
    "diagnose_push.bat", "execute_cleanup.bat", "final_upload_fix.bat",
    "fix_docker_error.bat", "fix_git_repo.bat", "fix_github_url.bat",
    "flatten_and_push.bat", "force_push.bat", "force_push_ALL.bat",
    "link_new_github.bat", "push_error_log.txt", "push_module_fix.bat",
    "push_prisma_fix.bat", "push_railway_updates.bat", "push_render_fix.bat",
    "switch_to_new_repo.bat", "upload_to_github.bat", "wipe_and_refresh_github.bat",
    "diagnose_android_build.bat", "build_android_apk.bat", "build_output_log.txt",
    "push_and_build_apk_cloud.bat", "push_github_fix.bat", "push_agp_fix.bat",
    "push_manual_gradle.bat", "push_plugin_fix.bat", "push_plugin_fix_root.bat",
    "fix_rogue_wrapper.bat", "PUSH_ALL_PRODUCTION_FIXES.bat", "push_final_apk_fix.bat",
    "app.gitignore", "metadata.json", "RESTRUCTURE_PROJECT.bat", "push_codemagic.bat",
    "BUILD_APK_LOCAL.bat", "BUILD_APK_NOW.bat", "SDD.txt", "SRS.txt",
    "DISTRIBUTOR_CALCULATOR_DESIGN.md"
)
foreach ($f in $junkFiles) {
    if (Test-Path $f) {
        git rm -f $f 2>$null
        Write-Host "  → حذف $f" -ForegroundColor DarkGray
    }
}
Write-Host "✅ تم" -ForegroundColor Green

# ============================
# Commit و Push
# ============================
Write-Host "`n[7] رفع الهيكل الجديد..." -ForegroundColor Yellow
git add -A
git commit -m "refactor: professional monorepo - backend/ + admin/ + mobile/ + docs/"
git push -u origin main

Write-Host "`n============================================" -ForegroundColor Green
Write-Host " تمت إعادة الهيكلة بنجاح!" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Green
Write-Host "  backend/  ← NestJS API" -ForegroundColor White
Write-Host "  admin/    ← Next.js Dashboard" -ForegroundColor White
Write-Host "  mobile/   ← Android App" -ForegroundColor White
Write-Host "  docs/     ← وثائق المشروع" -ForegroundColor White
