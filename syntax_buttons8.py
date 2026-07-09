import re
path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# Fix `label = { Text("الكروت")`
c = c.replace('label = { Text("الكروت")\n                    colors = NavigationBarItemDefaults.colors(', 'label = { Text("الكروت") },\n                    colors = NavigationBarItemDefaults.colors(')

# And `label = { Text("الرئيسية")`? Let's fix all NavigationBarItem's `label`s.
c = c.replace('label = { Text("الرئيسية")\n                    colors = NavigationBarItemDefaults.colors(', 'label = { Text("الرئيسية") },\n                    colors = NavigationBarItemDefaults.colors(')
c = c.replace('label = { Text("العملاء")\n                    colors = NavigationBarItemDefaults.colors(', 'label = { Text("العملاء") },\n                    colors = NavigationBarItemDefaults.colors(')
c = c.replace('label = { Text("الإعدادات")\n                    colors = NavigationBarItemDefaults.colors(', 'label = { Text("الإعدادات") },\n                    colors = NavigationBarItemDefaults.colors(')

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

