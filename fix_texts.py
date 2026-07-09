import re

path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# 3015: label = { Text(option, -> Text(option)
c = c.replace('label = { Text(option,\n\ncolors', 'label = { Text(option) },\ncolors')
c = c.replace('label = { Text(option, \n\ncolors', 'label = { Text(option) },\ncolors')

# 3070, 3081: OutlinedTextField weird Text issue
# The error was: 3070:26 Unresolved reference 'MaterialTheme'. 
# Because I replaced ` MaterialTheme.colorScheme.primary))` with `)` but it messed up something?
# Let's fix missing closing parens on FilterChip
c = c.replace(') {                                Text(if (option == "جيب") "محفظة كاش" else option,', ') {\n                                Text(if (option == "جيب") "محفظة كاش" else option)')


with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

