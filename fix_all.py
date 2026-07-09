import os
import re

def fix_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    original = content
    
    # 1. Colors and properties that were stripped of `if (isDarkTheme)`
    
    # colors = { listOf(...) } else { listOf(...) }
    content = re.sub(r'colors\s*=\s*\{\s*listOf\([^)]*\)\s*\}\s*else\s*\{\s*listOf\([^)]*\)\s*\}', 'colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha=0.15f), Color.Transparent)', content)

    # Color(0xFFE91E63).copy(alpha = 0.3f) else Color(0xFF7B1FA2).copy(alpha = 0.2f)
    content = re.sub(r'Color\(0x[A-F0-9]+\)(?:\.copy\(alpha\s*=\s*[0-9.]+f\))?\s*else\s*Color\(0x[A-F0-9]+\)(?:\.copy\(alpha\s*=\s*[0-9.]+f\))?', 'MaterialTheme.colorScheme.outline', content)
    
    # GlowPurplePink else Color(...)
    content = re.sub(r'GlowPurplePink\s*else\s*Color\([^)]*\)', 'MaterialTheme.colorScheme.primary', content)
    # GlowOrangeGold else Color(...)
    content = re.sub(r'GlowOrangeGold\s*else\s*Color\([^)]*\)', 'MaterialTheme.colorScheme.secondary', content)
    # StatusGreen else SurfaceLight
    content = re.sub(r'StatusGreen\s*else\s*SurfaceLight', 'MaterialTheme.colorScheme.secondaryContainer', content)
    # StatusGreen else MaterialTheme.colorScheme.primary
    content = re.sub(r'StatusGreen\s*else\s*MaterialTheme\.colorScheme\.primary', 'MaterialTheme.colorScheme.secondary', content)
    
    # 2. Leftover color references that are undefined
    content = content.replace('StatusGreen', 'Color(0xFF4CAF50)')
    content = content.replace('SurfaceLight', 'MaterialTheme.colorScheme.surfaceVariant')
    content = content.replace('Category100Cardboard', 'Color(0xFFD7CCC8)')
    content = content.replace('Category200Blue', 'Color(0xFF90CAF9)')
    content = content.replace('Category250Purple', 'Color(0xFFCE93D8)')
    content = content.replace('Category300Green', 'Color(0xFFA5D6A7)')
    content = content.replace('Category500Turmeric', 'Color(0xFFFFCC80)')
    content = content.replace('GlowPurplePink', 'Color(0xFFFF4081)')
    content = content.replace('GlowOrangeGold', 'Color(0xFFFF9800)')
    content = content.replace('PurplePinkGradient', 'Brush.horizontalGradient(listOf(Color(0xFF9C27B0), Color(0xFFFF4081)))')

    # 3. ArrowBack unresolved
    content = content.replace('Icons.Default.ArrowBack', 'androidx.compose.material.icons.automirrored.filled.ArrowBack')
    content = content.replace('ArrowBack', 'androidx.compose.material.icons.automirrored.filled.ArrowBack')
    
    # 4. Button mess in DistributorSystemScreen:
    # Button( onClick = { type = "payment" }, ,modifier = Modifier.weight(1f) )
    content = re.sub(r',\s*,?\s*modifier\s*=', ', modifier =', content)
    # Button( onClick = { type = "payment" }, modifier = Modifier.weight(1f) )
    # which might have had the color removed leaving a dangling comma.
    
    # 5. TextField missing colors syntax error.
    # It might be in MainDashboardScreen around 1599:
    # OutlinedTextField(value = ..., onValueChange = ..., colors = { TextFieldDefaults.outlinedTextFieldColors(...) } else { ... })
    content = re.sub(r'colors\s*=\s*\{\s*TextFieldDefaults\.outlinedTextFieldColors\([^}]*\)\s*\}\s*else\s*\{\s*TextFieldDefaults\.outlinedTextFieldColors\([^}]*\)\s*\}', 'colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors()', content)
    content = re.sub(r'colors\s*=\s*TextFieldDefaults\.outlinedTextFieldColors\([^)]*\)', 'colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors()', content)
    
    # 6. Modifier.border(BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline))
    # It might have missing commas or weird brackets.
    
    # 7. MainActivity MaterialTheme reference
    if 'MainActivity.kt' in filepath:
        if 'import androidx.compose.material3.MaterialTheme' not in content:
            content = content.replace('import androidx.compose.material3.Surface', 'import androidx.compose.material3.Surface\nimport androidx.compose.material3.MaterialTheme')
            
    if content != original:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Fixed {filepath}")

def main():
    dirs = ['kurotek/src/main/java/com/example', 'kayan_repo/mobile/app/src/main/java/com/example']
    for d in dirs:
        for root, _, files in os.walk(d):
            for file in files:
                if file.endswith('.kt'):
                    fix_file(os.path.join(root, file))

if __name__ == '__main__':
    main()
