import re
path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# Fix the button syntax around 2101. It should be a Button(...) with modifier.
# From the look of it, it was:
# Button(onClick = { ... }, colors = ButtonDefaults.buttonColors(...), modifier = Modifier.weight(1f).height(44.dp)) { Text(...) }
# Let's just strip out the mangled colors completely.
c = re.sub(r'MaterialTheme\.colorScheme\.secondary\),\s*MaterialTheme\.colorScheme\.secondary\)\.copy\(alpha = 0\.3f\)\), modifier = Modifier\s*\.weight\(1f\)\s*\.height\(44\.dp\)', 'modifier = Modifier.weight(1f).height(44.dp)', c)
c = re.sub(r'MaterialTheme\.colorScheme\.secondary\),\s*MaterialTheme\.colorScheme\.secondary\)\.copy\(alpha = 0\.3f\)\),\s*modifier = Modifier\s*\.weight\(1f\)\s*\.height\(44\.dp\)', 'modifier = Modifier.weight(1f).height(44.dp)', c)

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)
