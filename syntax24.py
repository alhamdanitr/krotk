import re
path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# Fix the `else Color.Red else Color.Red` issue
c = c.replace('else Color.Red else Color.Red', 'else Color.Red')

# What about 1569?
# "1569:60 Syntax error: Unexpected tokens (use ';' to separate expressions on the same line)."
# "1570:21 Syntax error: Expecting an element."
# "1590:26 Argument type mismatch: actual type is 'Color', but 'Modifier' was expected."
# "1684:26 Argument type mismatch: actual type is 'Color', but 'Modifier' was expected."
print("--- 1565 ---")
lines = c.split('\n')
for i in range(1565, 1575):
    print(f"{i+1}: {lines[i]}")
print("--- 1585 ---")
for i in range(1585, 1595):
    print(f"{i+1}: {lines[i]}")
print("--- 1680 ---")
for i in range(1680, 1690):
    print(f"{i+1}: {lines[i]}")

