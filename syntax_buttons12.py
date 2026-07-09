import re
path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# `fun HomeTab` starts at line 318.
# Let's inspect the lines right before 318.
lines = c.split('\n')
for i in range(300, 320):
    print(f"{i+1}: {lines[i]}")

