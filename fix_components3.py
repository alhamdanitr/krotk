path = "kurotek/src/main/java/com/example/ui/theme/Components.kt"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

if 'import androidx.compose.material.icons.Icons' not in c:
    c = c.replace('package', 'import androidx.compose.material.icons.Icons\npackage')
    
c = c.replace('import androidx.compose.material.icons.Icons\npackage', 'package')
if 'import androidx.compose.material.icons.Icons' not in c:
    c = c.replace('import androidx.compose.material.icons.automirrored.filled.ArrowBack', 'import androidx.compose.material.icons.Icons\nimport androidx.compose.material.icons.automirrored.filled.ArrowBack')

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

