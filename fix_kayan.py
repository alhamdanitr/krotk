import re

with open('kayan_repo/mobile/app/src/main/java/com/example/ui/MainDashboardScreen.kt', 'r') as f:
    content = f.read()

# Fix Card broken borders
content = re.sub(
    r'Card\(\s*-\s*MaterialTheme\.colorScheme\.outline\),\s*0\.dp else 2\.dp\), modifier = (.*?)\)',
    r'Card(border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = \1)',
    content
)

with open('kayan_repo/mobile/app/src/main/java/com/example/ui/MainDashboardScreen.kt', 'w') as f:
    f.write(content)
