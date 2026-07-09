import re
path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# Fix Card syntax at 2392
# The python regex earlier completely mangled Card attributes.
# Let's replace the mangled attributes with something basic.
c = re.sub(r'Card\(\s+0\.dp else 1\.5\.dp\), modifier = Modifier\.fillMaxWidth\(\)\s+\)', 'Card(modifier = Modifier.fillMaxWidth())', c)

# 2482 Card
c = re.sub(r'Card\(\s+MaterialTheme\.colorScheme\.outline\),\s+0\.dp else 1\.5\.dp\), modifier = Modifier\.fillMaxWidth\(\)\s+\)', 'Card(modifier = Modifier.fillMaxWidth())', c)

# Are there any other mangled Cards?
c = re.sub(r'Card\(\s+0\.dp else 1\.5\.dp\), modifier = Modifier\.fillMaxWidth\(\)', 'Card(modifier = Modifier.fillMaxWidth()', c)
c = re.sub(r'Card\(\s+MaterialTheme\.colorScheme\.outline\),\s+0\.dp else 1\.5\.dp\), modifier = Modifier\.fillMaxWidth\(\)', 'Card(modifier = Modifier.fillMaxWidth()', c)


with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

