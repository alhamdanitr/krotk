import sys

def view_file(filepath, start_line, end_line):
    print(f"--- {filepath} {start_line}-{end_line} ---")
    with open(filepath, 'r', encoding='utf-8') as f:
        lines = f.readlines()
        for i in range(max(0, start_line-1), min(len(lines), end_line)):
            print(f"{i+1}: {lines[i].rstrip()}")

view_file("kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt", 4300, 4320)
view_file("kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt", 4370, 4390)
view_file("kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt", 4420, 4440)
view_file("kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt", 4480, 4500)
view_file("kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt", 4560, 4580)
view_file("kurotek/src/main/java/com/example/ui/MainDashboardScreen.kt", 4640, 4660)
view_file("kurotek/src/main/java/com/example/ui/MikrotikGeneratorScreen.kt", 15, 20)
view_file("kurotek/src/main/java/com/example/ui/MikrotikGeneratorScreen.kt", 80, 90)
view_file("kurotek/src/main/java/com/example/ui/MikrotikGeneratorScreen.kt", 160, 170)
view_file("kurotek/src/main/java/com/example/ui/theme/Components.kt", 70, 80)

