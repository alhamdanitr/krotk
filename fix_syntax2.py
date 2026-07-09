import re
path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# I see `3373:1 Unresolved reference 'private'.` Let's remove any stray 'private'
c = c.replace('\nprivate fun ', '\nfun ')

c = c.replace('escapeCsv(user.username)', 'user.username.escapeCsv()')
c = c.replace('escapeCsv(user.role)', 'user.role.escapeCsv()')
c = c.replace('escapeCsv(user.isActive.toString())', 'user.isActive.toString().escapeCsv()')

# 3252: Button syntax issue
# "3250:2 Syntax error: Expecting ','" -> maybe it's missing a comma before modifier?
# Let's inspect line 3250
c = c.replace('Button(\n                                onClick = { viewModel.approvePending(pending.id, null) }\n                            ) {', 'Button(\n                                onClick = { viewModel.approvePending(pending.id, null) }\n                            ) {')

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

