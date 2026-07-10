'use client';

import React from 'react';
import { cn } from '@/lib/utils';

// ─── Spinner ──────────────────────────────────────────────────────────────────

interface SpinnerProps {
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}

const sizeClasses = {
  sm: 'w-4 h-4 border-2',
  md: 'w-7 h-7 border-2',
  lg: 'w-10 h-10 border-[3px]',
};

export function Spinner({ size = 'md', className }: SpinnerProps) {
  return (
    <div
      role="status"
      aria-label="جارٍ التحميل"
      className={cn(
        'rounded-full border-primary/30 border-t-primary animate-spin',
        sizeClasses[size],
        className,
      )}
    />
  );
}

// ─── Full-page Loading ────────────────────────────────────────────────────────

export function PageLoader() {
  return (
    <div className="flex h-[60vh] w-full flex-col items-center justify-center gap-4">
      <Spinner size="lg" />
      <p className="text-sm text-muted-foreground animate-pulse">جارٍ التحميل…</p>
    </div>
  );
}

// ─── Skeleton ─────────────────────────────────────────────────────────────────

interface SkeletonProps {
  className?: string;
}

export function Skeleton({ className }: SkeletonProps) {
  return (
    <div
      className={cn(
        'rounded-md bg-muted shimmer',
        className,
      )}
    />
  );
}

// ─── Skeleton Card (for stat cards) ──────────────────────────────────────────

export function StatCardSkeleton() {
  return (
    <div className="rounded-xl border border-border bg-card p-5 space-y-3">
      <div className="flex items-center justify-between">
        <Skeleton className="h-4 w-28" />
        <Skeleton className="h-9 w-9 rounded-lg" />
      </div>
      <Skeleton className="h-8 w-36" />
      <Skeleton className="h-3 w-24" />
    </div>
  );
}

// ─── Skeleton Row (for tables / activity lists) ───────────────────────────────

export function RowSkeleton({ rows = 5 }: { rows?: number }) {
  return (
    <div className="space-y-3">
      {Array.from({ length: rows }).map((_, i) => (
        <div key={i} className="flex items-center gap-3 p-3 rounded-lg border border-border bg-card">
          <Skeleton className="h-9 w-9 rounded-full flex-shrink-0" />
          <div className="flex-1 space-y-2">
            <Skeleton className="h-3 w-3/4" />
            <Skeleton className="h-3 w-1/2" />
          </div>
          <Skeleton className="h-5 w-16" />
        </div>
      ))}
    </div>
  );
}
