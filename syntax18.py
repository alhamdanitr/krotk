import re
path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# Let's fix the OutlinedTextField instances.
c = re.sub(r'textStyle = LocalTextStyle\.current\.copy\(textAlign = TextAlign\.Right, color = MaterialTheme\.colorScheme\.onSurface\)\s*MaterialTheme\.colorScheme\.primary\)\)', 'textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface)\n)', c)

# I saw some OutlinedTextField instances missing `)` altogether when mangled.
c = c.replace('textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface) \n                        MaterialTheme.colorScheme.primary))', 'textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface)\n)')
# Basically, replacing all occurrences of:
# `textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface)`
# followed by some whitespace and `MaterialTheme.colorScheme.primary))`
# I can just use a regex
c = re.sub(r'textStyle\s*=\s*LocalTextStyle\.current\.copy\(textAlign\s*=\s*TextAlign\.Right,\s*color\s*=\s*MaterialTheme\.colorScheme\.onSurface\)\s+MaterialTheme\.colorScheme\.primary\)\)', 'textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface)\n)', c)


# The syntax error at `2497:108 Syntax error: Expecting ')'`
# Let's look at what's there
lines = c.split('\n')
for i in range(2490, 2505):
    print(f"{i+1}: {lines[i]}")


