import os
import re

def fix_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    original = content
    
    # 1. Colors and general replacements
    content = content.replace('GlowEmeraldGreen', 'MaterialTheme.colorScheme.primary')
    content = content.replace('EmeraldGreenGradient', 'MaterialTheme.colorScheme.primary')
    content = content.replace('DeepBlack', 'MaterialTheme.colorScheme.background')
    content = content.replace('SurfaceDark', 'MaterialTheme.colorScheme.surface')
    content = content.replace('GoldPrimary', 'MaterialTheme.colorScheme.primary')
    content = content.replace('PureWhite', 'MaterialTheme.colorScheme.onSurface')
    content = content.replace('if (isSelected) Color.White else )', 'if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline)')

    # 2. Fix the Card() { content } syntax errors
    # If there's `Card(\n  modifier = ...` missing the closing `) {` or with dangling `))` 
    content = re.sub(r'Card\(\s*\)\),?\s*modifier', 'Card(modifier', content)
    content = re.sub(r'Card\(\s*\),?\s*modifier', 'Card(modifier', content)
    
    # Also I'll replace `Icons.Filled.ArrowBack` with standard one in Components if needed.
    content = content.replace('androidx.compose.material.icons.Icons.Filled.ArrowBack', 'androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack')
    content = content.replace('Icons.Filled.ArrowBack', 'androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack')

    # Also wait, what about the `KeyboardOptions(...)` error?
    content = re.sub(r',\s*textStyle\s*=\s*MaterialTheme.typography.bodyLarge', '', content)
    content = re.sub(r',\s*fontSize\s*=\s*.*?\.sp', '', content)
    content = re.sub(r',\s*fontWeight\s*=\s*FontWeight\..*?,', ',', content)
    content = re.sub(r',\s*textAlign\s*=\s*TextAlign\..*?,', ',', content)
    
    # Check for OutlinedTextField ending with `, \n modifier = `
    content = re.sub(r'\},\n\s*modifier = Modifier', '},\nmodifier = Modifier', content)

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
