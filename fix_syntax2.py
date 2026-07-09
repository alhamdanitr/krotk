import os
import re

def fix_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # The problem:
    # label = { Text("...") }),
    # modifier = Modifier.fillMaxWidth()
    # It should be:
    # label = { Text("...") },
    # modifier = Modifier.fillMaxWidth()
    
    # Let's fix this specifically: `}),\n *modifier` -> `},\nmodifier`
    content = re.sub(r'\}\),\s*modifier = Modifier', '},\n                                        modifier = Modifier', content)
    
    # Sometimes it's `VisualTransformation.None else PasswordVisualTransformation(),\n trailingIcon = {`
    # Let's just fix `}),` -> `},` if it's followed by a parameter like `modifier =`
    content = re.sub(r'\}\),\s*([a-zA-Z]+ = )', r'},\n\1', content)
    
    # Another issue: `No value passed for parameter 'content'` on Card/Button.
    # This means the script stripped `) {` or `, content = {` ?
    # No, it means the script stripped the `)` from `colors = ...)` but it also stripped the `{` of the block?
    # Let's check line 274 of MikrotikGeneratorScreen: 
    # `Syntax error: Unexpected tokens (use ';' to separate expressions on the same line).`
    # Unresolved reference 'textStyle'.

    # Let's just replace `}),\n` with `},\n` if it's breaking things?
    # Actually, the original block was:
    # colors = OutlinedTextFieldDefaults.colors(
    #    ...
    # ),
    # When I removed `colors = ... (`, the `),` remained. So I have `),` floating.
    content = re.sub(r',\n\s*\),', ',', content)
    content = re.sub(r'\n\s*\),\n\s*modifier = Modifier', ',\nmodifier = Modifier', content)
    content = re.sub(r'\n\s*\),\n\s*textStyle = ', ',\ntextStyle = ', content)
    content = re.sub(r'\n\s*\),\n\s*trailingIcon = ', ',\ntrailingIcon = ', content)
    content = re.sub(r'\n\s*\),\n\s*label = ', ',\nlabel = ', content)
    content = re.sub(r'\n\s*\),\n\s*placeholder = ', ',\nplaceholder = ', content)

    # Let's clean up dangling `),` that precede `{`
    content = re.sub(r'\n\s*\),\n\s*\{', ' {\n', content)
    content = re.sub(r'\n\s*\)\n\s*\{', ' {\n', content)
    
    # Fix `Unresolved reference 'colors'` in Components.kt
    content = content.replace('colors = colors,', '')

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
