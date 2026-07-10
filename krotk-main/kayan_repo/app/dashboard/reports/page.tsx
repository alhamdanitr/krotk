'use client';

import React, { useState } from 'react';
import { FileText, Download, TrendingUp, ArrowDownToLine, Activity } from 'lucide-react';
import {
  PageHeader,
  FilterBar,
  SelectFilter,
  StatCard,
  StatCardSkeleton,
  ActionButton,
  ErrorState,
} from '@/components';
import { useReportsSummary, useReportsProfits } from '@/hooks/use-modules';
import { formatCurrency } from '@/lib/utils';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts';

export default function ReportsPage() {
  const [period, setPeriod] = useState('month');

  // Simple date logic for demo purposes (could use date-fns)
  const getDates = (p: string) => {
    const now = new Date();
    const start = new Date();
    if (p === 'today') start.setHours(0, 0, 0, 0);
    else if (p === 'week') start.setDate(now.getDate() - 7);
    else if (p === 'month') start.setMonth(now.getMonth() - 1);
    else if (p === 'year') start.setFullYear(now.getFullYear() - 1);
    
    return {
      startDate: start.toISOString(),
      endDate: now.toISOString(),
    };
  };

  const dates = getDates(period);

  const { data: summary, isLoading: isSummaryLoading, error: summaryError, refetch: refetchSummary } = useReportsSummary(dates);
  const { data: profits, isLoading: isProfitsLoading, error: profitsError, refetch: refetchProfits } = useReportsProfits(dates);

  const isLoading = isSummaryLoading || isProfitsLoading;
  const hasError = summaryError || profitsError;

  const handleExport = (type: 'pdf' | 'excel') => {
    alert(`تصدير كـ ${type.toUpperCase()} غير متوفر حالياً في هذه النسخة التجريبية.`);
  };

  // Mock chart data based on summary
  const chartData = [
    {
      name: 'الإيداعات',
      amount: summary?.deposits.totalAmount || 0,
      count: summary?.deposits.count || 0,
    },
    {
      name: 'المبيعات',
      amount: summary?.sales.totalAmount || 0,
      count: summary?.sales.count || 0,
    },
  ];

  if (hasError) {
    return <ErrorState error={summaryError || profitsError} onRetry={() => { refetchSummary(); refetchProfits(); }} />;
  }

  return (
    <div className="space-y-8">
      <PageHeader
        title="التقارير والإحصائيات"
        description="تقارير مالية مفصلة للمبيعات والإيداعات والأرباح المتوقعة"
        actions={
          <div className="flex gap-2">
            <ActionButton variant="secondary" icon={<Download className="h-4 w-4" />} onClick={() => handleExport('excel')}>
              Excel
            </ActionButton>
            <ActionButton variant="primary" icon={<FileText className="h-4 w-4" />} onClick={() => handleExport('pdf')}>
              PDF
            </ActionButton>
          </div>
        }
      />

      <FilterBar>
        <div className="flex-1 min-w-[200px]">
          <SelectFilter
            value={period}
            onChange={setPeriod}
            placeholder="الفترة الزمنية"
            options={[
              { label: 'اليوم', value: 'today' },
              { label: 'آخر 7 أيام', value: 'week' },
              { label: 'هذا الشهر', value: 'month' },
              { label: 'هذا العام', value: 'year' },
            ]}
          />
        </div>
      </FilterBar>

      {/* ── Summary Stats ── */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {isLoading ? (
          Array.from({ length: 3 }).map((_, i) => <StatCardSkeleton key={i} />)
        ) : (
          <>
            <StatCard
              title="إجمالي المبيعات"
              value={formatCurrency(summary?.sales.totalAmount || 0)}
              description={`${summary?.sales.count || 0} عملية`}
              icon={<TrendingUp className="h-5 w-5" />}
              variant="success"
            />
            <StatCard
              title="إجمالي الإيداعات"
              value={formatCurrency(summary?.deposits.totalAmount || 0)}
              description={`${summary?.deposits.count || 0} عملية`}
              icon={<ArrowDownToLine className="h-5 w-5" />}
              variant="info"
            />
            <StatCard
              title="الأرباح المتوقعة"
              value={formatCurrency(profits?.estimatedProfit || 0)}
              description={`هامش ربح تقديري: ${profits?.margin || '2%'}`}
              icon={<Activity className="h-5 w-5" />}
              variant="default"
            />
          </>
        )}
      </div>

      {/* ── Chart ── */}
      <div className="rounded-xl border border-border bg-card p-6">
        <h3 className="text-lg font-bold mb-6">مقارنة الإيداعات والمبيعات</h3>
        <div className="h-[300px] w-full" dir="ltr">
          {isLoading ? (
            <div className="h-full w-full bg-muted/20 animate-pulse rounded-lg" />
          ) : (
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={chartData} margin={{ top: 10, right: 10, left: 10, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#334155" opacity={0.2} />
                <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fill: '#64748b' }} />
                <YAxis axisLine={false} tickLine={false} tick={{ fill: '#64748b' }} tickFormatter={(val) => `${val/1000}k`} />
                <Tooltip
                  cursor={{ fill: 'rgba(255, 255, 255, 0.05)' }}
                  contentStyle={{ backgroundColor: '#0f172a', borderColor: '#1e293b', borderRadius: '8px' }}
                  itemStyle={{ color: '#f8fafc' }}
                />
                <Bar dataKey="amount" fill="#3b82f6" radius={[4, 4, 0, 0]} maxBarSize={60} />
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>
      </div>
    </div>
  );
}
