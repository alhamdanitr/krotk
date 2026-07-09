import re
path = "kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt"
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# "Unclosed brace at 2261" which is `fun MainDashboardScreen( ... ) {`
# This means the very top level function is NOT properly closed.
# There are 5 missing closing braces at the end of the file? No, it could be that at the very end of the file, we missed `}` for `MainDashboardScreen`.
# Let's count how many extra `}` we need to balance the whole file.

stack = []
for i, char in enumerate(c):
    if char == '{':
        stack.append(i)
    elif char == '}':
        if stack:
            stack.pop()

print(f"Number of unclosed braces: {len(stack)}")
print(f"They open at: {[c[:idx].count(chr(10))+1 for idx in stack]}")

