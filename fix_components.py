path = "kurotek/src/main/java/com/example/ui/theme/Components.kt"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# Add import for Icons.AutoMirrored.Filled.ArrowBack
if 'import androidx.compose.material.icons.automirrored.filled.ArrowBack' not in c:
    c = c.replace('import androidx.compose.material.icons.Icons', 'import androidx.compose.material.icons.Icons\nimport androidx.compose.material.icons.automirrored.filled.ArrowBack')

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

