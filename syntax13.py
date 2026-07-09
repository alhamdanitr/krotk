import re
path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# Let's check what's around 2421 "None of the following candidates is applicable: Modifier.background"
lines = c.split('\n')
for i in range(2410, 2430):
    print(f"{i+1}: {lines[i]}")

# Let's also check what's around 2503 and 2508: "if must have both main and else branches"
print("=============")
for i in range(2495, 2515):
    print(f"{i+1}: {lines[i]}")

