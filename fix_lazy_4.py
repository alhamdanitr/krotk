import re
path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# 2896: if without else
c = c.replace('color = if (isDarkTheme) MaterialTheme.colorScheme.primary,', 'color = if (isDarkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,')
c = c.replace('color = if (isDarkTheme) MaterialTheme.colorScheme.primary\n', 'color = if (isDarkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary\n')

# 2899: GoldAccent
c = c.replace('HorizontalDivider(color = GoldAccent.copy(alpha = 0.15f))', 'HorizontalDivider(color = Color(0xFFFFD700).copy(alpha = 0.15f))')

# 2911: if without else
c = c.replace('tint = if (isDarkTheme) MaterialTheme.colorScheme.secondary,', 'tint = if (isDarkTheme) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.secondary,')

# 2931: Syntax error: Expecting ','
c = c.replace('onClick = { openWhatsApp(context) } {', 'onClick = { openWhatsApp(context) }) {')

# 2997: OutlinedTextField
c = c.replace('textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface)                         MaterialTheme.colorScheme.primary))', 'textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface)\n)')

# 3068, 3079: OutlinedTextField
c = c.replace('textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface) \n                        MaterialTheme.colorScheme.primary))', 'textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onSurface)\n)')

# 3252, 3308, 3380: Button missing ) {
c = c.replace('onClick = { viewModel.approvePending(pending.id, null) } {', 'onClick = { viewModel.approvePending(pending.id, null) }) {')
c = c.replace('onClick = { viewModel.deletePendingApproval(pending.id) } {', 'onClick = { viewModel.deletePendingApproval(pending.id) }) {')
c = c.replace('onClick = { viewModel.deleteTransaction(transaction.id) } {', 'onClick = { viewModel.deleteTransaction(transaction.id) }) {')

c = re.sub(r',\s*MaterialTheme\.colorScheme\.primary\)\)', '\n)', c)

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)
