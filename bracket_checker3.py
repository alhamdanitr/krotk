path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# 2801 OutlinedTextField: missing close parenthesis, dangling primary color.
c = c.replace('textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface)                         MaterialTheme.colorScheme.primary))', 'textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface)\n)')
c = c.replace('textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface)                        MaterialTheme.colorScheme.primary))', 'textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface)\n)')
c = c.replace('textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface) \n                        MaterialTheme.colorScheme.primary))', 'textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface)\n)')

# 2807: if without else
c = c.replace('color = if (feedbackSuccess) Color(0xFF4CAF50), modifier = Modifier.fillMaxWidth(),', 'color = if (feedbackSuccess) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth(),')

# 2865: 'if' must have both main and 'else'
# "color = if (transaction.isProfit) Color(0xFF4CAF50)"
c = c.replace('color = if (transaction.isProfit) Color(0xFF4CAF50),', 'color = if (transaction.isProfit) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,')
c = c.replace('color = if (transaction.isProfit) Color(0xFF4CAF50)', 'color = if (transaction.isProfit) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error')


with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

