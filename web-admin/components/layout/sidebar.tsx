'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import {
  LayoutDashboard,
  CreditCard,
  MessageSquare,
  Clock,
  BarChart3,
  Settings,
  Users,
  Wallet,
  ChevronLeft,
  ChevronRight,
  LogOut,
  Zap,
} from 'lucide-react';
import { cn } from '@/lib/utils';
import { useAuth } from '@/hooks/use-auth';

// ─── Navigation Items ─────────────────────────────────────────────────────────

interface NavItem {
  id: string;
  label: string;
  href: string;
  icon: React.ReactNode;
  badge?: number;
  roles?: string[];
}

const NAV_ITEMS: NavItem[] = [
  {
    id: 'dashboard',
    label: 'لوحة التحكم',
    href: '/dashboard',
    icon: <LayoutDashboard className="h-5 w-5" />,
  },
  {
    id: 'cards',
    label: 'الكروت',
    href: '/dashboard/cards',
    icon: <CreditCard className="h-5 w-5" />,
  },
  {
    id: 'sms',
    label: 'محرك SMS',
    href: '/dashboard/sms',
    icon: <MessageSquare className="h-5 w-5" />,
  },
  {
    id: 'pending',
    label: 'المعلقات',
    href: '/dashboard/pending',
    icon: <Clock className="h-5 w-5" />,
  },
  {
    id: 'distributor',
    label: 'الموزع',
    href: '/dashboard/distributor',
    icon: <Users className="h-5 w-5" />,
  },
  {
    id: 'wallets',
    label: 'المحافظ',
    href: '/dashboard/wallets',
    icon: <Wallet className="h-5 w-5" />,
  },
  {
    id: 'reports',
    label: 'التقارير',
    href: '/dashboard/reports',
    icon: <BarChart3 className="h-5 w-5" />,
  },
  {
    id: 'settings',
    label: 'الإعدادات',
    href: '/dashboard/settings',
    icon: <Settings className="h-5 w-5" />,
  },
];

// ─── Sidebar Component ────────────────────────────────────────────────────────

interface SidebarProps {
  pendingCount?: number;
  isOpen?: boolean;
  onClose?: () => void;
}

export function Sidebar({ pendingCount = 0, isOpen = true, onClose }: SidebarProps) {
  const [collapsed, setCollapsed] = useState(false);
  const pathname = usePathname();
  const { user, tenant, logout } = useAuth();

  const items = NAV_ITEMS.map((item) =>
    item.id === 'pending' ? { ...item, badge: pendingCount } : item,
  );

  return (
    <>
      {/* Mobile overlay */}
      {isOpen && (
        <div
          className="fixed inset-0 z-40 bg-black/60 backdrop-blur-sm lg:hidden"
          onClick={onClose}
        />
      )}

      {/* Sidebar panel */}
      <aside
        id="sidebar"
        className={cn(
          'fixed right-0 top-0 z-50 flex h-screen flex-col bg-sidebar border-l border-sidebar-border sidebar-transition lg:relative lg:z-auto',
          collapsed ? 'w-[70px]' : 'w-[250px]',
          // Mobile: slide in/out
          'lg:translate-x-0',
          isOpen ? 'translate-x-0' : 'translate-x-full lg:translate-x-0',
        )}
      >
        {/* ── Logo ─────────────────────────────────────────────────────── */}
        <div className="flex h-16 items-center justify-between border-b border-sidebar-border px-4">
          <div
            className={cn(
              'flex items-center gap-2.5 overflow-hidden transition-all duration-200',
              collapsed && 'w-0 opacity-0',
            )}
          >
            <div className="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-lg bg-primary shadow-lg shadow-primary/30">
              <Zap className="h-4 w-4 text-white" />
            </div>
            <div className="min-w-0">
              <p className="text-sm font-bold text-sidebar-foreground leading-tight truncate">
                {tenant?.networkName ?? 'Kurotek'}
              </p>
              <p className="text-[10px] text-muted-foreground">لوحة التحكم</p>
            </div>
          </div>

          {/* Collapse toggle — desktop only */}
          <button
            onClick={() => setCollapsed((c) => !c)}
            className="hidden lg:flex h-7 w-7 items-center justify-center rounded-md text-muted-foreground hover:bg-sidebar-accent hover:text-sidebar-foreground transition-colors flex-shrink-0"
            aria-label={collapsed ? 'توسيع القائمة' : 'طي القائمة'}
          >
            {collapsed ? (
              <ChevronLeft className="h-4 w-4" />
            ) : (
              <ChevronRight className="h-4 w-4" />
            )}
          </button>
        </div>

        {/* ── Navigation ───────────────────────────────────────────────── */}
        <nav className="flex-1 overflow-y-auto px-3 py-4 space-y-1 scrollbar-hide">
          {items.map((item) => {
            const isActive =
              item.href === '/dashboard'
                ? pathname === '/dashboard'
                : pathname.startsWith(item.href);

            return (
              <Link
                key={item.id}
                href={item.href}
                onClick={onClose}
                className={cn(
                  'group flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-all duration-150',
                  isActive
                    ? 'bg-primary/10 text-primary'
                    : 'text-sidebar-foreground/70 hover:bg-sidebar-accent hover:text-sidebar-foreground',
                  collapsed && 'justify-center px-2',
                )}
              >
                {/* Icon */}
                <span
                  className={cn(
                    'flex-shrink-0 transition-transform duration-150',
                    isActive && 'text-primary',
                    !isActive && 'group-hover:scale-110',
                  )}
                >
                  {item.icon}
                </span>

                {/* Label */}
                {!collapsed && (
                  <span className="flex-1 truncate">{item.label}</span>
                )}

                {/* Badge */}
                {!collapsed && item.badge && item.badge > 0 ? (
                  <span className="flex h-5 min-w-[20px] items-center justify-center rounded-full bg-primary px-1.5 text-[10px] font-bold text-white">
                    {item.badge > 99 ? '99+' : item.badge}
                  </span>
                ) : null}

                {/* Collapsed badge dot */}
                {collapsed && item.badge && item.badge > 0 ? (
                  <span className="absolute left-2 top-2 h-2 w-2 rounded-full bg-primary" />
                ) : null}
              </Link>
            );
          })}
        </nav>

        {/* ── User & Logout ─────────────────────────────────────────────── */}
        <div className="border-t border-sidebar-border p-3 space-y-1">
          {/* User info */}
          {!collapsed && (
            <div className="flex items-center gap-3 px-3 py-2 rounded-lg">
              <div className="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-full bg-primary/10 text-primary text-xs font-bold">
                {user?.username?.[0]?.toUpperCase() ?? 'U'}
              </div>
              <div className="min-w-0 flex-1">
                <p className="text-xs font-semibold text-sidebar-foreground truncate">
                  {user?.username ?? 'المستخدم'}
                </p>
                <p className="text-[10px] text-muted-foreground capitalize">{user?.role ?? ''}</p>
              </div>
            </div>
          )}

          {/* Logout */}
          <button
            onClick={logout}
            className={cn(
              'flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium text-red-500 hover:bg-red-50 dark:hover:bg-red-950/30 transition-colors',
              collapsed && 'justify-center px-2',
            )}
          >
            <LogOut className="h-5 w-5 flex-shrink-0" />
            {!collapsed && <span>تسجيل الخروج</span>}
          </button>
        </div>
      </aside>
    </>
  );
}
