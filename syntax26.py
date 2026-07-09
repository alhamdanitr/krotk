import re
path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()
    
# Let's check 1653: `1653:22 Syntax error: Expecting an argument.`
lines = c.split('\n')
print("--- 1650 ---")
for i in range(1650, 1660):
    if i < len(lines):
        print(f"{i+1}: {lines[i]}")

