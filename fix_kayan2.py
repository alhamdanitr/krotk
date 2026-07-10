import re

with open('kayan_repo/mobile/app/src/main/java/com/example/ui/MainDashboardScreen.kt', 'r') as f:
    content = f.read()

def replace_card_mess(match):
    mod = match.group(1)
    return f'Card(border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = {mod})'

content = re.sub(
    r'Card\(\s*-\s*MaterialTheme\.colorScheme\.outline\),\s*-\s*0\.dp else 2\.dp\), modifier = (.*?)\)',
    replace_card_mess,
    content,
    flags=re.DOTALL
)

with open('kayan_repo/mobile/app/src/main/java/com/example/ui/MainDashboardScreen.kt', 'w') as f:
    f.write(content)
