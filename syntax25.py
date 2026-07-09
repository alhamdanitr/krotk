import re
path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# Fix the button modifiers
c = re.sub(r'MaterialTheme\.colorScheme\.primary\),\s*modifier = Modifier\.fillMaxWidth\(\)\.height\(44\.dp\)', 'modifier = Modifier.fillMaxWidth().height(44.dp)', c)
c = re.sub(r'MaterialTheme\.colorScheme\.secondary\),\s*modifier = Modifier\.fillMaxWidth\(\)\.height\(44\.dp\)', 'modifier = Modifier.fillMaxWidth().height(44.dp)', c)
c = re.sub(r'MaterialTheme\.colorScheme\.outline\),\s*modifier = Modifier\.weight\(1f\)\.height\(46\.dp\)', 'modifier = Modifier.weight(1f).height(46.dp)', c)

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

