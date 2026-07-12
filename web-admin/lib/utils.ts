import { type ClassValue, clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

// ─── Style Utilities ──────────────────────────────────────────────────────────

export function cn(...inputs: ClassValue[]): string {
  return twMerge(clsx(inputs));
}

// ─── Number Formatting ────────────────────────────────────────────────────────

export function formatCurrency(
  amount: number,
  currency = 'YER',
  locale = 'ar-YE',
): string {
  return new Intl.NumberFormat(locale, {
    style: 'currency',
    currency,
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
  }).format(amount);
}

export function formatNumber(value: number, locale = 'ar-YE'): string {
  return new Intl.NumberFormat(locale).format(value);
}

// ─── Date Formatting ──────────────────────────────────────────────────────────

export function formatDate(
  date: string | Date,
  options?: Intl.DateTimeFormatOptions,
): string {
  const d = typeof date === 'string' ? new Date(date) : date;
  return new Intl.DateTimeFormat('ar-YE', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    ...options,
  }).format(d);
}

export function formatDateTime(date: string | Date): string {
  return formatDate(date, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export function formatRelativeTime(date: string | Date): string {
  const d = typeof date === 'string' ? new Date(date) : date;
  const now = new Date();
  const diffMs = now.getTime() - d.getTime();
  const diffSec = Math.floor(diffMs / 1000);
  const diffMin = Math.floor(diffSec / 60);
  const diffHr = Math.floor(diffMin / 60);
  const diffDay = Math.floor(diffHr / 24);

  if (diffSec < 60) return 'منذ لحظات';
  if (diffMin < 60) return `منذ ${diffMin} دقيقة`;
  if (diffHr < 24) return `منذ ${diffHr} ساعة`;
  if (diffDay < 7) return `منذ ${diffDay} أيام`;
  return formatDate(d);
}

// ─── String Utilities ─────────────────────────────────────────────────────────

export function truncate(str: string, maxLength: number): string {
  if (str.length <= maxLength) return str;
  return `${str.slice(0, maxLength)}...`;
}

export function initials(name: string): string {
  return name
    .split(' ')
    .slice(0, 2)
    .map((n) => n[0])
    .join('')
    .toUpperCase();
}

// ─── Validation ───────────────────────────────────────────────────────────────

export function isValidPhone(phone: string): boolean {
  return /^[7][0-9]{8}$/.test(phone);
}

// ─── Error Parsing ────────────────────────────────────────────────────────────

export function parseApiError(error: unknown): string {
  if (error && typeof error === 'object' && 'response' in error) {
    const axiosError = error as { response?: { data?: { message?: string } } };
    const msg = axiosError.response?.data?.message;
    if (Array.isArray(msg)) return msg.join('، ');
    if (typeof msg === 'string') return msg;
  }
  if (error instanceof Error) return error.message;
  return 'حدث خطأ غير متوقع، يرجى المحاولة مرة أخرى';
}

// ─── Alert Severity Colors ────────────────────────────────────────────────────

export const SEVERITY_COLORS = {
  red: 'text-red-500 bg-red-50 border-red-200 dark:bg-red-950/30 dark:border-red-800',
  orange: 'text-orange-500 bg-orange-50 border-orange-200 dark:bg-orange-950/30 dark:border-orange-800',
  yellow: 'text-yellow-600 bg-yellow-50 border-yellow-200 dark:bg-yellow-950/30 dark:border-yellow-800',
  blue: 'text-blue-500 bg-blue-50 border-blue-200 dark:bg-blue-950/30 dark:border-blue-800',
} as const;
