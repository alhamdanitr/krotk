import apiClient from '@/lib/api-client';
import type {
  DashboardOverview,
  DashboardAlert,
  ChartDataPoint,
  RecentActivity,
} from '@/types';

// ─── Dashboard Service ────────────────────────────────────────────────────────

export const dashboardService = {
  /**
   * GET /api/dashboard/overview
   * Comprehensive stats: SMS engine, distributor, inventory, subscription.
   */
  async getOverview(): Promise<DashboardOverview> {
    const { data } = await apiClient.get<DashboardOverview>('/dashboard/overview');
    return data;
  },

  /**
   * GET /api/dashboard/alerts
   * All aggregated alerts sorted by severity.
   */
  async getAlerts(): Promise<DashboardAlert[]> {
    const { data } = await apiClient.get<DashboardAlert[]>('/dashboard/alerts');
    return data;
  },

  /**
   * GET /api/dashboard/charts?days=30
   * Chart data points for the last N days.
   */
  async getCharts(days = 30): Promise<ChartDataPoint[]> {
    const { data } = await apiClient.get<ChartDataPoint[]>('/dashboard/charts', {
      params: { days },
    });
    return data;
  },

  /**
   * GET /api/dashboard/recent-activity?limit=10
   * Latest merged activity from SMS + distributor.
   */
  async getRecentActivity(limit = 10): Promise<RecentActivity[]> {
    const { data } = await apiClient.get<RecentActivity[]>('/dashboard/recent-activity', {
      params: { limit },
    });
    return data;
  },
};
