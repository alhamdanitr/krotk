import os
import re

def process_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    original = content

    # 1. Remove hardcoded shapes
    content = re.sub(r'shape\s*=\s*RoundedCornerShape\([^)]*\),?', '', content)
    content = re.sub(r'shape\s*=\s*CircleShape,?', '', content)
    
    # 2. Remove hardcoded colors from components
    content = re.sub(r'colors\s*=\s*CardDefaults\.cardColors\([^)]*\),?', '', content)
    content = re.sub(r'colors\s*=\s*ButtonDefaults\.buttonColors\([^)]*\),?', '', content)
    content = re.sub(r'colors\s*=\s*OutlinedTextFieldDefaults\.colors\([^)]*\),?', '', content)
    content = re.sub(r'colors\s*=\s*TopAppBarDefaults\.topAppBarColors\([^)]*\),?', '', content)
    
    # 3. Remove hardcoded borders and elevations from components
    content = re.sub(r'border\s*=\s*BorderStroke\([^)]*\),?', '', content)
    content = re.sub(r'elevation\s*=\s*CardDefaults\.cardElevation\([^)]*\),?', '', content)
    content = re.sub(r'elevation\s*=\s*ButtonDefaults\.buttonElevation\([^)]*\),?', '', content)
    
    # 4. Replace custom colors with semantic MaterialTheme colors
    # Text colors
    content = content.replace('color = Color.White', 'color = MaterialTheme.colorScheme.onSurface')
    content = content.replace('color = Color.Black', 'color = MaterialTheme.colorScheme.onSurface')
    content = content.replace('color = TextSecondary', 'color = MaterialTheme.colorScheme.onSurfaceVariant')
    content = content.replace('color = PureWhite', 'color = MaterialTheme.colorScheme.onSurface')
    content = content.replace('tint = Color.White', 'tint = MaterialTheme.colorScheme.onSurface')
    content = content.replace('tint = Color.Black', 'tint = MaterialTheme.colorScheme.onSurface')
    content = content.replace('tint = PureWhite', 'tint = MaterialTheme.colorScheme.onSurface')
    content = content.replace('color = BrandPrimaryRed', 'color = MaterialTheme.colorScheme.primary')
    content = content.replace('tint = BrandPrimaryRed', 'tint = MaterialTheme.colorScheme.primary')
    content = content.replace('color = StatusRed', 'color = MaterialTheme.colorScheme.error')
    content = content.replace('tint = StatusRed', 'tint = MaterialTheme.colorScheme.error')

    # Backgrounds
    content = content.replace('background(DeepBlack)', 'background(MaterialTheme.colorScheme.background)')
    content = content.replace('background(SurfaceDark)', 'background(MaterialTheme.colorScheme.surface)')
    content = content.replace('containerColor = DeepBlack', 'containerColor = MaterialTheme.colorScheme.background')
    content = content.replace('containerColor = SurfaceDark', 'containerColor = MaterialTheme.colorScheme.surface')
    
    content = content.replace('Color(0xFF0F172A)', 'MaterialTheme.colorScheme.surfaceVariant')
    content = content.replace('Color(0xFF1E293B)', 'MaterialTheme.colorScheme.secondaryContainer')
    content = content.replace('Color(0xFF1E1E2F)', 'MaterialTheme.colorScheme.surface')
    content = content.replace('Color(0xFF13151E)', 'MaterialTheme.colorScheme.surface')
    content = content.replace('Color(0xFF262936)', 'MaterialTheme.colorScheme.outline')

    # General replacements
    content = content.replace('BrandPrimaryRed', 'MaterialTheme.colorScheme.primary')
    content = content.replace('StatusRed', 'MaterialTheme.colorScheme.error')
    content = content.replace('DeepBlack', 'MaterialTheme.colorScheme.background')
    content = content.replace('SurfaceDark', 'MaterialTheme.colorScheme.surface')
    content = content.replace('PureWhite', 'MaterialTheme.colorScheme.onSurface')
    content = content.replace('TextSecondary', 'MaterialTheme.colorScheme.onSurfaceVariant')
    
    # 5. Fix double commas or trailing commas caused by regex
    content = re.sub(r',\s*,', ',', content)
    content = re.sub(r',\s*\)', ')', content)
    
    if content != original:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Updated {filepath}")

def main():
    for root, _, files in os.walk('kurotek/src/main/java/com/example/ui'):
        for file in files:
            if file.endswith('.kt') and "theme" not in root:
                process_file(os.path.join(root, file))
    
    for root, _, files in os.walk('kayan_repo/mobile/app/src/main/java/com/example/ui'):
        for file in files:
            if file.endswith('.kt') and "theme" not in root:
                process_file(os.path.join(root, file))

if __name__ == '__main__':
    main()
