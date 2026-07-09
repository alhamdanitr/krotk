import re
path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# Ah! `label = { Text("التفويضات")` missing closing `}`.
# And colors comes right after, so it swallowed everything.
# Let's fix that!
c = c.replace('label = { Text("التفويضات")\n                    colors = NavigationBarItemDefaults.colors(', 'label = { Text("التفويضات") },\n                    colors = NavigationBarItemDefaults.colors(')

# And wait, are there other missing brackets?
# Let's check brackets again on the new content.
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

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

