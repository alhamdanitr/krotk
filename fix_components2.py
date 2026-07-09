import os
path = "kurotek/src/main/java/com/example/ui/theme/Components.kt"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

c = c.replace('import androidx.compose.material.icons.Icons\npackage', 'package')
c = c.replace('import androidx.compose.material.icons.Icons\nimport androidx.compose.material.icons.Icons', 'import androidx.compose.material.icons.Icons')

# "medium" and "small" unresolved reference usually means we need:
# import androidx.compose.material3.MaterialTheme
# or it's referring to MaterialTheme.shapes.medium
c = c.replace('shape = medium', 'shape = androidx.compose.material3.MaterialTheme.shapes.medium')
c = c.replace('shape = small', 'shape = androidx.compose.material3.MaterialTheme.shapes.small')

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

