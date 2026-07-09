import re
import os

path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

content = re.sub(r',\s*MaterialTheme\.colorScheme\.primary\)\)', '\n)', content)

# 3180: Card missing category/code?
# Wait, error: Argument type mismatch: actual type is 'Color', but 'Int' was expected.
# file:///app/applet/kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt:3180:22 Argument type mismatch: actual type is 'Color', but 'Int' was expected.
# Let's check 3180.

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)
