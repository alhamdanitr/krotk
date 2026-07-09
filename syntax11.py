import re
path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# Let's fix the remaining syntax errors in 2392-2500.
# 2392:21 No value passed for parameter 'code'.
# `e: file:///app/applet/kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt:2394:26 Argument type mismatch: actual type is 'Dp', but 'Int' was expected.`
lines = c.split('\n')
for i in range(2385, 2400):
    print(f"{i+1}: {lines[i]}")
print("=========")
# `2484:22 Argument type mismatch: actual type is 'Color', but 'Int' was expected.`
# `2484:22 No value passed for parameter 'category'.`
for i in range(2480, 2490):
    print(f"{i+1}: {lines[i]}")

