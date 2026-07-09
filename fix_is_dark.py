import os
import re

def fix_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    original = content
    content = content.replace('isDarkThemeState.value', 'androidx.compose.foundation.isSystemInDarkTheme()')
    
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
