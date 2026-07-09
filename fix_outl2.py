import os
path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('                         MaterialTheme.colorScheme.primary))', '\n)')
with open(path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Done")
