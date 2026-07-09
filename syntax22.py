import re
path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# Make sure it actually replaced.
lines = c.split('\n')
for i in range(2095, 2115):
    if i < len(lines):
        print(f"{i+1}: {lines[i]}")

