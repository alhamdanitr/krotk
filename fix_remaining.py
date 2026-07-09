import os
import re

def fix_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    original = content
    
    # MaterialTheme.colorScheme.primary else Color(0xFF00796B) -> MaterialTheme.colorScheme.primary
    content = re.sub(r'MaterialTheme\.colorScheme\.primary\.copy\([^)]*\)\s*else\s*Color\([^)]*\)', 'MaterialTheme.colorScheme.primaryContainer', content)
    content = re.sub(r'MaterialTheme\.colorScheme\.primary\s*else\s*Color\([^)]*\)', 'MaterialTheme.colorScheme.primary', content)
    content = re.sub(r'MaterialTheme\.colorScheme\.secondary\s*else\s*Color\([^)]*\)', 'MaterialTheme.colorScheme.secondary', content)
    content = re.sub(r'MaterialTheme\.colorScheme\.secondaryContainer\s*else\s*Color\([^)]*\)', 'MaterialTheme.colorScheme.secondaryContainer', content)

    # Some weird ones:
    # MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color(0x3BFFCC80)), modifier = Modifier.fillMaxWidth()
    # It has a dangling `),` or similar. Let's just catch `else Color(...)` and remove it globally.
    content = re.sub(r'\s*else\s*Color\([^)]*\)', '', content)
    content = re.sub(r'\s*else\s*MaterialTheme\.colorScheme\.[a-zA-Z]+', '', content)
    
    # Fix Card missing parameters:
    # Card(
    #     MaterialTheme.colorScheme.primaryContainer), modifier = Modifier.fillMaxWidth()
    # )
    # This was likely `colors = CardDefaults.cardColors(containerColor = ...)`
    content = re.sub(r'Card\(\s*MaterialTheme\.colorScheme\.primaryContainer\)\s*,', 'Card(colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), ', content)

    # Missing parameter 'content' for Card:
    # Card(
    #    modifier = Modifier.fillMaxWidth()
    # )
    content = re.sub(r'Card\(\s*modifier\s*=\s*Modifier\.fillMaxWidth\(\)\s*\)', 'Card(\nmodifier = Modifier.fillMaxWidth()\n)', content)
    
    # If it's a Button:
    # Button(
    #     onClick = { },
    #     MaterialTheme.colorScheme.primary
    #     modifier = Modifier
    # )
    # Let's fix that.
    
    if content != original:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Fixed leftover elses in {filepath}")

def main():
    dirs = ['kurotek/src/main/java/com/example', 'kayan_repo/mobile/app/src/main/java/com/example']
    for d in dirs:
        for root, _, files in os.walk(d):
            for file in files:
                if file.endswith('.kt'):
                    fix_file(os.path.join(root, file))

if __name__ == '__main__':
    main()
