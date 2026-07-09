import re
path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# I see a LOT of "Unresolved reference 'MaterialTheme'" -> actually those are "Syntax error: Expecting an element."
# What is the actual code around 2802 and 2782 and 2791?
# It's the same error from before with `OutlinedTextField`.
# `textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface) \n MaterialTheme.colorScheme.primary))`
# Let's clean it up properly.
c = re.sub(r'textStyle = LocalTextStyle\.current\.copy\(textAlign = TextAlign\.Right, color = MaterialTheme\.colorScheme\.onSurface\)\s*MaterialTheme\.colorScheme\.primary\)\)', 'textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface)\n)', c)

# Fix OrangeGoldGradient
c = c.replace('OrangeGoldGradient', 'Color(0xFFFFD700)')
c = c.replace('GoldAccent', 'Color(0xFFFFD700)')

# Fix `if` must have both main and 'else' branches when used as an expression.
c = c.replace('color = if (transaction.isProfit) Color(0xFF4CAF50),', 'color = if (transaction.isProfit) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,')
c = c.replace('color = if (transaction.isProfit) Color(0xFF4CAF50)', 'color = if (transaction.isProfit) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error')
c = c.replace('color = if (isDarkTheme) MaterialTheme.colorScheme.primary,', 'color = if (isDarkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,')
c = c.replace('color = if (isDarkTheme) MaterialTheme.colorScheme.primary\n', 'color = if (isDarkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary\n')
c = c.replace('tint = if (isDarkTheme) MaterialTheme.colorScheme.secondary,', 'tint = if (isDarkTheme) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.secondary,')


with open(path, 'w', encoding='utf-8') as f:
    f.write(c)
