import re
path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# I see it closes at 312: `}`. But `MainDashboardScreen` needs one more `}`? Let's check `MainDashboardScreen` block brackets from 60 to 317.
def check_brackets_range(text, start, end):
    sub = text[start:end]
    stack = []
    for i, char in enumerate(sub):
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

home_idx = c.find('fun HomeTab(')
print(check_brackets_range(c, 0, home_idx))

