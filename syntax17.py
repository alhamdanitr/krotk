import re
path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# Let's inspect `fun SettingsTab(` at 2614.
lines = c.split('\n')
for i in range(2610, 2625):
    print(f"{i+1}: {lines[i]}")

