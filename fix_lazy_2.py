path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# Fix OutlinedTextField
c = c.replace('textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface)                         MaterialTheme.colorScheme.primary))', 'textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface)\n)')
c = c.replace('textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface)                        MaterialTheme.colorScheme.primary))', 'textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface)\n)')

# Fix Components.kt Icons issue
c2path = "kurotek/src/main/java/com/example/ui/theme/Components.kt"
with open(c2path, 'r', encoding='utf-8') as f:
    c2 = f.read()
if 'import androidx.compose.material.icons.Icons' not in c2:
    c2 = c2.replace('package com.example.ui.theme', 'package com.example.ui.theme\nimport androidx.compose.material.icons.Icons')
with open(c2path, 'w', encoding='utf-8') as f:
    f.write(c2)

# Unresolved private/escapeCsv is because LazyColumn was not closed, so top level functions are being parsed as if they are inside SpecialCustomersTab.
# Let's count where SpecialCustomersTab is.
# We had 1 LazyColumn open in `SpecialCustomersTab`. We need one `}` before `private fun exportStockToCsv`.
# The last thing in SpecialCustomersTab is `if (allPendingApprovals.isEmpty()) { item { Card ... } }` and then it should close `LazyColumn` and then close `SpecialCustomersTab`.
# Let's find `exportStockToCsv` and ensure it's outside.
import re
c = re.sub(r'(\n\s*)\}\s*private fun exportStockToCsv', r'\1}\n}\nprivate fun exportStockToCsv', c)

# Let's see if we can find `if (allPendingApprovals.isEmpty()) { ... }` closing.
# A regex to make sure `SpecialCustomersTab` is closed before `private fun exportStockToCsv`.
idx = c.find('private fun exportStockToCsv')
if idx != -1:
    # Look back 5 lines.
    before = c[idx-200:idx]
    if before.count('}') < 3: # Need at least } for LazyColumn and } for Tab
        c = c[:idx] + '}\n}\n' + c[idx:]
        c = c.replace('}\n}\n}\n}\nprivate fun', '}\n}\nprivate fun') # deduplicate if I added too many

# The error output showed:
# 3371:1 Unresolved reference 'private'.
# 3371:13 Anonymous functions with names are prohibited.
# This confirms `private fun` is being parsed inside something else!

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

