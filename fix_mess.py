import re
import os

def fix_file():
    path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()

    # 4307 & 4056 & 4044: Card with missing params and weird numbers
    # Let's fix missing `colors = CardDefaults.cardColors(...)` and missing `elevation` and missing `shape`.
    content = re.sub(r'Card\(\s*MaterialTheme\.colorScheme\.outline\),\s*0\.dp else 2\.dp\), modifier =', 'Card(\nborder = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),\nmodifier =', content)

    content = re.sub(r'Card\(\s*MaterialTheme\.colorScheme\.outline\), modifier =', 'Card(\nborder = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),\nmodifier =', content)
    
    # 4381: Surface with missing params
    # Surface( color = ..., .copy(alpha=0.3f))
    content = re.sub(r'Surface\(\s*color\s*=\s*Color\([^)]*\)\.copy\(alpha\s*=\s*0\.15f\),\s*\.copy\(alpha\s*=\s*0\.3f\)\)\s*\)', 'Surface( color = Color(0xFFD32F2F).copy(alpha = 0.15f) )', content)
    
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

fix_file()
