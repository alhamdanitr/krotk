import re
import os

path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. escapeCsv
if 'fun String.escapeCsv()' not in content:
    content = content.replace('private fun exportStockToCsv', 'private fun String.escapeCsv(): String { return this.replace("\\"", "\\"\\"").let { "\\"$it\\"" } }\n\nprivate fun exportStockToCsv')

# 2. 'private' is not applicable to 'local function'
content = content.replace('private fun exportStockToCsv', 'fun exportStockToCsv')
content = content.replace('private fun exportUsersToCsv', 'fun exportUsersToCsv')
content = content.replace('private fun exportMappingsToCsv', 'fun exportMappingsToCsv')

# 3. exportCustomerTransactionsToCsv
if 'fun exportCustomerTransactionsToCsv' not in content:
    content = content.replace('fun exportStockToCsv', 'fun exportCustomerTransactionsToCsv(context: android.content.Context, customer: Any, transactions: Any) { }\n\nfun exportStockToCsv')

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)
