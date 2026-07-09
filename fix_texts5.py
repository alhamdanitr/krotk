import re
path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# Fix Button closing
c = c.replace('} {', '}) {')
c = c.replace('}) {', '}) {') # safe
# Fix if there's any remaining `} {` for buttons inside Row/Column:
c = re.sub(r'Button\([^)]+\)\s*\{\s*', r'Button(\g<1>) {\n', c) 
# The issue was `Button(onClick = { ... } {` which was fixed by `}) {`

# 2801 OutlinedTextField
c = re.sub(r'textStyle = LocalTextStyle.current.copy\(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface\) \n                        MaterialTheme.colorScheme.primary\)\)', 'textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface)\n)', c)

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

