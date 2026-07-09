import re
path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# 2492: `.background(if (isSuccessfulDistribution) Color(0xFF4CAF50).copy(alpha = 0.1f).copy(alpha = 0.1f))`
# Missing `else` branch
c = c.replace('.background(if (isSuccessfulDistribution) Color(0xFF4CAF50).copy(alpha = 0.1f).copy(alpha = 0.1f))', '.background(if (isSuccessfulDistribution) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color.Red.copy(alpha = 0.1f))')

# 2497: `else Color.Red else Color.Red`
c = c.replace('else Color.Red else Color.Red', 'else Color.Red')


# Let's inspect `2063:67 Unresolved reference 'it'.`
# `2064:35 @Composable invocations can only happen from the context of a @Composable function`
lines = c.split('\n')
for i in range(2055, 2075):
    print(f"{i+1}: {lines[i]}")
print("=========")

