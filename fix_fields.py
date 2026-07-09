import os
import re

def fix_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    out_lines = []
    skip = False
    for line in lines:
        stripped = line.strip()
        if 'focusedLabelColor =' in line or \
           'unfocusedLabelColor =' in line or \
           'focusedTextColor =' in line or \
           'unfocusedTextColor =' in line or \
           'focusedBorderColor =' in line or \
           'unfocusedBorderColor =' in line or \
           'cursorColor =' in line or \
           'containerColor =' in line or \
           'contentColor =' in line or \
           'disabledContainerColor =' in line or \
           'disabledContentColor =' in line:
            # this is a hanging property from a removed colors= block
            continue
        
        # also remove lines that are just `),` if they look like the end of the broken colors block
        # wait, `),` could be the end of OutlinedTextField itself!
        # if the previous lines were skipped, we might need to remove `),` but we can't reliably.
        
        out_lines.append(line)

    content = "".join(out_lines)

    # now fix the `),` that are left over.
    # basically `modifier = Modifier.fillMaxWidth()\n    )` is correct.
    # but `, \n )` is invalid.
    content = re.sub(r',\s*\n\s*\)', '\n)', content)
    
    # fix the Icons.AutoMirrored.Filled.ArrowBack in Components.kt
    content = content.replace('Icons.AutoMirrored.Filled.ArrowBack', 'Icons.Filled.ArrowBack')

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
    print(f"Fixed {filepath}")

def main():
    for root, _, files in os.walk('kurotek/src/main/java/com/example/ui'):
        for file in files:
            if file.endswith('.kt'):
                fix_file(os.path.join(root, file))
    
    for root, _, files in os.walk('kayan_repo/mobile/app/src/main/java/com/example/ui'):
        for file in files:
            if file.endswith('.kt'):
                fix_file(os.path.join(root, file))

if __name__ == '__main__':
    main()
