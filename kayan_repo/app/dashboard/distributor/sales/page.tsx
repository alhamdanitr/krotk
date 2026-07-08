'use client';

import React, { useState } from 'react';
import { ShoppingCart, Plus, Tag } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  FilterBar,
  SearchInput,
  SelectFilter,
  DataTable,
  Column,
  Pagination,
  ActionButton,
  Modal,
  FormField,
  TextInput,
  Toast,
  Badge,
} from '@/components';
import {
  useDistributorSales,
  useCreateSale,
  useDistributorCustomers,
} from '@/hooks/use-distributor';
import { useStock, usePagination } from '@/hooks/use-modules';
import { formatCurrency, formatDateTime } from '@/lib/utils';
import type { DistributorSale } from '@/types';

// ─── Schema ───────────────────────────────────────────────────────────────────

const saleSchema = z.object({
  customerId: z.string().optional(),
  categoryValue: z.preprocess((val) => Number(val), z.number().min(1, 'اختر فئة الكرت')),
  cardType: z.string().min(1, 'نوع الكرت مطلوب'),
  quantity: z.preprocess((val) => Number(val), z.number().min(1, 'الكمية يجب أن تكون 1 على الأقل')),
  isCredit: z.boolean().default(false),
  receivedAmount: z.preprocess((val) => Number(val), z.number().min(0).optional()),
  notes: z.string().optional(),
});

type SaleFormData = z.infer<typeof saleSchema>;

// ─── Sales Page ───────────────────────────────────────────────────────────────

export default function DistributorSalesPage() {
  const { page, setPage, limit, search, setSearch } = usePagination(15);
  const [isCreditFilter, setIsCreditFilter] = useState('');

  // Queries
  const { data: response, isLoading } = useDistributorSales({
    page,
    limit,
    search,
    ...(isCreditFilter ? { isCredit: isCreditFilter === 'true' } : {}),
  });
  const sales = response?.data ?? [];
  const meta = response?.meta ?? { total: 0, totalPages: 0, page: 1, limit };

  const { data: stockData } = useStock();
  const { data: customersResponse } = useDistributorCustomers({ limit: 100 });
  const customers = customersResponse?.data ?? [];

  // Mutations
  const createMutation = useCreateSale();

  // States
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [toast, setToast] = useState<{ type: 'success' | 'error'; message: string } | null>(null);

  const { register, handleSubmit, reset, watch, formState: { errors, isSubmitting } } = useForm<SaleFormData>({
    resolver: zodResolver(saleSchema),
    defaultValues: {
      categoryValue: 0,
      quantity: 1,
      isCredit: false,
      receivedAmount: 0,
    },
  });

  const isCreditSelected = watch('isCredit');

  // Handlers
  const openAddModal = () => {
    reset({ categoryValue: 0, quantity: 1, isCredit: false, receivedAmount: 0 });
    setIsModalOpen(true);
  };

  const onSubmit = async (data: SaleFormData) => {
    try {
      await createMutation.mutateAsync(data);
      setToast({ type: 'success', message: 'تم تسجيل الفاتورة بنجاح' });
      setIsModalOpen(false);
    } catch (err: any) {
      setToast({ type: 'error', message: err.message || 'حدث خطأ أثناء حفظ الفاتورة' });
    }
  };

  // Columns
  const columns: Column<DistributorSale>[] = [
    {
      key: 'invoiceNumber',
      header: 'رقم الفاتورة',
      cell: (row) => <span className="font-mono">{row.invoiceNumber}</span>,
    },
    {
      key: 'customer',
      header: 'العميل',
      cell: (row) => (
        <span className="font-semibold">{row.customer?.name || 'مبيعات نقدية (بدون عميل)'}</span>
      ),
    },
    {
      key: 'items',
      header: 'الصنف (فئة × نوع)',
      cell: (row) => (
        <div className="flex flex-col gap-1">
          {row.items.map((item, i) => (
            <span key={i} className="text-xs">
              فئة {item.categoryValue} ({item.cardType}) × {item.quantity}
            </span>
          ))}
        </div>
      ),
    },
    {
      key: 'totalAmount',
      header: 'الإجمالي',
      cell: (row) => <span className="font-bold">{formatCurrency(row.totalAmount)}</span>,
    },
    {
      key: 'isCredit',
      header: 'نوع الدفع',
      cell: (row) => (
        <Badge variant={row.isCredit ? 'warning' : 'success'}>
          {row.isCredit ? 'آجل (دين)' : 'نقدي'}
        </Badge>
      ),
    },
    {
      key: 'createdAt',
      header: 'تاريخ الفاتورة',
      cell: (row) => formatDateTime(row.createdAt),
    },
  ];

  return (
    <div className="space-y-6">
      {toast && (
        <Toast type={toast.type} message={toast.message} onClose={() => setToast(null)} />
      )}

      {/* ── Filters & Actions ── */}
      <FilterBar>
        <div className="flex-1 min-w-[200px]">
          <SearchInput
            value={search}
            onChange={(v) => { setSearch(v); setPage(1); }}
            placeholder="ابحث برقم الفاتورة..."
          />
        </div>
        <SelectFilter
          value={isCreditFilter}
          onChange={(v) => { setIsCreditFilter(v); setPage(1); }}
          placeholder="طريقة الدفع"
          options={[
            { label: 'نقدي', value: 'false' },
            { label: 'آجل (دين)', value: 'true' },
          ]}
        />
        <ActionButton variant="primary" icon={<Plus className="h-4 w-4" />} onClick={openAddModal}>
          فاتورة جديدة
        </ActionButton>
      </FilterBar>

      {/* ── Data Table ── */}
      <DataTable
        columns={columns}
        data={sales}
        keyExtractor={(row) => row.id}
        loading={isLoading}
        emptyIcon={<ShoppingCart className="h-10 w-10" />}
        emptyMessage="لم يتم العثور على مبيعات تطابق بحثك."
      />

      {/* ── Pagination ── */}
      <Pagination
        page={meta.page}
        totalPages={meta.totalPages}
        total={meta.total}
        limit={meta.limit}
        onPageChange={setPage}
      />

      {/* ── Add Sale Modal ── */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="إنشاء فاتورة مبيعات جديدة"
        size="md"
        footer={
          <>
            <ActionButton variant="secondary" onClick={() => setIsModalOpen(false)}>
              إلغاء
            </ActionButton>
            <ActionButton
              variant="primary"
              onClick={handleSubmit(onSubmit)}
              loading={isSubmitting || createMutation.isPending}
            >
              حفظ الفاتورة
            </ActionButton>
          </>
        }
      >
        <form id="sale-form" className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <FormField label="العميل (اختياري للنقدي)" error={errors.customerId?.message}>
              <select
                {...register('customerId')}
                className="w-full rounded-lg border border-border bg-input py-2.5 px-3 text-sm focus:ring-2 focus:ring-primary/50"
              >
                <option value="">مبيعات نقدية عامة</option>
                {customers.map((c) => (
                  <option key={c.id} value={c.id}>{c.name}</option>
                ))}
              </select>
            </FormField>
            
            <FormField label="فئة الكرت" error={errors.categoryValue?.message} required>
              <select
                {...register('categoryValue')}
                className="w-full rounded-lg border border-border bg-input py-2.5 px-3 text-sm focus:ring-2 focus:ring-primary/50"
              >
                <option value="0">اختر الفئة...</option>
                {stockData?.map((s) => (
                  <option key={s.categoryValue} value={s.categoryValue}>
                    فئة {s.categoryValue} (متاح: {s.count})
                  </option>
                ))}
              </select>
            </FormField>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <FormField label="نوع الكرت" error={errors.cardType?.message} required>
              <select
                {...register('cardType')}
                className="w-full rounded-lg border border-border bg-input py-2.5 px-3 text-sm focus:ring-2 focus:ring-primary/50"
              >
                <option value="yemenmobile">يمن موبايل</option>
                <option value="sabafon">سبأفون</option>
                <option value="you">You</option>
                <option value="y">واي</option>
                <option value="internet">انترنت</option>
              </select>
            </FormField>

            <FormField label="الكمية" error={errors.quantity?.message} required>
              <TextInput {...register('quantity')} type="number" min="1" disabled={isSubmitting} />
            </FormField>
          </div>

          <div className="flex items-center gap-3 py-2 border-y border-border/50">
            <input
              type="checkbox"
              id="isCredit"
              {...register('isCredit')}
              className="h-4 w-4 rounded border-border text-primary focus:ring-primary"
            />
            <label htmlFor="isCredit" className="text-sm font-medium">فاتورة آجلة (تسجل كدين على العميل)</label>
          </div>

          {isCreditSelected && (
            <FormField label="المبلغ المستلم (دفعة أولى إن وجد)" error={errors.receivedAmount?.message}>
              <TextInput {...register('receivedAmount')} type="number" min="0" disabled={isSubmitting} />
            </FormField>
          )}

          <FormField label="ملاحظات" error={errors.notes?.message}>
            <TextInput {...register('notes')} placeholder="أي ملاحظات إضافية..." disabled={isSubmitting} />
          </FormField>
        </form>
      </Modal>
    </div>
  );
}
