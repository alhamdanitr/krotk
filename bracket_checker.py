path = "kurotek/src/main/java/com/example/ui/theme/Components.kt"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# Fix Unresolved ArrowBack: it means `Icons.AutoMirrored.Filled.ArrowBack` or `Icons.Default.ArrowBack` is not imported, or it's used nakedly as `ArrowBack`.
idx = c.find('ArrowBack')
print(f"ArrowBack usage in Components.kt:")
for line in c.split('\n'):
    if 'ArrowBack' in line:
        print(line)

