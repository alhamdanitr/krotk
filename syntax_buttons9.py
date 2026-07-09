import re
path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

line_num = c[:2261].count('\n') + 1
print(f"Line: {line_num}")
for i, line in enumerate(c.split('\n')):
    if i+1 in range(line_num - 5, line_num + 5):
        print(f"{i+1}: {line}")

