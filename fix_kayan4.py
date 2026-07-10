with open('kayan_repo/mobile/app/src/main/java/com/example/ui/MainDashboardScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
'''                Card(
                    
                    MaterialTheme.colorScheme.outline),
                    
                    0.dp else 2.dp), modifier = Modifier.fillMaxWidth().testTag("transaction_item_${transaction.id}")
                )''',
'''                Card(border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = Modifier.fillMaxWidth().testTag("transaction_item_${transaction.id}"))'''
)

content = content.replace(
'''                Card(
                    
                    MaterialTheme.colorScheme.outline),
                    
                    0.dp else 2.dp), modifier = Modifier
                )''',
'''                Card(border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = Modifier)'''
)

content = content.replace(
'''                Card(
                    
                    MaterialTheme.colorScheme.outline),
                    
                    0.dp else 2.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                )''',
'''                Card(border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp))'''
)

content = content.replace(
'''                Card(
                    
                    MaterialTheme.colorScheme.outline),
                    
                    0.dp else 2.dp), modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
                )''',
'''                Card(border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = Modifier.fillMaxWidth().padding(top = 24.dp))'''
)


with open('kayan_repo/mobile/app/src/main/java/com/example/ui/MainDashboardScreen.kt', 'w') as f:
    f.write(content)
