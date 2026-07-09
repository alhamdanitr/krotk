import re
path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# Why is exportCustomerTransactionsToCsv unresolved if it's defined right after?
# Let's search where `exportCustomerTransactionsToCsv` is DEFINED.
for i, line in enumerate(c.split('\n')):
    if 'fun exportCustomerTransactionsToCsv' in line:
        print(f"Defined at {i+1}: {line}")

# Then why does it complain? Let's check `3252:1 Mixing named and positional arguments is not allowed unless the order of the arguments matches the order of the parameters.`
# And `3252:12 Anonymous functions with names are prohibited.`
# `3252:87 Syntax error: Expecting ','`
# Line 3252 is: `fun String.escapeCsv(): String { return this.replace("\"", "\"\"").let { "\"$it\"" } }`
# This error means that `fun String.escapeCsv()` is inside a function call that was never closed.
# The `tab_content.count('{') == 56` is for `SpecialCustomersTab`. BUT maybe an earlier function like `LazyColumn` wasn't closed properly and it ate the end of `SpecialCustomersTab` and everything after!
# Let's check the curly braces for the entire file.

def check_brackets(text):
    stack = []
    for i, char in enumerate(text):
        if char == '{':
            stack.append(i)
        elif char == '}':
            if stack:
                stack.pop()
            else:
                return f"Extra closing brace at {i}"
    if stack:
        return f"Unclosed brace at {stack[-1]}"
    return "Balanced"

print(check_brackets(c))

