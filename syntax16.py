import re
path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# Let's inspect where `Anonymous functions with names are prohibited.` could be.
# It was at line 2624 earlier, which might be shifted. Let's look at lines around 2620.
# The error `2624:5 Anonymous functions with names are prohibited.` might be because a function was declared inside another without the `fun` keyword properly formatted, e.g., missing something.
# Wait, look at 2620:
# Is it `fun SettingsTab`?
# Let's grep for `fun` between 2600 and 2640.
lines = c.split('\n')
for i in range(2550, 2650):
    if 'fun ' in lines[i] and len(lines[i]) < 100:
        print(f"{i+1}: {lines[i]}")

