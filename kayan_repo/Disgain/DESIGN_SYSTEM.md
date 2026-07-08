# Jaib Fintech Design System

## Overview

Jaib is a premium fintech application design system built with Material Design 3 principles. The system supports dark and light themes, full RTL/Arabic localization, and responsive mobile-first design.

## Color Palette

### Primary Colors
- **Primary Red**: `#E53935` - Main brand color for CTAs and highlights
- **Primary Red Dark**: `#C62828` - Darker variant for hover states

### Semantic Colors
- **Success Green**: `#4CAF50` - Positive transactions, confirmations
- **Error Red**: `#F44336` - Destructive actions, errors
- **Warning Orange**: `#FF9800` - Cautions and warnings
- **Info Blue**: `#2196F3` - Information and secondary actions

### Neutral Colors (Light Theme)
- **Background**: `#F9FAFB` - Main page background
- **Surface**: `#F3F4F6` - Card backgrounds
- **Elevated**: `#FFFFFF` - Elevated surfaces

### Neutral Colors (Dark Theme)
- **Background**: `#0F172A` - Main page background
- **Surface**: `#1E293B` - Card backgrounds
- **Elevated**: `#1E293B` - Elevated surfaces

### Text Colors
- **Text Primary**: `#1F2937` (Light) / `#F1F5F9` (Dark)
- **Text Secondary**: `#6B7280` (Light) / `#CBD5E1` (Dark)
- **Text Disabled**: `#9CA3AF`

## Typography

### Font Family
- **Primary**: Cairo (Arabic-optimized, supports Latin fallback)
- **Heading**: Same as body (Cairo) with varying weights
- **Monospace**: Geist Mono (for account numbers, codes)

### Font Weights
- **Regular**: 400 - Body text
- **Medium**: 500 - Labels, secondary text
- **Semibold**: 600 - Subheadings
- **Bold**: 700 - Headings
- **Extra Bold**: 800 - Main titles

### Font Sizes & Line Heights
```
Display: 32px / 40px (h1)
Heading: 24px / 32px (h2)
Subheading: 18px / 24px (h3)
Body: 16px / 24px
Small: 14px / 20px
Tiny: 12px / 16px
```

## Spacing System

All spacing follows an 8px base unit grid:
- **xs**: 4px (0.25rem)
- **sm**: 8px (0.5rem)
- **md**: 16px (1rem)
- **lg**: 24px (1.5rem)
- **xl**: 32px (2rem)
- **2xl**: 48px (3rem)

## Border Radius

- **sm**: 6px - Small buttons, badges
- **md**: 8px - Input fields, small cards
- **lg**: 12px - Standard cards
- **xl**: 16px - Large cards
- **2xl**: 20px - Sheet corners
- **3xl**: 24px - Large dialogs
- **4xl**: 32px - Full-screen elements

## Elevation & Shadows

### Light Theme
```
No elevation: No shadow
Elevation 1: 0 2px 4px rgba(0,0,0,0.08)
Elevation 2: 0 4px 8px rgba(0,0,0,0.12)
Elevation 3: 0 8px 16px rgba(0,0,0,0.16)
```

### Dark Theme
```
Elevated surfaces use darker backgrounds instead of shadows
Border: 1px solid rgba(255,255,255,0.1)
```

## Components

### Buttons
- **Primary**: Red background, white text, full-width CTAs
- **Secondary**: Muted background, primary text
- **Ghost**: No background, primary text
- **Outline**: Bordered variant

### Cards
- **Service Card**: Icon + Title + Subtitle (3 columns grid)
- **Account Card**: Gradient background, account details
- **Transaction Card**: Icon + Title/Date + Amount
- **Settings Card**: Expandable menu items

### Navigation
- **Bottom Navigation**: 5 tabs with FAB in center
- **FAB**: Circular action button, always primary red
- **Bottom Sheet**: Modal drawer from bottom with swipe dismiss

### Forms
- **Input**: Text field with label, error state, optional icon
- **Tabs**: Two-tab selector (Location/Business)

## Responsive Design

### Breakpoints
- **Mobile**: 375px (primary target)
- **Tablet**: 768px
- **Desktop**: 1024px+

### Mobile-First Approach
- Base styles for mobile (375px)
- Enhance for larger screens with `md:` and `lg:` prefixes
- Single column grid for services on mobile

## Accessibility

### ARIA
- All interactive elements have proper role and label
- Bottom navigation items are semantically marked
- Expandable sections use aria-expanded

### Color Contrast
- Text meets WCAG AA standards (4.5:1 minimum)
- Light theme: Dark text on light backgrounds
- Dark theme: Light text on dark backgrounds

### Typography
- Readable font sizes (min 16px on mobile)
- Adequate line height (1.4-1.6)
- Proper text balance for headings

### RTL Support
- All components use logical properties (start/end)
- Flex direction automatically reverses in RTL
- Cairo font handles Arabic properly

## Animations

### Micro-interactions
- Button press: `active:scale-95` - Quick feedback
- Hover states: Smooth background/shadow transitions
- Navigation: Smooth tab switching (200ms)
- Modals: Fade in with bottom sheet slide up

### Duration
- Quick: 150ms - Hover, focus
- Standard: 200ms - Navigate, toggle
- Slow: 300ms - Modal entrance

## Localization

### Arabic Text
- Uses Cairo font which is optimized for Arabic
- Right-to-left (RTL) layout support built-in
- Numbers can be in Arabic or Western format (currently Western)

### Key Arabic Terms
- الرئيسية (Home)
- الملف (Profile)
- التقارير (Reports)
- الخدمات (Services)
- تسجيل الخروج (Logout)

## Implementation Notes

### Dark Theme (Current Default)
- Background: `#0F172A`
- Cards: `#1E293B`
- Primary text: `#F1F5F9`
- Borders: `rgba(255,255,255,0.1)`

### Light Theme (Available)
- Background: `#F9FAFB`
- Cards: `#FFFFFF`
- Primary text: `#1F2937`
- Borders: `#E5E7EB`

### Theme Switching
Apply `dark` class to `<html>` element for dark theme. Light theme uses default `:root` colors.

## Flutter Implementation Considerations

### Component Structure
- Keep components flat and functional
- Use consistent prop names across components
- Provide TypeScript types for Flutter bridge

### Design Tokens
- All colors, spacing, and sizes stored as CSS variables
- Can be easily translated to Flutter theme constants
- Shadow and elevation systems translate to Flutter Shadow widgets

### Responsive Design
- Use flexbox for responsive layouts
- Avoid hardcoded pixel values
- Use relative sizing (percentages, calc())

## Future Enhancements

- Animation library for complex interactions
- Skeleton loading states
- Toast/snackbar notification system
- Dropdown/select components
- Modal dialog variations
- Segmented control component
- Date picker component
- Stepper/wizard component

## File Structure

```
components/
├── navigation/
│   └── BottomNavigation.tsx
├── cards/
│   ├── ServiceCard.tsx
│   ├── AccountCard.tsx
│   ├── TransactionItem.tsx
│   └── SettingsMenuItem.tsx
├── modals/
│   └── BottomSheet.tsx
└── ui/
    ├── Button.tsx
    └── Input.tsx

app/
├── page.tsx (Home)
├── login/page.tsx (Login)
├── profile/page.tsx (Profile)
├── services/page.tsx (Services)
├── reports/page.tsx (Reports)
└── globals.css (Design tokens)
```
