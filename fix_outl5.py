import re
path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

c = re.sub(r'textStyle\s*=\s*LocalTextStyle\.current\.copy\([^)]+\)\s*MaterialTheme\.colorScheme\.primary\)\)', 
    'textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface))', c)

c = re.sub(r',\s*MaterialTheme\.colorScheme\.primary\)\)', '\n)', c)

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

