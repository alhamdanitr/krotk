import re
path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# Fix the OutlinedTextField at 2061:
c = re.sub(r'textStyle = LocalTextStyle\.current\.copy\(textAlign = TextAlign\.Center, color = MaterialTheme\.colorScheme\.onSurface\),\s*MaterialTheme\.colorScheme\.secondary\s*\)\)', 'textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)\n)', c)


# And around 2102:
# `2102:30 Argument type mismatch: actual type is 'Color', but 'Modifier' was expected.`
lines = c.split('\n')
for i in range(2095, 2115):
    if i < len(lines):
        print(f"{i+1}: {lines[i]}")

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

