# Jaib Fintech - Flutter Implementation Guide

## Overview

This document provides guidance for implementing the Jaib fintech UI prototype in Flutter. All components, styles, and layouts have been designed with Flutter implementation in mind.

## Component Mapping

### Navigation Components

#### BottomNavigation → Flutter BottomNavigationBar
```dart
// TypeScript:
<BottomNavigation items={navItems} onItemClick={setActiveNav} />

// Flutter equivalent:
BottomNavigationBar(
  items: [
    BottomNavigationBarItem(icon: Icon(Icons.person), label: 'الملف'),
    BottomNavigationBarItem(icon: Icon(Icons.description), label: 'التقارير'),
    // ...
  ],
  onTap: (index) => setActiveNav(navItems[index].id),
)
```

**Props Translation:**
- `items: BottomNavItem[]` → `items: List<BottomNavigationBarItem>`
- `onItemClick` → `onTap: (int) → void`
- `showFab: boolean` → `FloatingActionButton` as separate widget
- `onFabClick` → `onPressed: () → void`

### Card Components

#### ServiceCard → Flutter Container with Gesture
```dart
// TypeScript:
<ServiceCard icon={<Icon />} title="Service" />

// Flutter equivalent:
GestureDetector(
  onTap: onTap,
  child: Container(
    decoration: BoxDecoration(
      color: theme.colors.card,
      borderRadius: BorderRadius.circular(12),
      border: Border.all(color: theme.colors.border),
    ),
    child: Column(
      children: [
        Icon(icon, size: 32),
        Text(title),
      ],
    ),
  ),
)
```

#### AccountCard → Flutter Stack with Gradient
```dart
// TypeScript:
<AccountCard accountName="Account" balance="1,234.50" />

// Flutter equivalent:
Container(
  decoration: BoxDecoration(
    gradient: LinearGradient(
      colors: [theme.colors.primary, theme.colors.primary.withOpacity(0.9)],
    ),
    borderRadius: BorderRadius.circular(16),
  ),
  child: Padding(
    padding: EdgeInsets.all(24),
    child: Column(children: [...]),
  ),
)
```

#### TransactionItem → Flutter ListTile with Custom Styling
```dart
// TypeScript:
<TransactionItem icon={icon} title="Transaction" amount="2,400" />

// Flutter equivalent:
Container(
  decoration: BoxDecoration(
    color: theme.colors.card,
    borderRadius: BorderRadius.circular(12),
  ),
  child: ListTile(
    leading: CircleAvatar(child: Icon(icon)),
    title: Text(title),
    subtitle: Text(date),
    trailing: Text(amount, style: TextStyle(color: theme.colors.primary)),
  ),
)
```

### Modal Components

#### BottomSheet → Flutter showModalBottomSheet
```dart
// TypeScript:
<BottomSheet isOpen={true} onClose={onClose} title="Actions" />

// Flutter equivalent:
showModalBottomSheet(
  context: context,
  builder: (context) => Container(
    decoration: BoxDecoration(
      color: theme.colors.card,
      borderRadius: BorderRadius.only(
        topLeft: Radius.circular(24),
        topRight: Radius.circular(24),
      ),
    ),
    child: Column(
      children: [
        Text(title, style: theme.typography.heading),
        // ...children
      ],
    ),
  ),
)
```

## Design Tokens → Flutter Theme

### Color System

```dart
// lib/theme/colors.dart

class AppColors {
  // Primary
  static const Color primary = Color(0xFFE53935);
  static const Color primaryDark = Color(0xFFC62828);
  
  // Semantic
  static const Color success = Color(0xFF4CAF50);
  static const Color error = Color(0xFFF44336);
  static const Color warning = Color(0xFFFF9800);
  static const Color info = Color(0xFF2196F3);
  
  // Dark Theme
  static const Color darkBg = Color(0xFF0F172A);
  static const Color darkSurface = Color(0xFF1E293B);
  static const Color darkText = Color(0xFFF1F5F9);
  
  // Light Theme
  static const Color lightBg = Color(0xFFF9FAFB);
  static const Color lightSurface = Color(0xFFFFFFFF);
  static const Color lightText = Color(0xFF1F2937);
}
```

### Typography

```dart
// lib/theme/typography.dart

class AppTypography {
  static const String fontFamily = 'Cairo';
  
  static const TextStyle h1 = TextStyle(
    fontSize: 32,
    fontWeight: FontWeight.w800,
    height: 1.25,
    fontFamily: fontFamily,
  );
  
  static const TextStyle h2 = TextStyle(
    fontSize: 24,
    fontWeight: FontWeight.w700,
    height: 1.33,
    fontFamily: fontFamily,
  );
  
  static const TextStyle body = TextStyle(
    fontSize: 16,
    fontWeight: FontWeight.w400,
    height: 1.5,
    fontFamily: fontFamily,
  );
  
  static const TextStyle small = TextStyle(
    fontSize: 14,
    fontWeight: FontWeight.w500,
    height: 1.43,
    fontFamily: fontFamily,
  );
}
```

### Spacing

```dart
// lib/theme/spacing.dart

class AppSpacing {
  static const double xs = 4.0;
  static const double sm = 8.0;
  static const double md = 16.0;
  static const double lg = 24.0;
  static const double xl = 32.0;
  static const double xxl = 48.0;
}
```

### Border Radius

```dart
// lib/theme/radius.dart

class AppRadius {
  static const double sm = 6.0;
  static const double md = 8.0;
  static const double lg = 12.0;
  static const double xl = 16.0;
  static const double xxl = 20.0;
  static const double xxxl = 24.0;
  static const double xxxxl = 32.0;
}
```

## Screen Structure

### Home Screen Flow

```
MaterialApp(
  home: Scaffold(
    body: CustomScrollView(
      slivers: [
        SliverAppBar(title: "صباح الخير"),
        SliverToBoxAdapter(child: AccountCarousel()),
        SliverToBoxAdapter(child: PromoSection()),
        SliverGrid(delegates: ServiceGrid()),
        SliverList(delegate: TransactionList()),
      ],
    ),
    bottomNavigationBar: BottomNavigation(),
    floatingActionButton: FAB(),
    floatingActionButtonLocation: FloatingActionButtonLocation.centerDocked,
  ),
)
```

### Bottom Navigation Implementation

```dart
// lib/widgets/navigation/bottom_navigation.dart

class BottomNavigationBar extends StatelessWidget {
  final int selectedIndex;
  final Function(int) onItemTapped;
  
  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: AppColors.darkSurface,
        border: Border(
          top: BorderSide(color: AppColors.darkText.withOpacity(0.1)),
        ),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        children: [
          NavItem(icon: Icons.person, label: 'الملف'),
          NavItem(icon: Icons.description, label: 'التقارير'),
          SizedBox(width: 60), // Space for FAB
          NavItem(icon: Icons.shopping_bag, label: 'الخدمات'),
          NavItem(icon: Icons.home, label: 'الرئيسية'),
        ],
      ),
    );
  }
}
```

### Account Carousel

```dart
// lib/widgets/cards/account_carousel.dart

class AccountCarousel extends StatefulWidget {
  @override
  State<AccountCarousel> createState() => _AccountCarouselState();
}

class _AccountCarouselState extends State<AccountCarousel> {
  late PageController _pageController;
  int _currentPage = 0;
  
  @override
  void initState() {
    super.initState();
    _pageController = PageController();
  }
  
  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        SizedBox(
          height: 200,
          child: PageView.builder(
            controller: _pageController,
            onPageChanged: (index) {
              setState(() => _currentPage = index);
            },
            itemBuilder: (context, index) => AccountCard(
              account: accounts[index],
            ),
          ),
        ),
        DotsIndicator(
          dotsCount: accounts.length,
          position: _currentPage,
          onTap: (position) {
            _pageController.jumpToPage(position.toInt());
          },
        ),
      ],
    );
  }
}
```

## Routing Structure

```dart
// lib/main.dart

void main() {
  runApp(const JaibApp());
}

class JaibApp extends StatelessWidget {
  const JaibApp();
  
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Jaib',
      theme: AppTheme.darkTheme,
      home: const HomeScreen(),
      routes: {
        '/login': (context) => const LoginScreen(),
        '/profile': (context) => const ProfileScreen(),
        '/services': (context) => const ServicesScreen(),
        '/reports': (context) => const ReportsScreen(),
      },
    );
  }
}
```

## Project Structure for Flutter

```
lib/
├── main.dart
├── theme/
│   ├── colors.dart
│   ├── typography.dart
│   ├── spacing.dart
│   ├── radius.dart
│   └── app_theme.dart
├── models/
│   ├── account.dart
│   ├── service.dart
│   ├── transaction.dart
│   └── user.dart
├── widgets/
│   ├── navigation/
│   │   └── bottom_navigation.dart
│   ├── cards/
│   │   ├── service_card.dart
│   │   ├── account_card.dart
│   │   ├── transaction_item.dart
│   │   └── settings_menu_item.dart
│   ├── modals/
│   │   └── bottom_sheet.dart
│   └── common/
│       ├── button.dart
│       ├── input_field.dart
│       └── form_field.dart
├── screens/
│   ├── home_screen.dart
│   ├── login_screen.dart
│   ├── profile_screen.dart
│   ├── services_screen.dart
│   └── reports_screen.dart
├── utils/
│   └── constants.dart
└── providers/
    └── app_provider.dart
```

## State Management Recommendation

### Using Riverpod

```dart
// lib/providers/navigation_provider.dart

final navigationProvider = StateNotifierProvider<NavigationNotifier, int>((ref) {
  return NavigationNotifier();
});

class NavigationNotifier extends StateNotifier<int> {
  NavigationNotifier() : super(0);
  
  void selectTab(int index) => state = index;
}
```

### Using GetX

```dart
// lib/controllers/navigation_controller.dart

class NavigationController extends GetxController {
  var selectedIndex = 0.obs;
  
  void selectTab(int index) {
    selectedIndex.value = index;
  }
}
```

## Responsive Design

```dart
// lib/utils/responsive.dart

class Responsive {
  static bool isMobile(BuildContext context) =>
      MediaQuery.of(context).size.width < 600;
  
  static bool isTablet(BuildContext context) =>
      MediaQuery.of(context).size.width >= 600 &&
      MediaQuery.of(context).size.width < 1200;
  
  static bool isDesktop(BuildContext context) =>
      MediaQuery.of(context).size.width >= 1200;
  
  static double screenWidth(BuildContext context) =>
      MediaQuery.of(context).size.width;
}
```

## RTL Support

```dart
// Configure in MaterialApp
MaterialApp(
  builder: (context, child) {
    return Directionality(
      textDirection: TextDirection.rtl,
      child: child!,
    );
  },
)
```

## Performance Optimization

1. **Use const constructors**: Most widgets are already stateless
2. **Lazy loading**: Use PageView for carousel
3. **Caching**: Cache network images
4. **Code splitting**: Split screens into separate files
5. **Dependency injection**: Use GetIt or Riverpod

## Testing Strategy

```dart
// test/widgets/service_card_test.dart

void main() {
  testWidgets('ServiceCard displays correctly', (WidgetTester tester) async {
    await tester.pumpWidget(
      MaterialApp(
        home: ServiceCard(
          icon: Icons.send,
          title: 'تحويلات مالية',
        ),
      ),
    );
    
    expect(find.text('تحويلات مالية'), findsOneWidget);
    expect(find.byIcon(Icons.send), findsOneWidget);
  });
}
```

## Migration Checklist

- [ ] Set up Flutter project structure
- [ ] Create theme/colors/typography constants
- [ ] Implement bottom navigation
- [ ] Create card components
- [ ] Build account carousel
- [ ] Implement service grid
- [ ] Create transaction list
- [ ] Build modal sheets
- [ ] Implement screen routing
- [ ] Add state management
- [ ] Set up RTL/Arabic support
- [ ] Implement responsive design
- [ ] Add animations
- [ ] Write unit tests
- [ ] Write widget tests
- [ ] Add integration tests

## Key Considerations

1. **Arabic Text**: Use `Directionality` and ensure RTL is enabled
2. **Safe Areas**: Use `SafeArea` on screens with notches
3. **Keyboard Handling**: Wrap forms with `SingleChildScrollView`
4. **Image Loading**: Cache images and show placeholders
5. **Error Handling**: Implement proper error screens
6. **Loading States**: Show skeletons or progress indicators
7. **Offline Support**: Cache data locally
8. **Dark/Light Themes**: Respect system preferences

## Resources

- [Flutter Documentation](https://flutter.dev/docs)
- [Material Design 3](https://m3.material.io/)
- [Riverpod](https://riverpod.dev/)
- [GetX](https://pub.dev/packages/get)
- [Cairo Font Package](https://pub.dev/packages/google_fonts)

## Support & Questions

For questions about:
- Component structure: See respective widget files
- Design tokens: Refer to DESIGN_SYSTEM.md
- CSS to Flutter conversion: Check component mapping above
- State management: Implement based on team preference

---

**Last Updated**: June 2026
**Status**: Ready for Flutter Implementation
