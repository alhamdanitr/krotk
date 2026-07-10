import sys

def extract_function(filepath, func_name):
    with open(filepath, 'r') as f:
        lines = f.readlines()
    
    start_idx = -1
    for i, line in enumerate(lines):
        if line.strip().startswith(f"fun {func_name}"):
            start_idx = i
            break
            
    if start_idx == -1:
        return ""
        
    brace_count = 0
    extracted = []
    
    for i in range(start_idx, len(lines)):
        line = lines[i]
        extracted.append(line)
        brace_count += line.count('{')
        brace_count -= line.count('}')
        if brace_count == 0 and len(extracted) > 1:
            break
            
    return "".join(extracted)

with open('special_kuro.kt', 'w') as f:
    f.write(extract_function('kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt', 'SpecialCustomersTab'))

with open('special_kayan.kt', 'w') as f:
    f.write(extract_function('kayan_repo/mobile/app/src/main/java/com/example/ui/MainDashboardScreen.kt', 'SpecialCustomersTab'))

with open('pending_kuro.kt', 'w') as f:
    f.write(extract_function('kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt', 'PendingApprovalsTab'))

with open('pending_kayan.kt', 'w') as f:
    f.write(extract_function('kayan_repo/mobile/app/src/main/java/com/example/ui/MainDashboardScreen.kt', 'PendingApprovalsTab'))
