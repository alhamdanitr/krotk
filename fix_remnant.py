import re
import os

def fix_file():
    path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Fix OutlinedTextField dangling colors
    content = re.sub(r'textStyle = LocalTextStyle\.current\.copy\(textAlign = TextAlign\.Right, color = MaterialTheme\.colorScheme\.onSurface\),\s*MaterialTheme\.colorScheme\.primary\)\)', 
                     'textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface)\n)', content)

    # Fix Button confirmButton
    content = content.replace('} {\n                    Text("حفظ وربط ✔"', '}) {\n                    Text("حفظ وربط ✔"')
    
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

fix_file()
