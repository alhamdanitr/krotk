import re
path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# "Unclosed brace at 5672"
# Let's find out what line is index 5672
line_num = c[:5672].count('\n') + 1
col_num = 5672 - c.rfind('\n', 0, 5672)
print(f"Line: {line_num}, Col: {col_num}")

# Let's see what's at line 147
for i, line in enumerate(c.split('\n')):
    if i+1 in range(line_num - 5, line_num + 5):
        print(f"{i+1}: {line}")

