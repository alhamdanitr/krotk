'use client';

import React from 'react';
import { cn } from '@/lib/utils';
import type { AlertSeverity } from '@/types';

// ─── Badge ────────────────────────────────────────────────────────────────────

type BadgeVariant = 'default' | 'success' | 'warning' | 'danger' | 'info' | 'outline';

interface BadgeProps {
  variant?: BadgeVariant;
  children: React.ReactNode;
  className?: string;
}

const BADGE_VARIANTS: Record<BadgeVariant, string> = {
  default: 'bg-secondary text-secondary-foreground',
  success: 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400',
  warning: 'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400',
  danger: 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400',
  info: 'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400',
  outline: 'border border-border bg-transparent text-foreground',
};

export function Badge({ variant = 'default', children, className }: BadgeProps) {
  return (
    <span
      className={cn(
        'inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold',
        BADGE_VARIANTS[variant],
        className,
      )}
    >
      {children}
    </span>
  );
}

// ─── Severity Badge (for alerts) ──────────────────────────────────────────────

const SEVERITY_TO_VARIANT: Record<AlertSeverity, BadgeVariant> = {
  red: 'danger',
  orange: 'warning',
  yellow: 'warning',
  blue: 'info',
};

interface SeverityBadgeProps {
  severity: AlertSeverity;
  children: React.ReactNode;
}

export function SeverityBadge({ severity, children }: SeverityBadgeProps) {
  return <Badge variant={SEVERITY_TO_VARIANT[severity]}>{children}</Badge>;
}

// ─── Status Badge ─────────────────────────────────────────────────────────────

interface StatusBadgeProps {
  status: string;
}

export function StatusBadge({ status }: StatusBadgeProps) {
  const map: Record<string, { label: string; variant: BadgeVariant }> = {
    active: { label: 'نشط', variant: 'success' },
    trial: { label: 'تجريبي', variant: 'info' },
    suspended: { label: 'موقوف', variant: 'danger' },
    expired: { label: 'منتهي', variant: 'danger' },
    pending: { label: 'معلق', variant: 'warning' },
    approved: { label: 'مقبول', variant: 'success' },
    rejected: { label: 'مرفوض', variant: 'danger' },
  };

  const config = map[status] ?? { label: status, variant: 'default' as BadgeVariant };
  return <Badge variant={config.variant}>{config.label}</Badge>;
}
