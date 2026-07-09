import re
import os

def fix_dashboard():
    path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()

    # 4229: escapeCsv missing -> this might be because of a missing import
    # let's ignore for a second and fix syntax.

    # 4307: `color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 12.dp)`
    # The actual line was `Triple(0, totalUnusedCount, MaterialTheme.colorScheme.primary)` -> maybe some commas missed.
    
    # 4379: Card missing content:
    content = re.sub(r'Card\(\s*colors = androidx.compose.material3.CardDefaults.cardColors\(containerColor = MaterialTheme.colorScheme.primaryContainer\),\s*modifier = Modifier.fillMaxWidth\(\)\s*\)', 'Card(\ncolors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),\nmodifier = Modifier.fillMaxWidth()\n)', content)

    # 4429: `color = if (pending.isAccountCode) (MaterialTheme.colorScheme.secondary),` -> `else MaterialTheme.colorScheme.primary,`
    content = re.sub(r'color = if \(pending\.isAccountCode\) \(MaterialTheme\.colorScheme\.secondary\),', 'color = if (pending.isAccountCode) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,', content)

    # 4482: OutlinedButton( ... MaterialTheme.colorScheme.error else Color.Gray), modifier ...
    content = re.sub(r'MaterialTheme\.colorScheme\.error else Color\.Gray\), modifier = Modifier\.weight\(1f\)\.height\(42\.dp\)', 'colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error), modifier = Modifier.weight(1f).height(42.dp)', content)

    # 4566: KeyboardOptions
    content = content.replace("""                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone, modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface),
                         MaterialTheme.colorScheme.primary
))""", """                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface)
                    )""")

    # 4644: `tint = if (selected) MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp)`
    content = re.sub(r'tint = if \(selected\) MaterialTheme\.colorScheme\.outline, modifier = Modifier\.size\(20\.dp\)', 'tint = if (selected) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp)', content)

    # 4651: `color = if (selected) MaterialTheme.colorScheme.outline`
    content = re.sub(r'color = if \(selected\) MaterialTheme\.colorScheme\.outline$', 'color = if (selected) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface', content, flags=re.MULTILINE)

    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)


def fix_mikrotik():
    path = "kurotek/src/main/java/com/example/ui/MikrotikGeneratorScreen.kt"
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()

    content = content.replace('import androidx.compose.material.icons.automirrored.filled.androidx.compose.material.icons.automirrored.filled.ArrowBack', 'import androidx.compose.material.icons.automirrored.filled.ArrowBack')
    content = content.replace('imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.androidx.compose.material.icons.automirrored.filled.ArrowBack', 'imageVector = Icons.AutoMirrored.Filled.ArrowBack')
    content = content.replace('.background(if (isSelected) MaterialTheme.colorScheme.primary)', '.background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)')
    content = content.replace('.border(\n                                                BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.onSurface),\n', '.border(\n                                                BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent),\n')
    
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

def fix_components():
    path = "kurotek/src/main/java/com/example/ui/theme/Components.kt"
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()

    content = content.replace('imageVector = androidx.compose.material.icons.filled.androidx.compose.material.icons.automirrored.filled.ArrowBack,', 'imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,')
    
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

def fix_kayan():
    pass

fix_dashboard()
fix_mikrotik()
fix_components()

