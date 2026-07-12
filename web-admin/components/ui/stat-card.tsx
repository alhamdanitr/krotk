'use client';

import React from 'react';
import { TrendingDown, TrendingUp } from 'lucide-react';
import { cn } from '@/lib/utils';

// ─── Types ────────────────────────────────────────────────────────────────────

type StatTrend = 'up' | 'down' | 'neutral';
type StatVariant = 'default' | 'success' | 'warning' | 'danger' | 'info';

interface StatCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  icon: React.ReactNode;
  trend?: StatTrend;
  trendLabel?: string;
  variant?: StatVariant;
  className?: string;
  onClick?: () => void;
}

// ─── Variant Configs ──────────────────────────────────────────────────────────

const VARIANT_STYLES: Record<
  StatVariant,
  { icon: string; glow: string; border: string }
> = {
  default: {
    icon: 'bg-primary/10 text-primary',
    glow: '',
    border: 'border-border',
  },
  success: {
    icon: 'bg-green-100 text-green-600 dark:bg-green-900/30 dark:text-green-400',
    glow: 'shadow-green-500/10',
    border: 'border-green-200/50 dark:border-green-900/30',
  },
  warning: {
    icon: 'bg-amber-100 text-amber-600 dark:bg-amber-900/30 dark:text-amber-400',
    glow: 'shadow-amber-500/10',
    border: 'border-amber-200/50 dark:border-amber-900/30',
  },
  danger: {
    icon: 'bg-red-100 text-red-500 dark:bg-red-900/30 dark:text-red-400',
    glow: 'shadow-red-500/10',
    border: 'border-red-200/50 dark:border-red-900/30',
  },
  info: {
    icon: 'bg-blue-100 text-blue-500 dark:bg-blue-900/30 dark:text-blue-400',
    glow: 'shadow-blue-500/10',
    border: 'border-blue-200/50 dark:border-blue-900/30',
  },
};

// ─── Component ────────────────────────────────────────────────────────────────

export function StatCard({
  title,
  value,
  subtitle,
  icon,
  trend,
  trendLabel,
  variant = 'default',
  className,
  onClick,
}: StatCardProps) {
  const styles = VARIANT_STYLES[variant];

  return (
    <div
      role={onClick ? 'button' : undefined}
      tabIndex={onClick ? 0 : undefined}
      onClick={onClick}
      onKeyDown={(e) => e.key === 'Enter' && onClick?.()}
      className={cn(
        'group relative overflow-hidden rounded-xl border bg-card p-5 shadow-sm transition-all duration-200',
        styles.border,
        styles.glow && `shadow-md ${styles.glow}`,
        onClick &&
          'cursor-pointer hover:shadow-md hover:-translate-y-0.5 active:translate-y-0',
        className,
      )}
    >
      {/* Subtle gradient background */}
      <div className="pointer-events-none absolute inset-0 bg-gradient-to-br from-transparent via-transparent to-primary/[0.02]" />

      <div className="relative flex items-start justify-between">
        {/* Text section */}
        <div className="space-y-1 flex-1 min-w-0">
          <p className="text-sm font-medium text-muted-foreground truncate">{title}</p>
          <p className="text-2xl font-bold tracking-tight text-foreground">
            {typeof value === 'number' ? value.toLocaleString('ar-YE') : value}
          </p>
          {(trend || subtitle) && (
            <div className="flex items-center gap-1.5">
              {trend && trend !== 'neutral' && (
                <span
                  className={cn(
                    'flex items-center gap-0.5 text-xs font-semibold',
                    trend === 'up' ? 'text-green-600 dark:text-green-400' : 'text-red-500',
                  )}
                >
                  {trend === 'up' ? (
                    <TrendingUp className="h-3 w-3" />
                  ) : (
                    <TrendingDown className="h-3 w-3" />
                  )}
                  {trendLabel}
                </span>
              )}
              {subtitle && (
                <span className="text-xs text-muted-foreground">{subtitle}</span>
              )}
            </div>
          )}
        </div>

        {/* Icon */}
        <div
          className={cn(
            'flex h-11 w-11 flex-shrink-0 items-center justify-center rounded-xl',
            styles.icon,
          )}
        >
          {icon}
        </div>
      </div>
    </div>
  );
}
