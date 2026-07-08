# Jaib - Premium Fintech Application UI

A production-quality UI prototype for a Saudi Arabian fintech application built with Next.js 16, React 19, TypeScript, and Tailwind CSS.

## Features

### 🎨 Design System
- **Material Design 3**: Premium fintech aesthetic
- **Dark/Light Themes**: Full theme support with CSS variables
- **RTL Support**: Complete Arabic localization
- **Responsive**: Mobile-first design (375px+)
- **Accessible**: WCAG AA compliant

### 📱 Screens Included
1. **Home Dashboard**: Greeting, account carousel, services grid, transactions
2. **Authentication**: Login with tabs for different user types
3. **Services Catalog**: Complete grid of 26+ fintech services
4. **Profile/Settings**: User info, expandable settings menu
5. **Reports**: Transaction search and filtering interface

### 🧩 Reusable Components
- `BottomNavigation` - 5-tab navigation with FAB
- `ServiceCard` - Flexible service item component
- `AccountCard` - Gradient card with account details
- `TransactionItem` - Transaction list item
- `SettingsMenuItem` - Expandable menu items
- `BottomSheet` - Modal drawer from bottom
- `Button` - Variant button component
- `Input` - Form input with validation

### 🎯 Key Features
- Pixel-perfect layouts matching original design
- Smooth animations and micro-interactions
- Bottom sheet modal with swipe support
- Expandable/collapsible menu items
- Account carousel with indicators
- Transaction history with status indicators
- Responsive service grid

## Quick Start

### Prerequisites
- Node.js 18+
- pnpm (or npm/yarn)

### Installation
```bash
# Clone or download the project
cd jaib-fintech-ui

# Install dependencies
pnpm install

# Start development server
pnpm dev
```

The app will be available at `http://localhost:3000`

## Project Structure

```
jaib-fintech-ui/
├── app/
│   ├── page.tsx              # Home screen
│   ├── layout.tsx            # Root layout with Cairo font
│   ├── globals.css           # Design tokens and theme
│   ├── login/
│   │   └── page.tsx          # Login screen
│   ├── profile/
│   │   └── page.tsx          # Profile/Settings screen
│   ├── services/
│   │   └── page.tsx          # Services catalog
│   └── reports/
│       └── page.tsx          # Reports/Search screen
├── components/
│   ├── navigation/
│   │   └── BottomNavigation.tsx
│   ├── cards/
│   │   ├── ServiceCard.tsx
│   │   ├── AccountCard.tsx
│   │   ├── TransactionItem.tsx
│   │   └── SettingsMenuItem.tsx
│   ├── modals/
│   │   └── BottomSheet.tsx
│   └── ui/
│       ├── Button.tsx
│       └── Input.tsx
├── lib/
│   └── utils.ts              # Utility functions
├── public/                   # Static assets
├── DESIGN_SYSTEM.md         # Complete design documentation
└── README.md                # This file
```

## Design System

### Color Palette
```
Primary: #E53935 (Red)
Success: #4CAF50 (Green)
Error: #F44336 (Red)
Warning: #FF9800 (Orange)
Info: #2196F3 (Blue)
```

### Spacing (8px grid)
- xs: 4px
- sm: 8px
- md: 16px
- lg: 24px
- xl: 32px

### Typography
- Font: Cairo (Arabic-optimized)
- Weights: 400, 500, 600, 700, 800

### Border Radius
- sm: 6px
- md: 8px
- lg: 12px
- xl: 16px
- 2xl: 20px
- 3xl: 24px

See [DESIGN_SYSTEM.md](./DESIGN_SYSTEM.md) for complete documentation.

## Screens Overview

### 1. Home Screen (`/`)
- Greeting header with user name
- Account card carousel with balance and account details
- Promotional section with promo cards
- 3x3 service grid with 9 primary services
- Transaction history list
- Bottom navigation with FAB
- Bottom sheet menu with quick actions

### 2. Login Screen (`/login`)
- Kayan branding with animated arrow
- Tab switching between user types (Location/POS)
- Phone number input
- Password input with eye toggle
- Biometric authentication option
- Primary CTA button
- Sign up link
- Support options (3 button grid)

### 3. Profile Screen (`/profile`)
- User avatar with initials
- Account information (name, account number)
- Account details card with QR code
- Expandable settings menu items:
  - Update App Data
  - Device Management
  - Client File
  - Favorites & Preferences
  - Privacy & Security
  - Additional Settings
  - Support & Help
  - App Personalization
  - Share App
  - Delete Wallet
  - Logout
- App version display

### 4. Services Screen (`/services`)
- Header with title
- Full 3-column grid of 26 services
- All services organized in card format
- No bottom FAB (full navigation only)

### 5. Reports Screen (`/reports`)
- Header with "Other" title
- Search input field
- 4 main report options as cards:
  - Latest Transactions
  - Account Statement
  - Custom Search
  - Reference Number Search

## Styling & Theme

### CSS Variables
All colors and sizes are defined as CSS variables in `app/globals.css`:

```css
--primary: #E53935
--background: #0F172A (dark)
--foreground: #F1F5F9 (dark)
--card: #1E293B (dark)
```

### Light Theme
Add `light` class to `<html>` element or modify `:root` CSS variables.

### Dark Theme (Default)
Automatically applied with `dark` class on `<html>` element.

## RTL Support

The layout uses logical CSS properties for automatic RTL support:
- `start` / `end` instead of `left` / `right`
- `inline-start` / `inline-end` for inline padding/margin
- Flex direction automatically reverses in RTL mode

HTML is set to `dir="rtl"` and `lang="ar"` for proper Arabic handling.

## Responsive Design

- **Mobile**: 375px (primary viewport)
- **Tablet**: 768px+
- **Desktop**: 1024px+

All components are mobile-first and scale up responsively.

## Animations

### Micro-interactions
- Button press: `scale(0.95)` feedback
- Hover states: Smooth transitions (150-200ms)
- Modal entrance: Fade backdrop + slide up sheet
- Navigation: Smooth tab switching

### Accessibility
All animations respect `prefers-reduced-motion` through CSS.

## Performance

- Server-side rendering with Next.js
- CSS-in-JS with Tailwind for minimal bundle
- Optimized images with next/image
- No unnecessary re-renders with proper React patterns

## Localization

### Arabic Support
- Full RTL layout
- Cairo font for perfect Arabic rendering
- All UI text in Arabic
- Proper text direction for mixed content

### Key Terms
- الرئيسية (Home)
- الملف (Profile)
- التقارير (Reports)
- الخدمات (Services)

## Accessibility

- WCAG AA compliant color contrast
- Semantic HTML structure
- ARIA labels on interactive elements
- Keyboard navigation support
- Focus indicators
- Screen reader friendly

## Dependencies

- **next**: 16.2.6 - React framework
- **react**: 19.2.4 - UI library
- **tailwindcss**: 4.0+ - Styling
- **lucide-react**: 1.17.0 - Icons
- **typescript**: Latest - Type safety

## Development

### Add a New Component

1. Create file in `components/[category]/NewComponent.tsx`
2. Export named component with TypeScript interface
3. Use Tailwind classes following design system
4. Import and use in screens

### Add a New Screen

1. Create route folder: `app/new-screen/`
2. Create `page.tsx` with client component
3. Import navigation and other components
4. Use design tokens from globals.css

### Customize Colors

Edit `app/globals.css` theme variables:
```css
--primary: #E53935  /* Change primary color */
--background: #0F172A  /* Change background */
```

## Building for Flutter

This design is structured to be easily translated to Flutter:

1. **Components**: Each TypeScript component maps to a Flutter Widget
2. **Styling**: CSS variables → Flutter theme constants
3. **Layout**: Flexbox → Flex widgets
4. **Navigation**: React routing → Flutter navigation
5. **State**: React hooks → Flutter BLoC/Provider

All component props are typed and documented for clean Flutter bridge implementation.

## Browser Support

- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

## Deployment

### Vercel (Recommended)
```bash
# Connect GitHub repo and push
# Vercel will auto-deploy
```

### Docker
```bash
docker build -t jaib-fintech .
docker run -p 3000:3000 jaib-fintech
```

### Self-hosted
```bash
pnpm build
pnpm start
```

## Future Enhancements

- [ ] Animation library integration
- [ ] Loading skeleton states
- [ ] Toast notifications
- [ ] Date picker component
- [ ] More transaction filters
- [ ] Enhanced animations
- [ ] Gesture support (swipe)
- [ ] Offline mode
- [ ] Payment integration
- [ ] Biometric authentication UI

## License

This is a design prototype created for demonstration purposes.

## Support

For questions or issues:
1. Check [DESIGN_SYSTEM.md](./DESIGN_SYSTEM.md) for design details
2. Review component props and usage
3. Check Tailwind documentation for styling
4. Refer to Next.js docs for framework features

## Credits

- Design inspired by Material Design 3
- Built with Next.js, React, and Tailwind CSS
- Icons from Lucide React
- Fonts: Cairo (Arabic) and Geist (Latin)

---

**Status**: ✅ Production-Ready UI Prototype

**Last Updated**: June 2026

**Version**: 1.0.0
