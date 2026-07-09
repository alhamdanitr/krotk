import os
import re

def fix_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    original = content

    # Fix broken if else remaining from bad regex
    content = re.sub(r'0\.dp else 4\.dp\),?', '', content)
    content = re.sub(r'Color\(0xFF2C2C2C\) else Color\(0x0D000000\)', '', content)
    content = re.sub(r'Color\(0xFF2C2C2C\)\.copy\(alpha = 0\.5f\) else Color\(0x0A000000\)', '', content)
    content = re.sub(r'Color\.White\.copy\(alpha = 0\.05f\)', '', content)
    content = re.sub(r'Color\.White\.copy\(alpha = 0\.1f\)', '', content)
    content = re.sub(r'if \(isDarkTheme\) Color\(0xFF2D2D2D\) else Color\(0x1F000000\)', '', content)
    content = re.sub(r'if \(isDark\) \s*\n*\s*', '', content)
    
    # Remove empty lines with just commas or spaces
    content = re.sub(r',\s*,', ',', content)
    content = re.sub(r'\(\s*,', '(', content)
    content = re.sub(r',\s*\)', ')', content)
    
    # The syntax error will be things like:
    # Card(
    #    
    #    modifier = ...
    # )
    # This is actually valid Kotlin!
    # The only problem is if there are dangling values like `0.dp else 4.dp)` inside `Card()`.
    # Let's just run compilation and parse errors.

    if content != original:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Fixed {filepath}")

def main():
    for root, _, files in os.walk('kurotek/src/main/java/com/example/ui'):
        for file in files:
            if file.endswith('.kt') and "theme" not in root:
                fix_file(os.path.join(root, file))
    
    for root, _, files in os.walk('kayan_repo/mobile/app/src/main/java/com/example/ui'):
        for file in files:
            if file.endswith('.kt') and "theme" not in root:
                fix_file(os.path.join(root, file))

if __name__ == '__main__':
    main()
