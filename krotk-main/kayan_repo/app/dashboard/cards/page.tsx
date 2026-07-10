'use client';

import React, { useState } from 'react';
import { CreditCard, Upload, Plus, Package, CheckCircle2, XCircle } from 'lucide-react';
import {
  PageHeader,
  FilterBar,
  SearchInput,
  SelectFilter,
  DataTable,
  Column,
  Pagination,
  Badge,
  ActionButton,
} from '@/components';
import { useCards, useStock, usePagination } from '@/hooks/use-modules';
import { formatDateTime } from '@/lib/utils';
import type { Card } from '@/types';

// ─── Columns ──────────────────────────────────────────────────────────────────

const columns: Column<Card>[] = [
  {
    key: 'code',
    header: 'رقم الكرت',
    cell: (row) => <span className="font-mono font-medium">{row.code}</span>,
  },
  {
    key: 'categoryValue',
    header: 'الفئة',
    cell: (row) => (
      <Badge variant="default">
        {row.categoryValue} ريال
      </Badge>
    ),
  },
  {
    key: 'username',
    header: 'اسم المستخدم (لتطبيق الشبكة)',
    cell: (row) => row.username || '—',
  },
  {
    key: 'password',
    header: 'كلمة المرور',
    cell: (row) => row.password || '—',
  },
  {
    key: 'isUsed',
    header: 'الحالة',
    cell: (row) => (
      <Badge variant={row.isUsed ? 'danger' : 'success'}>
        {row.isUsed ? 'مستخدم' : 'متاح'}
      </Badge>
    ),
  },
  {
    key: 'usedAt',
    header: 'تاريخ الاستخدام',
    cell: (row) => (row.usedAt ? formatDateTime(row.usedAt) : '—'),
  },
  {
    key: 'createdAt',
    header: 'تاريخ الإضافة',
    cell: (row) => formatDateTime(row.createdAt),
  },
];

// ─── Cards Page ───────────────────────────────────────────────────────────────

export default function CardsPage() {
  const { page, setPage, limit, search, setSearch } = usePagination(15);
  const [isUsedFilter, setIsUsedFilter] = useState('');

  // Queries
  const { data: stockData = [], isLoading: stockLoading } = useStock();
  const { data: cardsResponse, isLoading: cardsLoading } = useCards({
    page,
    limit,
    search,
    ...(isUsedFilter ? { isUsed: isUsedFilter === 'true' } : {}),
  });

  const cards = cardsResponse?.data ?? [];
  const meta = cardsResponse?.meta ?? { total: 0, totalPages: 0, page: 1, limit };

  // Handlers
  const handleSearch = (value: string) => {
    setSearch(value);
    setPage(1);
  };

  const handleFilterChange = (value: string) => {
    setIsUsedFilter(value);
    setPage(1);
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title="إدارة الكروت"
        description="استيراد وتوليد وإدارة كروت الشحن"
        actions={
          <>
            <ActionButton variant="secondary" icon={<Upload className="h-4 w-4" />}>
              استيراد دفعة
            </ActionButton>
            <ActionButton variant="primary" icon={<Plus className="h-4 w-4" />}>
              توليد كروت
            </ActionButton>
          </>
        }
      />

      {/* ── Stock Overview ── */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        {stockLoading ? (
          Array.from({ length: 4 }).map((_, i) => (
            <div key={i} className="h-24 rounded-xl border border-border bg-card shimmer" />
          ))
        ) : (
          stockData.map((stock) => (
            <div
              key={stock.categoryValue}
              className="flex items-center gap-4 rounded-xl border border-border bg-card p-4"
            >
              <div
                className={`flex h-12 w-12 items-center justify-center rounded-lg ${
                  stock.isLow
                    ? 'bg-amber-100 text-amber-600 dark:bg-amber-900/30 dark:text-amber-400'
                    : 'bg-primary/10 text-primary'
                }`}
              >
                <Package className="h-6 w-6" />
              </div>
              <div>
                <p className="text-sm font-medium text-muted-foreground">فئة {stock.categoryValue}</p>
                <div className="flex items-center gap-2">
                  <h3 className="text-2xl font-bold text-foreground">{stock.count}</h3>
                  {stock.isLow && (
                    <span className="text-[10px] font-bold text-amber-600 dark:text-amber-400">
                      منخفض
                    </span>
                  )}
                </div>
              </div>
            </div>
          ))
        )}
      </div>

      {/* ── Filters ── */}
      <FilterBar>
        <div className="flex-1 min-w-[200px]">
          <SearchInput
            value={search}
            onChange={handleSearch}
            placeholder="ابحث برقم الكرت أو اسم المستخدم..."
          />
        </div>
        <SelectFilter
          value={isUsedFilter}
          onChange={handleFilterChange}
          placeholder="جميع الحالات"
          options={[
            { label: 'متاح', value: 'false' },
            { label: 'مستخدم', value: 'true' },
          ]}
        />
      </FilterBar>

      {/* ── Data Table ── */}
      <DataTable
        columns={columns}
        data={cards}
        keyExtractor={(row) => row.id}
        loading={cardsLoading}
        emptyIcon={<CreditCard className="h-10 w-10" />}
        emptyMessage="لم يتم العثور على أي كروت تطابق معايير البحث."
      />

      {/* ── Pagination ── */}
      <Pagination
        page={meta.page}
        totalPages={meta.totalPages}
        total={meta.total}
        limit={meta.limit}
        onPageChange={setPage}
      />
    </div>
  );
}
