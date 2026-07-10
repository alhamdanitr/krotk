'use client';

import React from 'react';
import { Wallet, Smartphone, MessageSquare } from 'lucide-react';
import { PageHeader, DataTable, Column, Badge, ActionButton } from '@/components';
import { useWallets, useSmsTemplates } from '@/hooks/use-modules';
import { formatDateTime } from '@/lib/utils';
import type { WalletConfig } from '@/types';

// ─── Wallets Page ─────────────────────────────────────────────────────────────

export default function WalletsConfigPage() {
  const { data: wallets = [], isLoading: walletsLoading } = useWallets();
  const { data: templates = [], isLoading: templatesLoading } = useSmsTemplates();

  const walletColumns: Column<WalletConfig>[] = [
    {
      key: 'name',
      header: 'اسم المحفظة',
      cell: (row) => (
        <div className="flex items-center gap-2">
          <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary/10 text-primary">
            <Wallet className="h-4 w-4" />
          </div>
          <span className="font-bold">{row.name}</span>
        </div>
      ),
    },
    {
      key: 'isActive',
      header: 'الحالة',
      cell: (row) => (
        <Badge variant={row.isActive ? 'success' : 'default'}>
          {row.isActive ? 'نشط' : 'متوقف'}
        </Badge>
      ),
    },
    {
      key: 'regexPattern',
      header: 'تنسيق قراءة الرسائل (Regex)',
      cell: (row) => (
        <div className="max-w-xs truncate font-mono text-xs text-muted-foreground bg-muted px-2 py-1 rounded">
          {row.regexPattern}
        </div>
      ),
    },
    {
      key: 'createdAt',
      header: 'تاريخ الإضافة',
      cell: (row) => formatDateTime(row.createdAt),
    },
    {
      key: 'actions',
      header: 'إجراءات',
      cell: () => (
        <ActionButton variant="secondary" size="sm">
          تعديل
        </ActionButton>
      ),
    },
  ];

  return (
    <div className="space-y-8">
      <PageHeader
        title="تخصيص المحافظ و SMS"
        description="إدارة قوالب وتنسيقات الرسائل للمحافظ الإلكترونية (الكريمي، جوالي، موبايل موني...)"
      />

      {/* ── Wallets Config ── */}
      <section className="space-y-4">
        <h2 className="text-lg font-bold flex items-center gap-2">
          <Smartphone className="h-5 w-5 text-primary" />
          المحافظ المدعومة
        </h2>
        <DataTable
          columns={walletColumns}
          data={wallets}
          keyExtractor={(row) => row.id}
          loading={walletsLoading}
          emptyIcon={<Wallet className="h-10 w-10" />}
          emptyMessage="لم يتم إعداد أي محافظ بعد."
        />
      </section>

      {/* ── SMS Templates ── */}
      <section className="space-y-4">
        <h2 className="text-lg font-bold flex items-center gap-2">
          <MessageSquare className="h-5 w-5 text-primary" />
          قوالب الإشعارات (SMS)
        </h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {templatesLoading ? (
            Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className="h-32 rounded-xl border border-border bg-card shimmer" />
            ))
          ) : templates.length === 0 ? (
            <div className="col-span-full rounded-xl border border-border bg-card p-8 text-center text-muted-foreground">
              لا توجد قوالب رسائل مخصصة
            </div>
          ) : (
            templates.map((template) => (
              <div key={template.id} className="rounded-xl border border-border bg-card p-5 space-y-3 relative group">
                <div className="absolute top-4 left-4 opacity-0 group-hover:opacity-100 transition-opacity">
                  <ActionButton variant="secondary" size="sm">تعديل</ActionButton>
                </div>
                <div className="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center text-primary">
                  <MessageSquare className="h-5 w-5" />
                </div>
                <p className="text-sm font-medium">{template.templateText}</p>
              </div>
            ))
          )}
        </div>
      </section>
    </div>
  );
}
