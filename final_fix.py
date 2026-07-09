import re

path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

# Fix OutlinedTextField closing error correctly
content = content.replace('textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface),\n                         MaterialTheme.colorScheme.primary))', 'textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface)\n)')

# Just catch it generally
content = re.sub(r',\s*MaterialTheme\.colorScheme\.primary\)\)', '\n)', content)

# 3121 & 3160 & 3171: item/items without implicit receiver.
# They are inside SpecialCustomersTab. Wait, if it's missing a LazyColumn wrap...
# Oh! LazyColumn { ... } was closed early because of dangling brackets!
# Let's count brackets in SpecialCustomersTab... this is too complex for regex.
# Actually, I'll just check if LazyColumn closed early.

# 3522: 'if' must have both main and 'else'
content = re.sub(r'color = if \(pending\.isAccountCode\) \(MaterialTheme\.colorScheme\.secondary\),', 'color = if (pending.isAccountCode) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,', content)

# 3652: KeyboardOptions issue again
content = re.sub(r'keyboardOptions = androidx\.compose\.foundation\.text\.KeyboardOptions\([^}]*MaterialTheme\.colorScheme\.primary\s*\)\)',
                 r'keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone),\nmodifier = Modifier.fillMaxWidth(),\ntextStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface)\n)', content)


# 3679: confirmButton closing
content = content.replace('} {\n                    Text("حفظ وربط ✔"', '}) {\n                    Text("حفظ وربط ✔"')


# Components.kt Unresolved reference 'Icons'.
# because I replaced Icons.AutoMirrored.Filled... with Icons.AutoMirrored... but maybe didn't import Icons.

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)

# Fix Components.kt
path2 = "kurotek/src/main/java/com/example/ui/theme/Components.kt"
with open(path2, 'r', encoding='utf-8') as f:
    c2 = f.read()
if 'import androidx.compose.material.icons.Icons' not in c2:
    c2 = 'import androidx.compose.material.icons.Icons\n' + c2
with open(path2, 'w', encoding='utf-8') as f:
    f.write(c2)

print("done")
