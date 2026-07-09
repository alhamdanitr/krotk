import os
import re

def fix_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    original = content
    
    # Fix the missing },
    content = content.replace('Text("توليد الكروت")\n', 'Text("توليد الكروت") },\n')
    content = content.replace('Text("الكروت المتاحة (${generatedCards.filter { !it.transferred }.size})")\n', 'Text("الكروت المتاحة (${generatedCards.filter { !it.transferred }.size})") },\n')
    content = content.replace('Text("إعداد المايكروتك")\n', 'Text("إعداد المايكروتك") },\n')
    
    if content != original:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Fixed texts in {filepath}")

def main():
    dirs = ['kurotek/src/main/java/com/example/ui', 'kayan_repo/mobile/app/src/main/java/com/example/ui']
    for d in dirs:
        for root, _, files in os.walk(d):
            for file in files:
                if file.endswith('.kt'):
                    fix_file(os.path.join(root, file))

if __name__ == '__main__':
    main()
