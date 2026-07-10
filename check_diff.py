with open('special_kayan.kt', 'r') as f:
    k1 = f.read()
with open('special_kuro.kt', 'r') as f:
    k2 = f.read()

import re

# Remove all whitespace to see if there's any logical difference
k1_clean = re.sub(r'\s+', '', k1)
k2_clean = re.sub(r'\s+', '', k2)

if k1_clean == k2_clean:
    print("special tabs are IDENTICAL logically")
else:
    print("special tabs are DIFFERENT")
    
with open('pending_kayan.kt', 'r') as f:
    p1 = f.read()
with open('pending_kuro.kt', 'r') as f:
    p2 = f.read()
    
p1_clean = re.sub(r'\s+', '', p1)
p2_clean = re.sub(r'\s+', '', p2)

if p1_clean == p2_clean:
    print("pending tabs are IDENTICAL logically")
else:
    print("pending tabs are DIFFERENT")
