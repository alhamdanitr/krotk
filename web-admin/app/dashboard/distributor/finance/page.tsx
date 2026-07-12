'use client';

import React, { useState } from 'react';
import { Landmark, Plus, ArrowDownToLine, ArrowUpFromLine, Banknote } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  PageHeader,
  FilterBar,
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
import { useCapitalHistory, useCreateCapital, useFinanceExpenses, useCreateExpense } from '@/hooks/use-distributor';
import { usePagination } from '@/hooks/use-modules';
import { formatCurrency, formatDateTime } from '@/lib/utils';
import type { CapitalEntry, ExpenseItem } from '@/services/distributor.service';

// ─── Schemas ──────────────────────────────────────────────────────────────────

const capitalSchema = z.object({
  type: z.enum(['injection', 'withdrawal']),
  amount: z.preprocess((val) => Number(val), z.number().min(1, 'المبلغ يجب أن يكون أكبر من 0')),
  description: z.string().optional(),
});

type CapitalFormData = z.infer<typeof capitalSchema>;

const expenseSchema = z.object({
  category: z.string().min(2, 'الفئة مطلوبة'),
  amount: z.preprocess((val) => Number(val), z.number().min(1, 'المبلغ يجب أن يكون أكبر من 0')),
  description: z.string().optional(),
});

type ExpenseFormData = z.infer<typeof expenseSchema>;

// ─── Finance Page ─────────────────────────────────────────────────────────────

export default function DistributorFinancePage() {
  const [activeTab, setActiveTab] = useState<'capital' | 'expenses'>('capital');
  const { page, setPage, limit } = usePagination(10);
  const [periodFilter, setPeriodFilter] = useState('month');

  // Queries
  const { data: capitalResponse, isLoading: capitalLoading } = useCapitalHistory({ page, limit });
  const capitalHistory = capitalResponse?.data ?? [];
  const capitalMeta = capitalResponse?.meta ?? { total: 0, totalPages: 0, page: 1, limit };

  const { data: expensesData, isLoading: expensesLoading } = useFinanceExpenses({ period: periodFilter });
  const expensesList: ExpenseItem[] = expensesData?.items ?? [];

  // Mutations
  const createCapitalMutation = useCreateCapital();
  const createExpenseMutation = useCreateExpense();

  // Modals
  const [isCapitalModalOpen, setIsCapitalModalOpen] = useState(false);
  const [isExpenseModalOpen, setIsExpenseModalOpen] = useState(false);
  const [toast, setToast] = useState<{ type: 'success' | 'error'; message: string } | null>(null);

  const capitalForm = useForm<CapitalFormData>({
    resolver: zodResolver(capitalSchema),
    defaultValues: { type: 'injection' },
  });

  const expenseForm = useForm<ExpenseFormData>({
    resolver: zodResolver(expenseSchema),
  });

  const onCapitalSubmit = async (data: CapitalFormData) => {
    try {
      await createCapitalMutation.mutateAsync(data);
      setToast({ type: 'success', message: 'تم تسجيل حركة رأس المال بنجاح' });
      setIsCapitalModalOpen(false);
      capitalForm.reset();
    } catch (err: any) {
      setToast({ type: 'error', message: err.message || 'حدث خطأ' });
    }
  };

  const onExpenseSubmit = async (data: ExpenseFormData) => {
    try {
      await createExpenseMutation.mutateAsync(data);
      setToast({ type: 'success', message: 'تم تسجيل المصروف بنجاح' });
      setIsExpenseModalOpen(false);
      expenseForm.reset();
    } catch (err: any) {
      setToast({ type: 'error', message: err.message || 'حدث خطأ' });
    }
  };

  // Columns
  const capitalColumns: Column<CapitalEntry>[] = [
    {
      key: 'type',
      header: 'نوع الحركة',
      cell: (row) => (
        <Badge variant={row.type === 'injection' ? 'success' : 'warning'}>
          {row.type === 'injection' ? 'إيداع (ضخ)' : 'سحب'}
        </Badge>
      ),
    },
    {
      key: 'amount',
      header: 'المبلغ',
      cell: (row) => (
        <span className={`font-bold ${row.type === 'injection' ? 'text-green-600 dark:text-green-400' : 'text-amber-600 dark:text-amber-400'}`}>
          {row.type === 'injection' ? '+' : '-'}{formatCurrency(row.amount)}
        </span>
      ),
    },
    {
      key: 'description',
      header: 'البيان',
      cell: (row) => row.description || '—',
    },
    {
      key: 'createdAt',
      header: 'التاريخ',
      cell: (row) => formatDateTime(row.createdAt),
    },
  ];

  const expenseColumns: Column<ExpenseItem>[] = [
    {
      key: 'category',
      header: 'الفئة',
      cell: (row) => <span className="font-semibold">{row.category}</span>,
    },
    {
      key: 'amount',
      header: 'المبلغ',
      cell: (row) => <span className="font-bold text-destructive">{formatCurrency(row.amount)}</span>,
    },
    {
      key: 'description',
      header: 'البيان',
      cell: (row) => row.description || '—',
    },
    {
      key: 'expenseDate',
      header: 'التاريخ',
      cell: (row) => formatDateTime(row.expenseDate),
    },
  ];

  return (
    <div className="space-y-6">
      {toast && <Toast type={toast.type} message={toast.message} onClose={() => setToast(null)} />}

      <div className="flex gap-4 border-b border-border pb-4">
        <button
          onClick={() => setActiveTab('capital')}
          className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
            activeTab === 'capital' ? 'bg-primary/10 text-primary' : 'text-muted-foreground hover:bg-muted'
          }`}
        >
          <Landmark className="h-4 w-4" />
          حركات رأس المال
        </button>
        <button
          onClick={() => setActiveTab('expenses')}
          className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
            activeTab === 'expenses' ? 'bg-primary/10 text-primary' : 'text-muted-foreground hover:bg-muted'
          }`}
        >
          <Banknote className="h-4 w-4" />
          المصروفات
        </button>
      </div>

      {activeTab === 'capital' && (
        <div className="space-y-4 animate-in fade-in">
          <FilterBar>
            <div className="flex-1" />
            <ActionButton variant="primary" icon={<Plus className="h-4 w-4" />} onClick={() => setIsCapitalModalOpen(true)}>
              تسجيل حركة رأس مال
            </ActionButton>
          </FilterBar>

          <DataTable
            columns={capitalColumns}
            data={capitalHistory}
            keyExtractor={(row) => row.id}
            loading={capitalLoading}
            emptyIcon={<Landmark className="h-10 w-10" />}
          />

          <Pagination
            page={capitalMeta.page}
            totalPages={capitalMeta.totalPages}
            total={capitalMeta.total}
            limit={capitalMeta.limit}
            onPageChange={setPage}
          />
        </div>
      )}

      {activeTab === 'expenses' && (
        <div className="space-y-4 animate-in fade-in">
          <FilterBar>
            <div className="flex-1 min-w-[200px]">
              <SelectFilter
                value={periodFilter}
                onChange={setPeriodFilter}
                options={[
                  { label: 'اليوم', value: 'today' },
                  { label: 'هذا الأسبوع', value: 'week' },
                  { label: 'هذا الشهر', value: 'month' },
                  { label: 'هذا العام', value: 'year' },
                ]}
                placeholder="الفترة الزمنية"
              />
            </div>
            <ActionButton variant="primary" icon={<Plus className="h-4 w-4" />} onClick={() => setIsExpenseModalOpen(true)}>
              إضافة مصروف
            </ActionButton>
          </FilterBar>

          <DataTable
            columns={expenseColumns}
            data={expensesList}
            keyExtractor={(row) => row.id}
            loading={expensesLoading}
            emptyIcon={<Banknote className="h-10 w-10" />}
          />
        </div>
      )}

      {/* ── Capital Modal ── */}
      <Modal
        isOpen={isCapitalModalOpen}
        onClose={() => setIsCapitalModalOpen(false)}
        title="تسجيل حركة رأس مال"
        size="sm"
        footer={
          <>
            <ActionButton variant="secondary" onClick={() => setIsCapitalModalOpen(false)}>إلغاء</ActionButton>
            <ActionButton variant="primary" onClick={capitalForm.handleSubmit(onCapitalSubmit)} loading={createCapitalMutation.isPending}>حفظ</ActionButton>
          </>
        }
      >
        <form className="space-y-4">
          <FormField label="نوع الحركة" error={capitalForm.formState.errors.type?.message}>
            <div className="grid grid-cols-2 gap-2">
              <label className={`flex items-center gap-2 p-3 rounded-xl border cursor-pointer transition-colors ${capitalForm.watch('type') === 'injection' ? 'border-primary bg-primary/5' : 'border-border'}`}>
                <input type="radio" value="injection" {...capitalForm.register('type')} className="hidden" />
                <ArrowDownToLine className={`h-5 w-5 ${capitalForm.watch('type') === 'injection' ? 'text-primary' : 'text-muted-foreground'}`} />
                <span className="font-medium">إيداع (ضخ)</span>
              </label>
              <label className={`flex items-center gap-2 p-3 rounded-xl border cursor-pointer transition-colors ${capitalForm.watch('type') === 'withdrawal' ? 'border-primary bg-primary/5' : 'border-border'}`}>
                <input type="radio" value="withdrawal" {...capitalForm.register('type')} className="hidden" />
                <ArrowUpFromLine className={`h-5 w-5 ${capitalForm.watch('type') === 'withdrawal' ? 'text-primary' : 'text-muted-foreground'}`} />
                <span className="font-medium">سحب من الرصيد</span>
              </label>
            </div>
          </FormField>

          <FormField label="المبلغ" error={capitalForm.formState.errors.amount?.message} required>
            <TextInput {...capitalForm.register('amount')} type="number" />
          </FormField>
          <FormField label="البيان" error={capitalForm.formState.errors.description?.message}>
            <TextInput {...capitalForm.register('description')} placeholder="سبب السحب/الإيداع..." />
          </FormField>
        </form>
      </Modal>

      {/* ── Expense Modal ── */}
      <Modal
        isOpen={isExpenseModalOpen}
        onClose={() => setIsExpenseModalOpen(false)}
        title="تسجيل مصروف جديد"
        size="sm"
        footer={
          <>
            <ActionButton variant="secondary" onClick={() => setIsExpenseModalOpen(false)}>إلغاء</ActionButton>
            <ActionButton variant="primary" onClick={expenseForm.handleSubmit(onExpenseSubmit)} loading={createExpenseMutation.isPending}>حفظ</ActionButton>
          </>
        }
      >
        <form className="space-y-4">
          <FormField label="الفئة (مثال: رواتب، إيجار، تشغيل...)" error={expenseForm.formState.errors.category?.message} required>
            <TextInput {...expenseForm.register('category')} placeholder="تصنيف المصروف" />
          </FormField>
          <FormField label="المبلغ" error={expenseForm.formState.errors.amount?.message} required>
            <TextInput {...expenseForm.register('amount')} type="number" />
          </FormField>
          <FormField label="البيان والتفاصيل" error={expenseForm.formState.errors.description?.message}>
            <TextInput {...expenseForm.register('description')} placeholder="تفاصيل المصروف..." />
          </FormField>
        </form>
      </Modal>
    </div>
  );
}
