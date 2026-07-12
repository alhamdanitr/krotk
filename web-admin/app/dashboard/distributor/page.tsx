'use client';

import React from 'react';
import { DollarSign, TrendingUp, Users, ArrowDownToLine, ArrowUpFromLine, Activity } from 'lucide-react';
import { StatCard, StatCardSkeleton, ErrorState } from '@/components';
import { useFinanceDashboard } from '@/hooks/use-distributor';
import { formatCurrency } from '@/lib/utils';

export default function DistributorDashboardPage() {
  const { data, isLoading, error, refetch } = useFinanceDashboard();

  if (error) {
    return <ErrorState error={error} onRetry={refetch} />;
  }

  return (
    <div className="space-y-8">
      {/* ── Today's Stats ── */}
      <div>
        <h2 className="text-sm font-semibold text-muted-foreground uppercase tracking-wider mb-4 flex items-center gap-2">
          <Activity className="h-4 w-4" />
          إحصائيات اليوم
        </h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {isLoading ? (
            Array.from({ length: 4 }).map((_, i) => <StatCardSkeleton key={i} />)
          ) : (
            <>
              <StatCard
                title="مبيعات اليوم"
                value={formatCurrency(data?.today?.sales ?? 0)}
                icon={<DollarSign className="h-5 w-5" />}
                variant="success"
              />
              <StatCard
                title="أرباح اليوم"
                value={formatCurrency(data?.today?.netProfit ?? 0)}
                icon={<TrendingUp className="h-5 w-5" />}
                variant={(data?.today?.netProfit ?? 0) >= 0 ? 'success' : 'danger'}
              />
              <StatCard
                title="إجمالي الأرباح"
                value={formatCurrency(data?.today?.profit ?? 0)}
                icon={<TrendingUp className="h-5 w-5" />}
                variant="info"
              />
              <StatCard
                title="المصروفات"
                value={formatCurrency(data?.today?.expenses ?? 0)}
                icon={<ArrowDownToLine className="h-5 w-5" />}
                variant={(data?.today?.expenses ?? 0) > 0 ? 'warning' : 'default'}
              />
            </>
          )}
        </div>
      </div>

      {/* ── Month's Stats ── */}
      <div>
        <h2 className="text-sm font-semibold text-muted-foreground uppercase tracking-wider mb-4 flex items-center gap-2">
          <Activity className="h-4 w-4" />
          إحصائيات الشهر
        </h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {isLoading ? (
            Array.from({ length: 4 }).map((_, i) => <StatCardSkeleton key={i} />)
          ) : (
            <>
              <StatCard
                title="مبيعات الشهر"
                value={formatCurrency(data?.month?.sales ?? 0)}
                icon={<DollarSign className="h-5 w-5" />}
                variant="success"
              />
              <StatCard
                title="صافي أرباح الشهر"
                value={formatCurrency(data?.month?.netProfit ?? 0)}
                icon={<TrendingUp className="h-5 w-5" />}
                variant={(data?.month?.netProfit ?? 0) >= 0 ? 'success' : 'danger'}
              />
              <StatCard
                title="إجمالي الأرباح"
                value={formatCurrency(data?.month?.profit ?? 0)}
                icon={<TrendingUp className="h-5 w-5" />}
                variant="info"
              />
              <StatCard
                title="المصروفات"
                value={formatCurrency(data?.month?.expenses ?? 0)}
                icon={<ArrowDownToLine className="h-5 w-5" />}
                variant={(data?.month?.expenses ?? 0) > 0 ? 'warning' : 'default'}
              />
            </>
          )}
        </div>
      </div>

      {/* ── Capital & Receivables ── */}
      <div>
        <h2 className="text-sm font-semibold text-muted-foreground uppercase tracking-wider mb-4 flex items-center gap-2">
          <Activity className="h-4 w-4" />
          رأس المال والذمم
        </h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {isLoading ? (
            Array.from({ length: 3 }).map((_, i) => <StatCardSkeleton key={i} />)
          ) : (
            <>
              <StatCard
                title="رأس المال الإجمالي"
                value={formatCurrency(data?.capital?.total ?? 0)}
                icon={<ArrowUpFromLine className="h-5 w-5" />}
                variant="info"
              />
              <StatCard
                title="رأس المال المتاح"
                value={formatCurrency(data?.capital?.available ?? 0)}
                icon={<DollarSign className="h-5 w-5" />}
                variant="success"
              />
              <StatCard
                title="الذمم (الديون الخارجية)"
                value={formatCurrency(data?.totalReceivables ?? 0)}
                icon={<Users className="h-5 w-5" />}
                variant={(data?.totalReceivables ?? 0) > 0 ? 'warning' : 'default'}
              />
            </>
          )}
        </div>
      </div>
    </div>
  );
}
