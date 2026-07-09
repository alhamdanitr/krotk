import re
path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

def check_brackets(text):
    stack = []
    for i, char in enumerate(text):
        if char == '{':
            stack.append(i)
        elif char == '}':
            if stack:
                stack.pop()
            else:
                return f"Extra closing brace at {i}"
    if stack:
        return f"Unclosed brace at {stack[-1]}"
    return "Balanced"

print(check_brackets(c))

