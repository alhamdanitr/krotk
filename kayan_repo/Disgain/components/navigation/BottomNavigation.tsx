'use client';

import React from 'react';
import {
  User,
  FileText,
  Home,
  ShoppingBag,
  House,
  Plus,
} from 'lucide-react';

export interface BottomNavItem {
  id: string;
  label: string;
  icon: React.ReactNode;
  badge?: number;
  active?: boolean;
}

interface BottomNavigationProps {
  items: BottomNavItem[];
  onItemClick?: (id: string) => void;
  showFab?: boolean;
  fabLabel?: string;
  onFabClick?: () => void;
}

export function BottomNavigation({
  items,
  onItemClick,
  showFab = true,
  fabLabel = 'إنشاء',
  onFabClick,
}: BottomNavigationProps) {
  return (
    <>
      <nav className="fixed bottom-0 left-0 right-0 bg-card border-t border-border shadow-lg z-40">
        <div className="flex items-center justify-between h-20 px-0">
          {items.map((item, index) => (
            <button
              key={item.id}
              onClick={() => onItemClick?.(item.id)}
              className={`flex-1 flex flex-col items-center justify-center py-3 relative transition-colors ${
                item.active
                  ? 'text-primary'
                  : 'text-muted-foreground hover:text-foreground'
              }`}
            >
              <div className="text-2xl mb-1">{item.icon}</div>
              <span className="text-xs text-center font-medium leading-tight">
                {item.label}
              </span>
              {item.badge && item.badge > 0 && (
                <span className="absolute -top-1 -right-1 bg-primary text-primary-foreground text-xs font-bold px-1.5 py-0.5 rounded-full">
                  {item.badge}
                </span>
              )}
            </button>
          ))}
        </div>
      </nav>

      {showFab && (
        <button
          onClick={onFabClick}
          className="fixed bottom-20 left-1/2 -translate-x-1/2 w-16 h-16 rounded-full bg-primary text-primary-foreground flex items-center justify-center shadow-xl hover:shadow-2xl transition-all z-50 hover:scale-110"
          aria-label={fabLabel}
        >
          <Plus size={28} />
        </button>
      )}
    </>
  );
}
