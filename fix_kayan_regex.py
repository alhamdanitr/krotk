import re

with open('kayan_repo/mobile/app/src/main/java/com/example/ui/MainDashboardScreen.kt', 'r') as f:
    content = f.read()

# The pattern is:
# Card(
# [whitespace]
# MaterialTheme.colorScheme.outline),
# [whitespace]
# 0.dp else 2.dp), modifier = [something]
# )

pattern = re.compile(r'Card\(\s*MaterialTheme\.colorScheme\.outline\),\s*0\.dp else 2\.dp\), (modifier = [^\n\)]+)\s*\)', re.MULTILINE)

content = pattern.sub(r'Card(border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline), \1)', content)

with open('kayan_repo/mobile/app/src/main/java/com/example/ui/MainDashboardScreen.kt', 'w') as f:
    f.write(content)
