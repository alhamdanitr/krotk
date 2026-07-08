'use client';

import React, { useState } from 'react';
import { Users, Plus, FileText, AlertCircle, Edit } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  FilterBar,
  SearchInput,
  DataTable,
  Column,
  Pagination,
  ActionButton,
  Modal,
  FormField,
  TextInput,
  Toast,
} from '@/components';
import {
  useDistributorCustomers,
  useCreateCustomer,
  useUpdateCustomer,
} from '@/hooks/use-distributor';
import { usePagination } from '@/hooks/use-modules';
import { formatCurrency, formatDateTime } from '@/lib/utils';
import type { CustomerWithDebt } from '@/services/distributor.service';

// ─── Schema ───────────────────────────────────────────────────────────────────

const customerSchema = z.object({
  name: z.string().min(2, 'الاسم مطلوب'),
  phone: z.string().optional(),
  address: z.string().optional(),
  creditLimit: z.preprocess((val) => Number(val), z.number().min(0, 'يجب أن يكون صفر أو أكثر')),
});

type CustomerFormData = z.infer<typeof customerSchema>;

// ─── Customers Page ───────────────────────────────────────────────────────────

export default function DistributorCustomersPage() {
  const { page, setPage, limit, search, setSearch } = usePagination(15);
  
  // Queries
  const { data: response, isLoading } = useDistributorCustomers({ page, limit, search });
  const customers = response?.data ?? [];
  const meta = response?.meta ?? { total: 0, totalPages: 0, page: 1, limit };

  // Mutations
  const createMutation = useCreateCustomer();
  const updateMutation = useUpdateCustomer();

  // States
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingCustomer, setEditingCustomer] = useState<CustomerWithDebt | null>(null);
  const [toast, setToast] = useState<{ type: 'success' | 'error'; message: string } | null>(null);

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<CustomerFormData>({
    resolver: zodResolver(customerSchema),
    defaultValues: {
      creditLimit: 0,
    },
  });

  // Handlers
  const openAddModal = () => {
    setEditingCustomer(null);
    reset({ name: '', phone: '', address: '', creditLimit: 0 });
    setIsModalOpen(true);
  };

  const openEditModal = (customer: CustomerWithDebt) => {
    setEditingCustomer(customer);
    reset({
      name: customer.name,
      phone: customer.phone || '',
      address: customer.address || '',
      creditLimit: customer.creditLimit,
    });
    setIsModalOpen(true);
  };

  const onSubmit = async (data: CustomerFormData) => {
    try {
      if (editingCustomer) {
        await updateMutation.mutateAsync({ id: editingCustomer.id, payload: data });
        setToast({ type: 'success', message: 'تم تحديث العميل بنجاح' });
      } else {
        await createMutation.mutateAsync(data);
        setToast({ type: 'success', message: 'تمت إضافة العميل بنجاح' });
      }
      setIsModalOpen(false);
    } catch (err: any) {
      setToast({ type: 'error', message: err.message || 'حدث خطأ أثناء حفظ العميل' });
    }
  };

  // Columns
  const columns: Column<CustomerWithDebt>[] = [
    {
      key: 'name',
      header: 'الاسم',
      cell: (row) => <span className="font-semibold">{row.name}</span>,
    },
    {
      key: 'phone',
      header: 'رقم الهاتف',
      cell: (row) => row.phone || '—',
    },
    {
      key: 'creditLimit',
      header: 'حد الائتمان',
      cell: (row) => formatCurrency(row.creditLimit),
    },
    {
      key: 'currentDebt',
      header: 'الذمة الحالية',
      cell: (row) => (
        <span className={`font-bold ${row.currentDebt > 0 ? 'text-red-500' : 'text-green-500'}`}>
          {formatCurrency(row.currentDebt)}
        </span>
      ),
    },
    {
      key: 'debtRatio',
      header: 'نسبة الائتمان',
      cell: (row) => {
        const ratio = row.debtRatio * 100;
        return (
          <div className="flex items-center gap-2">
            <div className="w-24 h-2 rounded-full bg-muted overflow-hidden">
              <div
                className={`h-full ${ratio > 80 ? 'bg-red-500' : ratio > 50 ? 'bg-amber-500' : 'bg-green-500'}`}
                style={{ width: `${Math.min(ratio, 100)}%` }}
              />
            </div>
            <span className="text-xs text-muted-foreground">{ratio.toFixed(0)}%</span>
          </div>
        );
      },
    },
    {
      key: 'createdAt',
      header: 'تاريخ الإضافة',
      cell: (row) => formatDateTime(row.createdAt),
    },
    {
      key: 'actions',
      header: 'إجراءات',
      cell: (row) => (
        <div className="flex gap-2">
          <ActionButton
            variant="secondary"
            size="sm"
            icon={<Edit className="h-3.5 w-3.5" />}
            onClick={(e) => {
              e.stopPropagation();
              openEditModal(row);
            }}
          />
          <ActionButton
            variant="secondary"
            size="sm"
            icon={<FileText className="h-3.5 w-3.5" />}
            onClick={(e) => {
              e.stopPropagation();
              // Navigation to statement page or open statement modal
            }}
          >
            كشف حساب
          </ActionButton>
        </div>
      ),
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
            placeholder="ابحث باسم العميل أو رقم الهاتف..."
          />
        </div>
        <ActionButton variant="primary" icon={<Plus className="h-4 w-4" />} onClick={openAddModal}>
          إضافة عميل
        </ActionButton>
      </FilterBar>

      {/* ── Data Table ── */}
      <DataTable
        columns={columns}
        data={customers}
        keyExtractor={(row) => row.id}
        loading={isLoading}
        emptyIcon={<Users className="h-10 w-10" />}
        emptyMessage="لم يتم العثور على أي عملاء."
      />

      {/* ── Pagination ── */}
      <Pagination
        page={meta.page}
        totalPages={meta.totalPages}
        total={meta.total}
        limit={meta.limit}
        onPageChange={setPage}
      />

      {/* ── Add/Edit Modal ── */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingCustomer ? 'تعديل بيانات العميل' : 'إضافة عميل جديد'}
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
              حفظ العميل
            </ActionButton>
          </>
        }
      >
        <form id="customer-form" className="space-y-4">
          <FormField label="اسم العميل" error={errors.name?.message} required>
            <TextInput {...register('name')} placeholder="أدخل اسم العميل" disabled={isSubmitting} />
          </FormField>
          <FormField label="رقم الهاتف" error={errors.phone?.message}>
            <TextInput {...register('phone')} placeholder="مثال: 77XXXXXXX" disabled={isSubmitting} />
          </FormField>
          <FormField label="العنوان" error={errors.address?.message}>
            <TextInput {...register('address')} placeholder="أدخل العنوان" disabled={isSubmitting} />
          </FormField>
          <FormField label="حد الائتمان (الديون المسموحة)" error={errors.creditLimit?.message}>
            <TextInput
              {...register('creditLimit')}
              type="number"
              placeholder="0"
              disabled={isSubmitting}
            />
          </FormField>
        </form>
      </Modal>
    </div>
  );
}
