'use client';

import { useQuery } from '@tanstack/react-query';
import { dashboardService } from '@/services/dashboard.service';

// ─── Query Keys ───────────────────────────────────────────────────────────────

export const DASHBOARD_KEYS = {
  overview: ['dashboard', 'overview'] as const,
  alerts: ['dashboard', 'alerts'] as const,
  charts: (days: number) => ['dashboard', 'charts', days] as const,
  recentActivity: (limit: number) => ['dashboard', 'recent-activity', limit] as const,
};

// ─── Hooks ────────────────────────────────────────────────────────────────────

export function useDashboardOverview() {
  return useQuery({
    queryKey: DASHBOARD_KEYS.overview,
    queryFn: () => dashboardService.getOverview(),
    staleTime: 30_000,       // 30 seconds
    refetchInterval: 60_000, // auto-refresh every minute
  });
}

export function useDashboardAlerts() {
  return useQuery({
    queryKey: DASHBOARD_KEYS.alerts,
    queryFn: () => dashboardService.getAlerts(),
    staleTime: 60_000,
    refetchInterval: 2 * 60_000,
  });
}

export function useDashboardCharts(days = 30) {
  return useQuery({
    queryKey: DASHBOARD_KEYS.charts(days),
    queryFn: () => dashboardService.getCharts(days),
    staleTime: 5 * 60_000, // chart data changes less frequently
  });
}

export function useRecentActivity(limit = 10) {
  return useQuery({
    queryKey: DASHBOARD_KEYS.recentActivity(limit),
    queryFn: () => dashboardService.getRecentActivity(limit),
    staleTime: 30_000,
    refetchInterval: 60_000,
  });
}
