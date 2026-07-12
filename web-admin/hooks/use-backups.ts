import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import axios from 'axios';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'https://kurotek-backend-production.up.railway.app';

const api = axios.create({
  baseURL: API_URL,
});

api.interceptors.request.use((config) => {
  const token = typeof window !== 'undefined' ? localStorage.getItem('token') : null;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export interface BackupFile {
  filename: string;
  size: number;
  createdAt: string;
}

export function useBackups() {
  return useQuery({
    queryKey: ['backups'],
    queryFn: async (): Promise<BackupFile[]> => {
      const res = await api.get('/backups');
      return res.data.data;
    },
  });
}

export function useCreateBackup() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async () => {
      const res = await api.post('/backups');
      return res.data;
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['backups'] });
    },
  });
}

export function useRestoreBackup() {
  return useMutation({
    mutationFn: async (filename: string) => {
      const res = await api.post(`/backups/restore/${filename}`);
      return res.data;
    },
  });
}

export function useDeleteBackup() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (filename: string) => {
      const res = await api.delete(`/backups/${filename}`);
      return res.data;
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['backups'] });
    },
  });
}

export function useExportSettings() {
  return useQuery({
    queryKey: ['settings', 'export'],
    queryFn: async () => {
      const res = await api.get('/backups/settings');
      return res.data.data;
    },
  });
}

export function useImportSettings() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (settings: any) => {
      const res = await api.post('/backups/settings', settings);
      return res.data;
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['settings'] });
    },
  });
}
