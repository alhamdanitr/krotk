path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# Make sure we close SpecialCustomersTab properly
# It ends with:
# } // end if
# } // end LazyColumn
# } // end SpecialCustomersTab

idx = c.find('fun exportStockToCsv')
if idx == -1:
    idx = c.find('private fun exportStockToCsv')

if idx != -1:
    # Just aggressively put 4 brackets and if it's too much we will remove later.
    c = c[:idx] + '\n}\n}\n}\n}\n' + c[idx:]
    c = c.replace('}\n}\n}\n}\n}\n}\nfun export', '}\n}\nfun export')
    c = c.replace('}\n}\n}\n}\n}\nfun export', '}\n}\nfun export')
    
with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

