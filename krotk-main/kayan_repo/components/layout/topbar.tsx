'use client';

import React from 'react';
import { usePathname } from 'next/navigation';
import { Bell, Moon, Sun, Menu, ChevronLeft } from 'lucide-react';
import { useTheme } from 'next-themes';
import Link from 'next/link';
import { cn } from '@/lib/utils';
import { useAuth } from '@/hooks/use-auth';
import type { DashboardAlert } from '@/types';

// ─── Breadcrumb Map ───────────────────────────────────────────────────────────

const BREADCRUMB_MAP: Record<string, string> = {
  '/dashboard': 'الرئيسية',
  '/dashboard/cards': 'الكروت',
  '/dashboard/sms': 'محرك SMS',
  '/dashboard/pending': 'المعلقات',
  '/dashboard/distributor': 'الموزع',
  '/dashboard/wallets': 'المحافظ',
  '/dashboard/reports': 'التقارير',
  '/dashboard/settings': 'الإعدادات',
};

// ─── Theme Toggle ─────────────────────────────────────────────────────────────

function ThemeToggle() {
  const { theme, setTheme } = useTheme();
  const isDark = theme === 'dark';

  return (
    <button
      onClick={() => setTheme(isDark ? 'light' : 'dark')}
      className="flex h-9 w-9 items-center justify-center rounded-lg border border-border bg-card text-muted-foreground hover:bg-muted hover:text-foreground transition-colors"
      aria-label="تبديل المظهر"
    >
      {isDark ? <Sun className="h-4 w-4" /> : <Moon className="h-4 w-4" />}
    </button>
  );
}

// ─── Notifications Bell ───────────────────────────────────────────────────────

interface NotificationsBellProps {
  alerts: DashboardAlert[];
}

function NotificationsBell({ alerts }: NotificationsBellProps) {
  const criticalCount = alerts.filter((a) => a.severity === 'red').length;

  return (
    <button
      className="relative flex h-9 w-9 items-center justify-center rounded-lg border border-border bg-card text-muted-foreground hover:bg-muted hover:text-foreground transition-colors"
      aria-label={`الإشعارات${criticalCount > 0 ? ` (${criticalCount} حرجة)` : ''}`}
    >
      <Bell className="h-4 w-4" />
      {criticalCount > 0 && (
        <span className="absolute -right-1 -top-1 flex h-4 w-4 items-center justify-center rounded-full bg-primary text-[9px] font-bold text-white">
          {criticalCount > 9 ? '9+' : criticalCount}
        </span>
      )}
    </button>
  );
}

// ─── Topbar ───────────────────────────────────────────────────────────────────

interface TopbarProps {
  onMenuClick: () => void;
  alerts?: DashboardAlert[];
}

export function Topbar({ onMenuClick, alerts = [] }: TopbarProps) {
  const pathname = usePathname();
  const { user, tenant } = useAuth();

  // Build breadcrumb segments
  const currentPage = BREADCRUMB_MAP[pathname] ?? 'الصفحة';
  const isRoot = pathname === '/dashboard';

  return (
    <header className="sticky top-0 z-30 flex h-16 items-center justify-between border-b border-border bg-background/80 backdrop-blur-md px-4 gap-4">
      {/* Left: Menu + Breadcrumb */}
      <div className="flex items-center gap-3 min-w-0">
        {/* Hamburger — mobile */}
        <button
          onClick={onMenuClick}
          className="flex h-9 w-9 flex-shrink-0 items-center justify-center rounded-lg border border-border bg-card text-muted-foreground hover:bg-muted hover:text-foreground transition-colors lg:hidden"
          aria-label="القائمة"
        >
          <Menu className="h-4 w-4" />
        </button>

        {/* Breadcrumb */}
        <nav aria-label="مسار التنقل" className="flex items-center gap-1.5 text-sm min-w-0">
          {!isRoot && (
            <>
              <Link
                href="/dashboard"
                className="text-muted-foreground hover:text-foreground transition-colors flex-shrink-0"
              >
                الرئيسية
              </Link>
              <ChevronLeft className="h-3.5 w-3.5 text-muted-foreground flex-shrink-0" />
            </>
          )}
          <span className="font-semibold text-foreground truncate">{currentPage}</span>
        </nav>
      </div>

      {/* Right: Actions */}
      <div className="flex items-center gap-2 flex-shrink-0">
        {/* Subscription badge */}
        {tenant && (
          <div
            className={cn(
              'hidden md:flex items-center gap-1.5 rounded-full border px-3 py-1 text-xs font-medium',
              tenant.status === 'trial'
                ? 'border-amber-300/50 bg-amber-50 text-amber-700 dark:bg-amber-900/20 dark:text-amber-400'
                : tenant.remainingDays <= 7
                ? 'border-red-300/50 bg-red-50 text-red-600 dark:bg-red-900/20 dark:text-red-400'
                : 'border-green-300/50 bg-green-50 text-green-700 dark:bg-green-900/20 dark:text-green-400',
            )}
          >
            <span
              className={cn(
                'inline-block h-1.5 w-1.5 rounded-full',
                tenant.status === 'trial'
                  ? 'bg-amber-500'
                  : tenant.remainingDays <= 7
                  ? 'bg-red-500'
                  : 'bg-green-500',
              )}
            />
            {tenant.status === 'trial'
              ? `تجريبي — ${tenant.remainingDays} أيام`
              : `${tenant.planType} — ${tenant.remainingDays}د`}
          </div>
        )}

        <NotificationsBell alerts={alerts} />
        <ThemeToggle />

        {/* User avatar */}
        <div className="flex h-9 w-9 items-center justify-center rounded-full bg-primary/10 text-primary text-sm font-bold select-none">
          {user?.username?.[0]?.toUpperCase() ?? 'U'}
        </div>
      </div>
    </header>
  );
}
