'use client';

import React, { useState } from 'react';
import { Tag, RefreshCcw, Save } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  PageHeader,
  DataTable,
  Column,
  ActionButton,
  Modal,
  FormField,
  TextInput,
  Toast,
  ConfirmDialog,
} from '@/components';
import { useDistributorPricing, useUpdatePricing, useResetPricing } from '@/hooks/use-distributor';
import { formatCurrency } from '@/lib/utils';
import type { DistributorPricing } from '@/types';

// ─── Schema ───────────────────────────────────────────────────────────────────

const pricingSchema = z.object({
  categoryValue: z.number(),
  cardType: z.string(),
  buyPrice: z.preprocess((val) => Number(val), z.number().min(0)),
  sellPrice: z.preprocess((val) => Number(val), z.number().min(0)),
}).refine(data => data.sellPrice >= data.buyPrice, {
  message: "سعر البيع يجب أن يكون أكبر من أو يساوي سعر الشراء",
  path: ["sellPrice"]
});

type PricingFormData = z.infer<typeof pricingSchema>;

// ─── Pricing Page ─────────────────────────────────────────────────────────────

export default function DistributorPricingPage() {
  const { data: pricingList = [], isLoading } = useDistributorPricing();
  const updateMutation = useUpdatePricing();
  const resetMutation = useResetPricing();

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isConfirmResetOpen, setIsConfirmResetOpen] = useState(false);
  const [editingPricing, setEditingPricing] = useState<DistributorPricing | null>(null);
  const [toast, setToast] = useState<{ type: 'success' | 'error'; message: string } | null>(null);

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<PricingFormData>({
    resolver: zodResolver(pricingSchema),
  });

  const openEditModal = (pricing: DistributorPricing) => {
    setEditingPricing(pricing);
    reset({
      categoryValue: pricing.categoryValue,
      cardType: pricing.cardType,
      buyPrice: pricing.buyPrice,
      sellPrice: pricing.sellPrice,
    });
    setIsModalOpen(true);
  };

  const onSubmit = async (data: PricingFormData) => {
    try {
      await updateMutation.mutateAsync(data);
      setToast({ type: 'success', message: 'تم تحديث التسعيرة بنجاح' });
      setIsModalOpen(false);
    } catch (err: any) {
      setToast({ type: 'error', message: err.message || 'حدث خطأ أثناء التحديث' });
    }
  };

  const handleReset = async () => {
    try {
      await resetMutation.mutateAsync();
      setToast({ type: 'success', message: 'تم إعادة ضبط التسعيرة للقيم الافتراضية' });
      setIsConfirmResetOpen(false);
    } catch (err: any) {
      setToast({ type: 'error', message: err.message || 'حدث خطأ' });
    }
  };

  const columns: Column<DistributorPricing>[] = [
    {
      key: 'cardType',
      header: 'الشركة',
      cell: (row) => <span className="font-semibold">{row.cardType}</span>,
    },
    {
      key: 'categoryValue',
      header: 'الفئة (ريال)',
      cell: (row) => <span className="font-bold text-primary">{row.categoryValue}</span>,
    },
    {
      key: 'buyPrice',
      header: 'سعر الشراء (التكلفة)',
      cell: (row) => formatCurrency(row.buyPrice),
    },
    {
      key: 'sellPrice',
      header: 'سعر البيع',
      cell: (row) => <span className="font-bold text-green-600 dark:text-green-400">{formatCurrency(row.sellPrice)}</span>,
    },
    {
      key: 'profit',
      header: 'الربح المتوقع',
      cell: (row) => {
        const profit = row.sellPrice - row.buyPrice;
        return <span className={profit > 0 ? 'text-success' : profit < 0 ? 'text-destructive' : 'text-muted-foreground'}>
          {formatCurrency(profit)}
        </span>
      },
    },
    {
      key: 'actions',
      header: 'إجراءات',
      cell: (row) => (
        <ActionButton variant="secondary" size="sm" onClick={() => openEditModal(row)}>
          تعديل السعر
        </ActionButton>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      {toast && <Toast type={toast.type} message={toast.message} onClose={() => setToast(null)} />}

      <PageHeader
        title="التسعيرة"
        description="إدارة أسعار الشراء والبيع لفئات الكروت المختلفة"
        actions={
          <ActionButton
            variant="danger"
            icon={<RefreshCcw className="h-4 w-4" />}
            onClick={() => setIsConfirmResetOpen(true)}
          >
            إعادة ضبط للأسعار الافتراضية
          </ActionButton>
        }
      />

      <DataTable
        columns={columns}
        data={pricingList}
        keyExtractor={(row) => `${row.cardType}-${row.categoryValue}`}
        loading={isLoading}
        emptyIcon={<Tag className="h-10 w-10" />}
      />

      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="تعديل التسعيرة"
        size="sm"
        footer={
          <>
            <ActionButton variant="secondary" onClick={() => setIsModalOpen(false)}>إلغاء</ActionButton>
            <ActionButton variant="primary" onClick={handleSubmit(onSubmit)} loading={isSubmitting || updateMutation.isPending} icon={<Save className="h-4 w-4" />}>
              حفظ
            </ActionButton>
          </>
        }
      >
        <form className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <p className="text-xs text-muted-foreground mb-1">الشركة</p>
              <p className="font-semibold">{editingPricing?.cardType}</p>
            </div>
            <div>
              <p className="text-xs text-muted-foreground mb-1">الفئة</p>
              <p className="font-bold text-primary">{editingPricing?.categoryValue}</p>
            </div>
          </div>
          
          <FormField label="سعر الشراء (التكلفة)" error={errors.buyPrice?.message}>
            <TextInput {...register('buyPrice')} type="number" step="0.01" />
          </FormField>
          
          <FormField label="سعر البيع" error={errors.sellPrice?.message}>
            <TextInput {...register('sellPrice')} type="number" step="0.01" />
          </FormField>
        </form>
      </Modal>

      <ConfirmDialog
        isOpen={isConfirmResetOpen}
        onClose={() => setIsConfirmResetOpen(false)}
        onConfirm={handleReset}
        title="إعادة ضبط التسعيرة"
        message="هل أنت متأكد من إعادة جميع الأسعار للقيم الافتراضية للشركة؟ ستفقد جميع تعديلاتك الحالية."
        confirmLabel="نعم، إعادة ضبط"
        variant="danger"
        isLoading={resetMutation.isPending}
      />
    </div>
  );
}
