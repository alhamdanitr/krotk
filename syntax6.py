import re
path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# 3693:2 Syntax error: Expecting ')'
# The `AlertDialog` has `modifier = Modifier.border(...)`.
# Let's fix it by properly closing AlertDialog before it. Wait, `modifier` is an argument to `AlertDialog`, so it must be inside the `AlertDialog(...)` call.
# The code is:
# AlertDialog(
#     ...
#     dismissButton = { ... },
#     modifier = Modifier...
# )
# That syntax is correct. But maybe there's a missing `)` or something.
c = c.replace('            }, modifier = Modifier.border(BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(20.dp))\n        )', '            }, modifier = Modifier.border(BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(20.dp))\n        )')

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

