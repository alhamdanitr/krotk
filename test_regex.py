import re

c = "textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface)                         MaterialTheme.colorScheme.primary))"

c = re.sub(r'textStyle\s*=\s*LocalTextStyle\.current\.copy\([^)]+\)\s*MaterialTheme\.colorScheme\.primary\)\)', 
    'textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface))', c)

print(c)
