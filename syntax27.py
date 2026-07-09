import re
path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# Fix Card(, modifier -> Card(modifier
c = c.replace('Card(, modifier = Modifier', 'Card(modifier = Modifier')

# Is there any other `Card(,*`?
c = re.sub(r'Card\(\s*,', 'Card(', c)

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

