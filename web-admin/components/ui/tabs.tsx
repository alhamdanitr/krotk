'use client';

import React from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { cn } from '@/lib/utils';

interface TabItem {
  label: string;
  href: string;
  icon?: React.ReactNode;
}

interface TabsProps {
  items: TabItem[];
  className?: string;
}

export function Tabs({ items, className }: TabsProps) {
  const pathname = usePathname();

  return (
    <div className={cn('border-b border-border overflow-x-auto scrollbar-hide', className)}>
      <nav className="flex items-center gap-6 px-1" aria-label="Tabs">
        {items.map((item) => {
          const isActive = pathname === item.href;
          return (
            <Link
              key={item.href}
              href={item.href}
              className={cn(
                'group inline-flex items-center gap-2 border-b-2 py-4 px-1 text-sm font-medium whitespace-nowrap transition-colors',
                isActive
                  ? 'border-primary text-primary'
                  : 'border-transparent text-muted-foreground hover:border-muted-foreground/30 hover:text-foreground',
              )}
            >
              {item.icon && (
                <span
                  className={cn(
                    'transition-colors',
                    isActive ? 'text-primary' : 'text-muted-foreground group-hover:text-foreground',
                  )}
                >
                  {item.icon}
                </span>
              )}
              {item.label}
            </Link>
          );
        })}
      </nav>
    </div>
  );
}
