'use client';

import React, { useState } from 'react';
import {
  CreditCard,
  MessageSquare,
  Clock,
  TrendingUp,
  DollarSign,
  Package,
  AlertTriangle,
  ArrowUpRight,
  Users,
  BarChart2,
  Zap,
} from 'lucide-react';
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from 'recharts';
import { useTheme } from 'next-themes';
import {
  useDashboardOverview,
  useDashboardAlerts,
  useDashboardCharts,
  useRecentActivity,
} from '@/hooks/use-dashboard';
import { StatCard } from '@/components/ui/stat-card';
import {
  StatCardSkeleton,
  RowSkeleton,
  Skeleton,
} from '@/components/ui/loading';
import { ErrorState } from '@/components/ui/states';
import { Badge } from '@/components/ui/badge';
import { cn, formatCurrency, formatRelativeTime, SEVERITY_COLORS } from '@/lib/utils';
import type { DashboardAlert, AlertSeverity } from '@/types';

// ─── Alert Banner ─────────────────────────────────────────────────────────────

function AlertBanner({ alert }: { alert: DashboardAlert }) {
  return (
    <div
      className={cn(
        'flex items-center gap-3 rounded-lg border px-4 py-3 text-sm',
        SEVERITY_COLORS[alert.severity],
      )}
    >
      <AlertTriangle className="h-4 w-4 flex-shrink-0" />
      <span className="flex-1">{alert.message}</span>
    </div>
  );
}

// ─── Chart Period Selector ────────────────────────────────────────────────────

const PERIODS = [
  { label: '7 أيام', value: 7 },
  { label: '30 يوم', value: 30 },
  { label: '90 يوم', value: 90 },
] as const;

type Period = (typeof PERIODS)[number]['value'];

// ─── Activity Type Icon ───────────────────────────────────────────────────────

function ActivityIcon({ type }: { type: string }) {
  if (type === 'sms_transaction') {
    return (
      <div className="flex h-9 w-9 items-center justify-center rounded-full bg-blue-100 text-blue-600 dark:bg-blue-900/30 dark:text-blue-400 flex-shrink-0">
        <MessageSquare className="h-4 w-4" />
      </div>
    );
  }
  return (
    <div className="flex h-9 w-9 items-center justify-center rounded-full bg-green-100 text-green-600 dark:bg-green-900/30 dark:text-green-400 flex-shrink-0">
      <Package className="h-4 w-4" />
    </div>
  );
}

// ─── Section Header ───────────────────────────────────────────────────────────

function SectionHeader({
  title,
  subtitle,
  action,
}: {
  title: string;
  subtitle?: string;
  action?: React.ReactNode;
}) {
  return (
    <div className="flex items-center justify-between gap-4">
      <div>
        <h2 className="text-base font-semibold text-foreground">{title}</h2>
        {subtitle && <p className="text-sm text-muted-foreground">{subtitle}</p>}
      </div>
      {action}
    </div>
  );
}

// ─── Dashboard Page ───────────────────────────────────────────────────────────

export default function DashboardPage() {
  const [chartPeriod, setChartPeriod] = useState<Period>(30);
  const { theme } = useTheme();
  const isDark = theme === 'dark';

  const {
    data: overview,
    isLoading: overviewLoading,
    error: overviewError,
    refetch: refetchOverview,
  } = useDashboardOverview();

  const { data: alerts = [], isLoading: alertsLoading } = useDashboardAlerts();
  const { data: chartData = [], isLoading: chartLoading } = useDashboardCharts(chartPeriod);
  const { data: activity = [], isLoading: activityLoading } = useRecentActivity(10);

  // ─── Error state
  if (overviewError) {
    return (
      <ErrorState
        error={overviewError}
        onRetry={refetchOverview}
      />
    );
  }

  const gridColor = isDark ? 'rgba(255,255,255,0.06)' : 'rgba(0,0,0,0.06)';
  const tickColor = isDark ? '#94A3B8' : '#64748B';

  return (
    <div className="space-y-6">
      {/* ── Page Header ──────────────────────────────────────────────────── */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-foreground">لوحة التحكم</h1>
          <p className="text-sm text-muted-foreground mt-0.5">
            نظرة شاملة على أداء الشبكة اليوم
          </p>
        </div>
        <div className="flex items-center gap-2">
          <div className="pulse-dot" />
          <span className="text-xs text-muted-foreground">مباشر</span>
        </div>
      </div>

      {/* ── Alerts ───────────────────────────────────────────────────────── */}
      {!alertsLoading && alerts.length > 0 && (
        <div className="space-y-2">
          {alerts.slice(0, 3).map((alert, i) => (
            <AlertBanner key={i} alert={alert} />
          ))}
          {alerts.length > 3 && (
            <p className="text-xs text-muted-foreground text-center">
              و {alerts.length - 3} تنبيهات أخرى
            </p>
          )}
        </div>
      )}

      {/* ── SMS Engine Stats ──────────────────────────────────────────────── */}
      <div>
        <div className="flex items-center gap-2 mb-3">
          <Zap className="h-4 w-4 text-primary" />
          <h2 className="text-sm font-semibold text-muted-foreground uppercase tracking-wider">
            محرك SMS
          </h2>
        </div>
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-2 lg:grid-cols-4">
          {overviewLoading ? (
            Array.from({ length: 4 }).map((_, i) => <StatCardSkeleton key={i} />)
          ) : (
            <>
              <StatCard
                title="مبيعات اليوم"
                value={formatCurrency(overview?.smsEngine.today.salesAmount ?? 0)}
                subtitle={`${overview?.smsEngine.today.salesCount ?? 0} عملية`}
                icon={<DollarSign className="h-5 w-5" />}
                variant="success"
              />
              <StatCard
                title="مبيعات الشهر"
                value={formatCurrency(overview?.smsEngine.month.salesAmount ?? 0)}
                subtitle={`${overview?.smsEngine.month.salesCount ?? 0} عملية`}
                icon={<TrendingUp className="h-5 w-5" />}
                variant="info"
              />
              <StatCard
                title="إيداعات اليوم"
                value={formatCurrency(overview?.smsEngine.today.depositsAmount ?? 0)}
                subtitle={`${overview?.smsEngine.today.depositsCount ?? 0} إيداع`}
                icon={<MessageSquare className="h-5 w-5" />}
                variant="default"
              />
              <StatCard
                title="طلبات معلقة"
                value={overview?.pendingApprovals ?? 0}
                subtitle="تنتظر الموافقة"
                icon={<Clock className="h-5 w-5" />}
                variant={(overview?.pendingApprovals ?? 0) > 0 ? 'warning' : 'default'}
              />
            </>
          )}
        </div>
      </div>

      {/* ── Distributor Stats ─────────────────────────────────────────────── */}
      <div>
        <div className="flex items-center gap-2 mb-3">
          <Users className="h-4 w-4 text-primary" />
          <h2 className="text-sm font-semibold text-muted-foreground uppercase tracking-wider">
            الموزع
          </h2>
        </div>
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-2 lg:grid-cols-4">
          {overviewLoading ? (
            Array.from({ length: 4 }).map((_, i) => <StatCardSkeleton key={i} />)
          ) : (
            <>
              <StatCard
                title="مبيعات اليوم"
                value={formatCurrency(overview?.distributor.today.salesAmount ?? 0)}
                subtitle={`${overview?.distributor.today.salesCount ?? 0} فاتورة`}
                icon={<CreditCard className="h-5 w-5" />}
                variant="success"
              />
              <StatCard
                title="صافي الربح اليوم"
                value={formatCurrency(overview?.distributor.today.netProfit ?? 0)}
                subtitle={`إجمالي: ${formatCurrency(overview?.distributor.today.grossProfit ?? 0)}`}
                icon={<TrendingUp className="h-5 w-5" />}
                variant={(overview?.distributor.today.netProfit ?? 0) >= 0 ? 'success' : 'danger'}
              />
              <StatCard
                title="الذمم المستحقة"
                value={formatCurrency(overview?.totalReceivables ?? 0)}
                subtitle="إجمالي الائتمان"
                icon={<Users className="h-5 w-5" />}
                variant={(overview?.totalReceivables ?? 0) > 0 ? 'warning' : 'default'}
              />
              <StatCard
                title="المخزون المتاح"
                value={overview?.inventory.totalAvailable ?? 0}
                subtitle={`${overview?.inventory.byCategory.filter((c) => c.isLow).length ?? 0} فئات منخفضة`}
                icon={<Package className="h-5 w-5" />}
                variant={
                  (overview?.inventory.byCategory.filter((c) => c.isLow).length ?? 0) > 0
                    ? 'warning'
                    : 'default'
                }
              />
            </>
          )}
        </div>
      </div>

      {/* ── Chart + Activity ──────────────────────────────────────────────── */}
      <div className="grid grid-cols-1 gap-6 xl:grid-cols-3">
        {/* Chart — spans 2 cols */}
        <div className="xl:col-span-2 rounded-xl border border-border bg-card p-5 space-y-4">
          <div className="flex items-center justify-between gap-3 flex-wrap">
            <SectionHeader
              title="أداء المبيعات"
              subtitle="مبيعات SMS ومبيعات الموزع"
            />
            <div className="flex items-center gap-1 rounded-lg border border-border bg-muted p-1">
              {PERIODS.map((p) => (
                <button
                  key={p.value}
                  onClick={() => setChartPeriod(p.value)}
                  className={cn(
                    'rounded-md px-3 py-1.5 text-xs font-medium transition-all',
                    chartPeriod === p.value
                      ? 'bg-card text-foreground shadow-sm'
                      : 'text-muted-foreground hover:text-foreground',
                  )}
                >
                  {p.label}
                </button>
              ))}
            </div>
          </div>

          {chartLoading ? (
            <Skeleton className="h-[260px] w-full" />
          ) : (
            <ResponsiveContainer width="100%" height={260}>
              <AreaChart
                data={chartData}
                margin={{ top: 5, right: 5, left: -20, bottom: 0 }}
              >
                <defs>
                  <linearGradient id="gradSms" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#EF5350" stopOpacity={0.3} />
                    <stop offset="95%" stopColor="#EF5350" stopOpacity={0} />
                  </linearGradient>
                  <linearGradient id="gradDist" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#60a5fa" stopOpacity={0.3} />
                    <stop offset="95%" stopColor="#60a5fa" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid stroke={gridColor} strokeDasharray="3 3" />
                <XAxis
                  dataKey="date"
                  tick={{ fontSize: 11, fill: tickColor }}
                  tickFormatter={(d) => {
                    const dt = new Date(d);
                    return `${dt.getDate()}/${dt.getMonth() + 1}`;
                  }}
                  axisLine={false}
                  tickLine={false}
                />
                <YAxis
                  tick={{ fontSize: 11, fill: tickColor }}
                  tickFormatter={(v) => `${(v / 1000).toFixed(0)}k`}
                  axisLine={false}
                  tickLine={false}
                />
                <Tooltip
                  contentStyle={{
                    background: isDark ? '#131C2E' : '#fff',
                    border: `1px solid ${isDark ? 'rgba(255,255,255,0.06)' : '#E2E8F0'}`,
                    borderRadius: '10px',
                    fontSize: '12px',
                    direction: 'rtl',
                  }}
                  formatter={(value: number) => [formatCurrency(value), '']}
                  labelFormatter={(label: string) => {
                    const dt = new Date(label);
                    return dt.toLocaleDateString('ar-YE');
                  }}
                />
                <Legend
                  iconType="circle"
                  iconSize={8}
                  formatter={(value) =>
                    value === 'smsSales' ? 'مبيعات SMS' : 'مبيعات الموزع'
                  }
                  wrapperStyle={{ fontSize: 12, paddingTop: 12 }}
                />
                <Area
                  type="monotone"
                  dataKey="smsSales"
                  stroke="#EF5350"
                  strokeWidth={2}
                  fill="url(#gradSms)"
                  dot={false}
                  activeDot={{ r: 4, fill: '#EF5350' }}
                />
                <Area
                  type="monotone"
                  dataKey="distributorSales"
                  stroke="#60a5fa"
                  strokeWidth={2}
                  fill="url(#gradDist)"
                  dot={false}
                  activeDot={{ r: 4, fill: '#60a5fa' }}
                />
              </AreaChart>
            </ResponsiveContainer>
          )}
        </div>

        {/* Recent Activity — spans 1 col */}
        <div className="rounded-xl border border-border bg-card p-5 space-y-4">
          <SectionHeader
            title="آخر العمليات"
            subtitle="SMS + الموزع"
            action={
              <span className="text-xs text-muted-foreground">
                {activity.length} عملية
              </span>
            }
          />

          {activityLoading ? (
            <RowSkeleton rows={6} />
          ) : activity.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-10 text-center">
              <BarChart2 className="h-10 w-10 text-muted-foreground/30 mb-2" />
              <p className="text-sm text-muted-foreground">لا توجد عمليات بعد</p>
            </div>
          ) : (
            <div className="space-y-3 overflow-y-auto max-h-[300px] scrollbar-hide">
              {activity.map((item, i) => (
                <div
                  key={i}
                  className="flex items-center gap-3 rounded-lg p-2 hover:bg-muted/50 transition-colors"
                >
                  <ActivityIcon type={item.type} />
                  <div className="flex-1 min-w-0">
                    <p className="text-xs font-medium text-foreground truncate">
                      {item.detail}
                    </p>
                    <p className="text-[10px] text-muted-foreground">
                      {formatRelativeTime(item.date)}
                    </p>
                  </div>
                  <span className="text-xs font-semibold text-foreground flex-shrink-0">
                    {formatCurrency(item.amount)}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* ── Inventory by Category ─────────────────────────────────────────── */}
      <div className="rounded-xl border border-border bg-card p-5 space-y-4">
        <SectionHeader
          title="المخزون حسب الفئة"
          subtitle="عدد الكروت المتاحة لكل فئة"
          action={
            <Badge
              variant={
                (overview?.inventory.byCategory.filter((c) => c.isLow).length ?? 0) > 0
                  ? 'warning'
                  : 'success'
              }
            >
              {overviewLoading
                ? '…'
                : `${overview?.inventory.totalAvailable ?? 0} إجمالي`}
            </Badge>
          }
        />

        {overviewLoading ? (
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-3">
            {Array.from({ length: 5 }).map((_, i) => (
              <Skeleton key={i} className="h-20 rounded-xl" />
            ))}
          </div>
        ) : (overview?.inventory.byCategory.length ?? 0) === 0 ? (
          <p className="text-sm text-muted-foreground py-6 text-center">
            لا توجد كروت في المخزون
          </p>
        ) : (
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-3">
            {overview?.inventory.byCategory.map((cat) => (
              <div
                key={cat.categoryValue}
                className={cn(
                  'rounded-xl border p-4 text-center space-y-1',
                  cat.isLow
                    ? 'border-amber-200/50 bg-amber-50/50 dark:bg-amber-900/10 dark:border-amber-800/30'
                    : 'border-border bg-muted/30',
                )}
              >
                <p className="text-lg font-bold text-foreground">
                  {cat.count}
                </p>
                <p className="text-xs text-muted-foreground">
                  فئة {cat.categoryValue}
                </p>
                {cat.isLow && (
                  <Badge variant="warning" className="text-[10px] px-1.5">
                    منخفض
                  </Badge>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
