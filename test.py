import re
text = "modifier = Modifier.fillMaxWidth(),\n)"
text2 = re.sub(r',\s*\n\s*\)', '\n)', text)
print(text2)
