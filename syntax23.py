import re
path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# Let's fix the OutlinedTextField instances. Again!
# They were:
# textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface) \n MaterialTheme.colorScheme.primary))
c = re.sub(r'textStyle\s*=\s*LocalTextStyle\.current\.copy\(textAlign\s*=\s*TextAlign\.Right,\s*color\s*=\s*MaterialTheme\.colorScheme\.onSurface\)\n\s*MaterialTheme\.colorScheme\.primary\)\)', 'textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface)\n)', c)

# 2492: `.background(if (isSuccessfulDistribution) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color.Red.copy(alpha = 0.1f))`
# But there was another error at 2493:108 Syntax error: Expecting ')' -> meaning the previous fix didn't apply properly or had extra stuff.
lines = c.split('\n')
print("--- 2490 ---")
for i in range(2490, 2498):
    if i < len(lines):
        print(f"{i+1}: {lines[i]}")


