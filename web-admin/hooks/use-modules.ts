'use client';

import { useQuery, useMutation, useQueryClient, keepPreviousData } from '@tanstack/react-query';
import { useState } from 'react';
import {
  cardsService,
  depositsService,
  transactionsService,
  walletsService,
  reportsService,
} from '@/services/modules.service';
import type { PaginationParams } from '@/services/modules.service';

// ─── Query Keys ───────────────────────────────────────────────────────────────

export const QUERY_KEYS = {
  cards: (params: object) => ['cards', params] as const,
  stock: ['inventory', 'stock'] as const,
  deposits: (params: object) => ['deposits', params] as const,
  transactions: (params: object) => ['transactions', params] as const,
  wallets: ['wallets'] as const,
  smsTemplates: ['notifications', 'templates'] as const,
  reportsProfits: (params: object) => ['reports', 'profits', params] as const,
  reportsSummary: (params: object) => ['reports', 'summary', params] as const,
};

// ─── Pagination Hook ──────────────────────────────────────────────────────────

export function usePagination(initialLimit = 20) {
  const [page, setPage] = useState(1);
  const [limit] = useState(initialLimit);
  const [search, setSearch] = useState('');

  return { page, setPage, limit, search, setSearch };
}

// ─── Cards Hooks ──────────────────────────────────────────────────────────────

export function useCards(params: PaginationParams & { isUsed?: boolean } = {}) {
  return useQuery({
    queryKey: QUERY_KEYS.cards(params),
    queryFn: () => cardsService.getCards(params),
    placeholderData: keepPreviousData,
    staleTime: 30_000,
  });
}

export function useStock() {
  return useQuery({
    queryKey: QUERY_KEYS.stock,
    queryFn: () => cardsService.getStock(),
    staleTime: 60_000,
    refetchInterval: 2 * 60_000,
  });
}

// ─── Deposits Hooks ───────────────────────────────────────────────────────────

export function useDeposits(params: PaginationParams = {}) {
  return useQuery({
    queryKey: QUERY_KEYS.deposits(params),
    queryFn: () => depositsService.getDeposits(params),
    placeholderData: keepPreviousData,
    staleTime: 30_000,
  });
}

// ─── Transactions Hooks ───────────────────────────────────────────────────────

export function useTransactions(params: PaginationParams = {}) {
  return useQuery({
    queryKey: QUERY_KEYS.transactions(params),
    queryFn: () => transactionsService.getTransactions(params),
    placeholderData: keepPreviousData,
    staleTime: 30_000,
  });
}

// ─── Wallets Hooks ────────────────────────────────────────────────────────────

export function useWallets() {
  return useQuery({
    queryKey: QUERY_KEYS.wallets,
    queryFn: () => walletsService.getWallets(),
    staleTime: 5 * 60_000,
  });
}

export function useSmsTemplates() {
  return useQuery({
    queryKey: QUERY_KEYS.smsTemplates,
    queryFn: () => walletsService.getSmsTemplates(),
    staleTime: 5 * 60_000,
  });
}

// ─── Reports Hooks ────────────────────────────────────────────────────────────

export function useReportsProfits(params: { startDate?: string; endDate?: string } = {}) {
  return useQuery({
    queryKey: QUERY_KEYS.reportsProfits(params),
    queryFn: () => reportsService.getProfits(params),
    staleTime: 5 * 60_000,
  });
}

export function useReportsSummary(params: { startDate?: string; endDate?: string } = {}) {
  return useQuery({
    queryKey: QUERY_KEYS.reportsSummary(params),
    queryFn: () => reportsService.getSummary(params),
    staleTime: 5 * 60_000,
  });
}
