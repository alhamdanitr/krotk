import re
path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()
    
# Let's fix the other missing else branches.
c = c.replace('color = if (transaction.isProfit) Color(0xFF4CAF50),', 'color = if (transaction.isProfit) Color(0xFF4CAF50) else Color.Red,')
c = c.replace('color = if (transaction.isProfit) Color(0xFF4CAF50)', 'color = if (transaction.isProfit) Color(0xFF4CAF50) else Color.Red')

c = c.replace('if (isDarkTheme) MaterialTheme.colorScheme.primary,', 'if (isDarkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,')
c = c.replace('if (isDarkTheme) MaterialTheme.colorScheme.primary\n', 'if (isDarkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary\n')
c = c.replace('if (isDarkTheme) MaterialTheme.colorScheme.secondary,', 'if (isDarkTheme) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.secondary,')
c = c.replace('if (isDarkTheme) MaterialTheme.colorScheme.secondary\n', 'if (isDarkTheme) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.secondary\n')

# Check 2624: "Anonymous functions with names are prohibited."
lines = c.split('\n')
print("--- 2624 ---")
for i in range(2620, 2630):
    if i < len(lines):
        print(f"{i+1}: {lines[i]}")

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)
