'use client';

import React, { useState } from 'react';
import { Clock, CheckCircle2, XCircle, FileText } from 'lucide-react';
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
  Modal,
  ConfirmDialog,
  Toast,
} from '@/components';
import { useDeposits, usePagination } from '@/hooks/use-modules';
import { formatCurrency, formatDateTime } from '@/lib/utils';
import type { Deposit } from '@/types';

// ─── Pending Approvals Page ───────────────────────────────────────────────────

export default function PendingApprovalsPage() {
  const { page, setPage, limit, search, setSearch } = usePagination(10);
  const [statusFilter, setStatusFilter] = useState('pending');
  
  // Dialog States
  const [selectedDeposit, setSelectedDeposit] = useState<Deposit | null>(null);
  const [isDetailsOpen, setIsDetailsOpen] = useState(false);
  const [isConfirmApproveOpen, setIsConfirmApproveOpen] = useState(false);
  const [isConfirmRejectOpen, setIsConfirmRejectOpen] = useState(false);
  
  const [toast, setToast] = useState<{ type: 'success' | 'error' | 'warning'; message: string } | null>(null);

  // In a real scenario, useDeposits should support filtering by status
  const { data: depositsResponse, isLoading } = useDeposits({
    page,
    limit,
    search,
    // status: statusFilter // assuming backend supports this
  });

  const deposits = depositsResponse?.data ?? [];
  // For demo: filter locally if backend doesn't support it yet
  const filteredDeposits = deposits.filter((d) => d.status === statusFilter || statusFilter === '');
  const meta = depositsResponse?.meta ?? { total: 0, totalPages: 0, page: 1, limit };

  // Handlers
  const openDetails = (row: Deposit) => {
    setSelectedDeposit(row);
    setIsDetailsOpen(true);
  };

  const handleApprove = () => {
    setIsConfirmApproveOpen(false);
    setIsDetailsOpen(false);
    setToast({ type: 'success', message: `تمت الموافقة على الطلب ${selectedDeposit?.referenceId}` });
    // TODO: Call actual API mutation
  };

  const handleReject = () => {
    setIsConfirmRejectOpen(false);
    setIsDetailsOpen(false);
    setToast({ type: 'success', message: `تم رفض الطلب ${selectedDeposit?.referenceId}` });
    // TODO: Call actual API mutation
  };

  // Columns
  const columns: Column<Deposit>[] = [
    {
      key: 'referenceId',
      header: 'الرقم المرجعي',
      cell: (row) => <span className="font-mono font-medium">{row.referenceId}</span>,
    },
    {
      key: 'amount',
      header: 'المبلغ',
      cell: (row) => (
        <span className="font-bold text-foreground">
          {formatCurrency(row.amount)}
        </span>
      ),
    },
    {
      key: 'paymentMethod',
      header: 'طريقة الدفع',
      cell: (row) => row.paymentMethod || '—',
    },
    {
      key: 'status',
      header: 'الحالة',
      cell: (row) => {
        if (row.status === 'pending') return <Badge variant="warning">قيد الانتظار</Badge>;
        if (row.status === 'approved') return <Badge variant="success">مقبول</Badge>;
        return <Badge variant="danger">مرفوض</Badge>;
      },
    },
    {
      key: 'createdAt',
      header: 'تاريخ الطلب',
      cell: (row) => formatDateTime(row.createdAt),
    },
    {
      key: 'actions',
      header: 'إجراءات',
      cell: (row) => (
        <ActionButton
          variant="secondary"
          size="sm"
          icon={<FileText className="h-3.5 w-3.5" />}
          onClick={(e) => {
            e.stopPropagation();
            openDetails(row);
          }}
        >
          التفاصيل
        </ActionButton>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      {toast && (
        <Toast type={toast.type} message={toast.message} onClose={() => setToast(null)} />
      )}

      <PageHeader
        title="المعلقات (طلبات الموافقة)"
        description="إدارة ومراجعة طلبات الإيداع والتحويلات المعلقة"
      />

      {/* ── Filters ── */}
      <FilterBar>
        <div className="flex-1 min-w-[200px]">
          <SearchInput
            value={search}
            onChange={(v) => { setSearch(v); setPage(1); }}
            placeholder="ابحث بالرقم المرجعي..."
          />
        </div>
        <SelectFilter
          value={statusFilter}
          onChange={(v) => { setStatusFilter(v); setPage(1); }}
          placeholder="جميع الحالات"
          options={[
            { label: 'قيد الانتظار', value: 'pending' },
            { label: 'مقبول', value: 'approved' },
            { label: 'مرفوض', value: 'rejected' },
          ]}
        />
      </FilterBar>

      {/* ── Data Table ── */}
      <DataTable
        columns={columns}
        data={filteredDeposits}
        keyExtractor={(row) => row.id}
        loading={isLoading}
        emptyIcon={<Clock className="h-10 w-10" />}
        emptyMessage="لا توجد طلبات معلقة حالياً."
        onRowClick={openDetails}
      />

      {/* ── Pagination ── */}
      <Pagination
        page={meta.page}
        totalPages={Math.ceil(filteredDeposits.length / limit) || 1}
        total={filteredDeposits.length}
        limit={meta.limit}
        onPageChange={setPage}
      />

      {/* ── Details Modal ── */}
      <Modal
        isOpen={isDetailsOpen}
        onClose={() => setIsDetailsOpen(false)}
        title="تفاصيل الطلب"
        size="md"
        footer={
          selectedDeposit?.status === 'pending' ? (
            <>
              <ActionButton
                variant="danger"
                icon={<XCircle className="h-4 w-4" />}
                onClick={() => setIsConfirmRejectOpen(true)}
              >
                رفض الطلب
              </ActionButton>
              <ActionButton
                variant="success"
                icon={<CheckCircle2 className="h-4 w-4" />}
                onClick={() => setIsConfirmApproveOpen(true)}
              >
                الموافقة
              </ActionButton>
            </>
          ) : null
        }
      >
        {selectedDeposit && (
          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-4 rounded-xl border border-border bg-muted/30 p-4">
              <div>
                <p className="text-xs text-muted-foreground mb-1">الرقم المرجعي</p>
                <p className="font-mono text-sm font-medium">{selectedDeposit.referenceId}</p>
              </div>
              <div>
                <p className="text-xs text-muted-foreground mb-1">تاريخ الطلب</p>
                <p className="text-sm font-medium">{formatDateTime(selectedDeposit.createdAt)}</p>
              </div>
              <div>
                <p className="text-xs text-muted-foreground mb-1">المبلغ</p>
                <p className="text-lg font-bold text-primary">{formatCurrency(selectedDeposit.amount)}</p>
              </div>
              <div>
                <p className="text-xs text-muted-foreground mb-1">طريقة الدفع</p>
                <p className="text-sm font-medium">{selectedDeposit.paymentMethod || 'غير محدد'}</p>
              </div>
            </div>
            
            {/* Notes / Sender / Details */}
            {selectedDeposit.notes && (
              <div className="rounded-xl border border-border bg-card p-4">
                <p className="text-xs text-muted-foreground mb-2">ملاحظات / بيانات إضافية</p>
                <p className="text-sm">{selectedDeposit.notes}</p>
              </div>
            )}
            
            <div className="rounded-xl border border-border bg-card p-4">
              <p className="text-xs text-muted-foreground mb-2">سجل التدقيق (Audit Log)</p>
              <ul className="space-y-3 relative border-r-2 border-border pr-4 mr-2">
                <li className="relative">
                  <span className="absolute -right-[21px] top-1 h-3 w-3 rounded-full bg-primary ring-4 ring-background" />
                  <p className="text-xs text-muted-foreground">{formatDateTime(selectedDeposit.createdAt)}</p>
                  <p className="text-sm">تم إنشاء الطلب بواسطة النظام</p>
                </li>
                {selectedDeposit.status !== 'pending' && (
                  <li className="relative">
                    <span className={`absolute -right-[21px] top-1 h-3 w-3 rounded-full ring-4 ring-background ${selectedDeposit.status === 'approved' ? 'bg-green-500' : 'bg-red-500'}`} />
                    <p className="text-xs text-muted-foreground">{formatDateTime(selectedDeposit.updatedAt || new Date())}</p>
                    <p className="text-sm">
                      تم {selectedDeposit.status === 'approved' ? 'قبول' : 'رفض'} الطلب بواسطة المشرف
                    </p>
                  </li>
                )}
              </ul>
            </div>
          </div>
        )}
      </Modal>

      {/* ── Confirmation Dialogs ── */}
      <ConfirmDialog
        isOpen={isConfirmApproveOpen}
        onClose={() => setIsConfirmApproveOpen(false)}
        onConfirm={handleApprove}
        title="تأكيد الموافقة"
        message={`هل أنت متأكد من الموافقة على الطلب رقم ${selectedDeposit?.referenceId} بمبلغ ${selectedDeposit ? formatCurrency(selectedDeposit.amount) : ''}؟ سيتم تحديث الرصيد تلقائياً.`}
        confirmLabel="نعم، موافقة"
        variant="default"
      />

      <ConfirmDialog
        isOpen={isConfirmRejectOpen}
        onClose={() => setIsConfirmRejectOpen(false)}
        onConfirm={handleReject}
        title="تأكيد الرفض"
        message={`هل أنت متأكد من رفض الطلب رقم ${selectedDeposit?.referenceId}؟`}
        confirmLabel="نعم، رفض"
        variant="danger"
      />
    </div>
  );
}
