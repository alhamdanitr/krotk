path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()
    
# Let's inspect the `exportCustomerTransactionsToCsv` function call that causes 3128:25 Unresolved reference 'exportCustomerTransactionsToCsv'
for line in c.split('\n'):
    if 'exportCustomerTransactionsToCsv' in line:
        print(line)

print("=============")

# And inspect the button around 3252
lines = c.split('\n')
for i in range(3240, 3260):
    print(f"{i+1}: {lines[i]}")

