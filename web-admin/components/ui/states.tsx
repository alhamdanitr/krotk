'use client';

import React from 'react';
import { AlertTriangle, Inbox, RefreshCcw, WifiOff } from 'lucide-react';
import { cn } from '@/lib/utils';
import { parseApiError } from '@/lib/utils';

// ─── Empty State ──────────────────────────────────────────────────────────────

interface EmptyStateProps {
  icon?: React.ReactNode;
  title: string;
  description?: string;
  action?: React.ReactNode;
  className?: string;
}

export function EmptyState({ icon, title, description, action, className }: EmptyStateProps) {
  return (
    <div
      className={cn(
        'flex flex-col items-center justify-center gap-4 py-16 text-center',
        className,
      )}
    >
      <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-muted text-muted-foreground">
        {icon ?? <Inbox className="h-8 w-8" />}
      </div>
      <div className="space-y-1">
        <p className="font-semibold text-foreground">{title}</p>
        {description && (
          <p className="text-sm text-muted-foreground max-w-sm">{description}</p>
        )}
      </div>
      {action && <div>{action}</div>}
    </div>
  );
}

// ─── Error State ──────────────────────────────────────────────────────────────

interface ErrorStateProps {
  error: unknown;
  onRetry?: () => void;
  className?: string;
}

export function ErrorState({ error, onRetry, className }: ErrorStateProps) {
  const message = parseApiError(error);

  return (
    <div
      className={cn(
        'flex flex-col items-center justify-center gap-4 py-16 text-center',
        className,
      )}
    >
      <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-red-50 dark:bg-red-950/30 text-red-500">
        <AlertTriangle className="h-8 w-8" />
      </div>
      <div className="space-y-1">
        <p className="font-semibold text-foreground">حدث خطأ</p>
        <p className="text-sm text-muted-foreground max-w-sm">{message}</p>
      </div>
      {onRetry && (
        <button
          onClick={onRetry}
          className="flex items-center gap-2 rounded-lg bg-primary px-4 py-2 text-sm font-medium text-primary-foreground hover:bg-primary/90 transition-colors"
        >
          <RefreshCcw className="h-4 w-4" />
          إعادة المحاولة
        </button>
      )}
    </div>
  );
}

// ─── Network Error State ──────────────────────────────────────────────────────

export function NetworkErrorState({ onRetry }: { onRetry?: () => void }) {
  return (
    <div className="flex flex-col items-center justify-center gap-4 py-16 text-center">
      <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-orange-50 dark:bg-orange-950/30 text-orange-500">
        <WifiOff className="h-8 w-8" />
      </div>
      <div className="space-y-1">
        <p className="font-semibold text-foreground">لا يوجد اتصال</p>
        <p className="text-sm text-muted-foreground">
          تعذّر الاتصال بالخادم. تأكد من تشغيل الباكند على{' '}
          <code className="rounded bg-muted px-1 py-0.5 font-mono text-xs">localhost:3000</code>
        </p>
      </div>
      {onRetry && (
        <button
          onClick={onRetry}
          className="flex items-center gap-2 rounded-lg border border-border bg-card px-4 py-2 text-sm font-medium hover:bg-muted transition-colors"
        >
          <RefreshCcw className="h-4 w-4" />
          إعادة المحاولة
        </button>
      )}
    </div>
  );
}
