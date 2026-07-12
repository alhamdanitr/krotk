'use client';

import React, { useState } from 'react';
import { Database, Download, Upload, ServerCrash, Archive, RefreshCw, Trash2, HardDrive, FileJson } from 'lucide-react';
import { PageHeader, DataTable, Column, ActionButton, ConfirmDialog, Toast, Badge } from '@/components';
import { useBackups, useCreateBackup, useRestoreBackup, useDeleteBackup, useExportSettings, useImportSettings, BackupFile } from '@/hooks/use-backups';
import { formatDateTime } from '@/lib/utils';

export default function SettingsAndBackupsPage() {
  const { data: backups = [], isLoading } = useBackups();
  const createMutation = useCreateBackup();
  const restoreMutation = useRestoreBackup();
  const deleteMutation = useDeleteBackup();
  const exportSettingsQuery = useExportSettings();
  const importSettingsMutation = useImportSettings();

  const [toast, setToast] = useState<{ type: 'success' | 'error'; message: string } | null>(null);
  const [confirmRestore, setConfirmRestore] = useState<string | null>(null);
  const [confirmDelete, setConfirmDelete] = useState<string | null>(null);

  // Handlers
  const handleCreateBackup = async () => {
    try {
      const res = await createMutation.mutateAsync();
      setToast({ type: 'success', message: res.message || 'تم أخذ نسخة احتياطية بنجاح' });
    } catch (err: any) {
      setToast({ type: 'error', message: err?.response?.data?.message || 'فشل في إنشاء النسخة الاحتياطية' });
    }
  };

  const handleRestore = async () => {
    if (!confirmRestore) return;
    try {
      const res = await restoreMutation.mutateAsync(confirmRestore);
      setToast({ type: 'success', message: res.message || 'تم استعادة النظام بنجاح' });
      setConfirmRestore(null);
    } catch (err: any) {
      setToast({ type: 'error', message: err?.response?.data?.message || 'فشل في استعادة النسخة الاحتياطية' });
      setConfirmRestore(null);
    }
  };

  const handleDelete = async () => {
    if (!confirmDelete) return;
    try {
      const res = await deleteMutation.mutateAsync(confirmDelete);
      setToast({ type: 'success', message: res.message || 'تم حذف النسخة بنجاح' });
      setConfirmDelete(null);
    } catch (err: any) {
      setToast({ type: 'error', message: err?.response?.data?.message || 'فشل في حذف النسخة' });
      setConfirmDelete(null);
    }
  };

  const handleExportSettings = () => {
    if (exportSettingsQuery.data) {
      const dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(exportSettingsQuery.data, null, 2));
      const downloadAnchorNode = document.createElement('a');
      downloadAnchorNode.setAttribute("href", dataStr);
      downloadAnchorNode.setAttribute("download", "kurotek_settings.json");
      document.body.appendChild(downloadAnchorNode); // required for firefox
      downloadAnchorNode.click();
      downloadAnchorNode.remove();
    }
  };

  const handleImportSettingsClick = () => {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = '.json';
    input.onchange = async (e: any) => {
      const file = e.target.files[0];
      if (!file) return;
      const reader = new FileReader();
      reader.onload = async (event) => {
        try {
          const json = JSON.parse(event.target?.result as string);
          await importSettingsMutation.mutateAsync(json);
          setToast({ type: 'success', message: 'تم استيراد الإعدادات بنجاح' });
        } catch (error) {
          setToast({ type: 'error', message: 'ملف إعدادات غير صالح' });
        }
      };
      reader.readAsText(file);
    };
    input.click();
  };

  // Format Bytes to human readable
  const formatBytes = (bytes: number, decimals = 2) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const dm = decimals < 0 ? 0 : decimals;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
  };

  // Columns
  const columns: Column<BackupFile>[] = [
    {
      key: 'filename',
      header: 'اسم الملف',
      cell: (row) => (
        <div className="flex items-center gap-2">
          <Database className="h-4 w-4 text-muted-foreground" />
          <span className="font-medium font-mono text-sm">{row.filename}</span>
        </div>
      ),
    },
    {
      key: 'size',
      header: 'الحجم',
      cell: (row) => <Badge variant="secondary">{formatBytes(row.size)}</Badge>,
    },
    {
      key: 'createdAt',
      header: 'تاريخ الإنشاء',
      cell: (row) => formatDateTime(row.createdAt),
    },
    {
      key: 'actions',
      header: 'الإجراءات',
      cell: (row) => (
        <div className="flex gap-2">
          <ActionButton
            variant="danger"
            size="sm"
            icon={<ServerCrash className="h-4 w-4" />}
            onClick={() => setConfirmRestore(row.filename)}
          >
            استعادة
          </ActionButton>
          <ActionButton
            variant="secondary"
            size="sm"
            icon={<Trash2 className="h-4 w-4 text-destructive" />}
            onClick={() => setConfirmDelete(row.filename)}
          />
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-8">
      {toast && <Toast type={toast.type} message={toast.message} onClose={() => setToast(null)} />}

      <PageHeader
        title="الإعدادات المتقدمة والنسخ الاحتياطي"
        description="إدارة النسخ الاحتياطية لقواعد البيانات واستيراد/تصدير إعدادات النظام بالكامل"
        actions={
          <ActionButton
            variant="primary"
            icon={<Archive className="h-4 w-4" />}
            onClick={handleCreateBackup}
            loading={createMutation.isPending}
          >
            أخذ نسخة احتياطية الآن
          </ActionButton>
        }
      />

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Settings Import/Export Card */}
        <div className="rounded-xl border border-border bg-card p-6 space-y-4">
          <div className="flex items-center gap-3">
            <div className="p-3 bg-primary/10 rounded-lg text-primary">
              <FileJson className="h-6 w-6" />
            </div>
            <div>
              <h3 className="font-bold text-lg">إعدادات النظام</h3>
              <p className="text-sm text-muted-foreground">قم بنسخ واستعادة إعدادات التطبيق والتسعيرات</p>
            </div>
          </div>
          <div className="flex gap-4 pt-4 border-t border-border/50">
            <ActionButton
              variant="secondary"
              className="flex-1"
              icon={<Download className="h-4 w-4" />}
              onClick={handleExportSettings}
              loading={exportSettingsQuery.isLoading}
            >
              تصدير الإعدادات
            </ActionButton>
            <ActionButton
              variant="secondary"
              className="flex-1"
              icon={<Upload className="h-4 w-4" />}
              onClick={handleImportSettingsClick}
              loading={importSettingsMutation.isPending}
            >
              استيراد الإعدادات
            </ActionButton>
          </div>
        </div>

        {/* Auto Backup Configuration Card */}
        <div className="rounded-xl border border-border bg-card p-6 space-y-4">
          <div className="flex items-center gap-3">
            <div className="p-3 bg-blue-500/10 rounded-lg text-blue-500">
              <RefreshCw className="h-6 w-6" />
            </div>
            <div>
              <h3 className="font-bold text-lg">النسخ الاحتياطي التلقائي</h3>
              <p className="text-sm text-muted-foreground">جدولة المهام لأخذ نسخ احتياطية دورية</p>
            </div>
          </div>
          <div className="pt-4 border-t border-border/50 flex items-center justify-between">
            <div>
              <p className="font-medium text-sm">الحالة: <Badge variant="success">مفعل يومياً</Badge></p>
              <p className="text-xs text-muted-foreground mt-1">يتم الحفظ في مسار: /backups</p>
            </div>
            <ActionButton variant="secondary" size="sm">
              تعديل الجدولة
            </ActionButton>
          </div>
        </div>
      </div>

      {/* Backups List */}
      <div className="space-y-4">
        <h2 className="text-xl font-bold flex items-center gap-2">
          <HardDrive className="h-5 w-5 text-primary" />
          سجل النسخ الاحتياطية (Database Dumps)
        </h2>
        <DataTable
          columns={columns}
          data={backups}
          keyExtractor={(row) => row.filename}
          loading={isLoading}
          emptyIcon={<Database className="h-10 w-10" />}
          emptyMessage="لا يوجد أي نسخ احتياطية مسجلة حالياً."
        />
      </div>

      {/* Dialogs */}
      <ConfirmDialog
        isOpen={!!confirmRestore}
        onClose={() => setConfirmRestore(null)}
        onConfirm={handleRestore}
        title="استعادة قاعدة البيانات"
        message={`هل أنت متأكد من استعادة النسخة "${confirmRestore}"؟ سيتم الكتابة فوق جميع البيانات الحالية بشكل نهائي، وهذه العملية لا يمكن التراجع عنها.`}
        confirmLabel="نعم، استعادة"
        variant="danger"
        isLoading={restoreMutation.isPending}
      />

      <ConfirmDialog
        isOpen={!!confirmDelete}
        onClose={() => setConfirmDelete(null)}
        onConfirm={handleDelete}
        title="حذف النسخة الاحتياطية"
        message={`هل أنت متأكد من حذف الملف "${confirmDelete}" نهائياً من السيرفر؟`}
        confirmLabel="حذف"
        variant="danger"
        isLoading={deleteMutation.isPending}
      />
    </div>
  );
}
