import os
import re

def fix_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    original = content
    
    # Fix dangling parentheses in Card components
    # Card(\n )) -> Card(
    # Card(\n ) -> Card(
    content = re.sub(r'Card\(\s*\)\),', 'Card(', content)
    content = re.sub(r'Card\(\s*\),', 'Card(', content)
    content = re.sub(r'Card\(\s*\)', 'Card(', content)
    content = re.sub(r'Card\(\s*modifier = Modifier', 'Card(modifier = Modifier', content)

    # Re-add } for missing `{` if it happens? No, `Card(...) {` was probably intact but the `)` was misplaced before the modifier
    content = re.sub(r'Card\(\s*\)\),?\s*modifier', 'Card(modifier', content)
    content = re.sub(r'Card\(\s*\),?\s*modifier', 'Card(modifier', content)

    if content != original:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Fixed {filepath}")

def main():
    for root, _, files in os.walk('kurotek/src/main/java/com/example'):
        for file in files:
            if file.endswith('.kt'):
                fix_file(os.path.join(root, file))
    
    for root, _, files in os.walk('kayan_repo/mobile/app/src/main/java/com/example'):
        for file in files:
            if file.endswith('.kt'):
                fix_file(os.path.join(root, file))

if __name__ == '__main__':
    main()
