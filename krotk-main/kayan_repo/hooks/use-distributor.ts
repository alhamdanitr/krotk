'use client';

import { useQuery, useMutation, useQueryClient, keepPreviousData } from '@tanstack/react-query';
import {
  distributorCustomersService,
  distributorSalesService,
  distributorPricingService,
  distributorFinanceService,
} from '@/services/distributor.service';
import type { PaginationParams } from '@/services/modules.service';

// ─── Query Keys ───────────────────────────────────────────────────────────────

export const DISTRIBUTOR_KEYS = {
  customers: (params: object) => ['distributor', 'customers', params] as const,
  customer: (id: string) => ['distributor', 'customer', id] as const,
  customerStatement: (id: string) => ['distributor', 'customer', id, 'statement'] as const,
  customerAlerts: ['distributor', 'customers', 'alerts'] as const,
  sales: (params: object) => ['distributor', 'sales', params] as const,
  sale: (id: string) => ['distributor', 'sale', id] as const,
  pricing: ['distributor', 'pricing'] as const,
  financeDashboard: ['distributor', 'finance', 'dashboard'] as const,
  profits: (params: object) => ['distributor', 'finance', 'profits', params] as const,
  expenses: (params: object) => ['distributor', 'finance', 'expenses', params] as const,
  capital: (params: object) => ['distributor', 'finance', 'capital', params] as const,
  receivables: (params: object) => ['distributor', 'finance', 'receivables', params] as const,
  topCustomers: (params: object) => ['distributor', 'finance', 'top-customers', params] as const,
  topProducts: (params: object) => ['distributor', 'finance', 'top-products', params] as const,
};

// ─── Customers Hooks ──────────────────────────────────────────────────────────

export function useDistributorCustomers(params: PaginationParams = {}) {
  return useQuery({
    queryKey: DISTRIBUTOR_KEYS.customers(params),
    queryFn: () => distributorCustomersService.getCustomers(params),
    placeholderData: keepPreviousData,
    staleTime: 30_000,
  });
}

export function useDistributorCustomer(id: string) {
  return useQuery({
    queryKey: DISTRIBUTOR_KEYS.customer(id),
    queryFn: () => distributorCustomersService.getCustomerById(id),
    enabled: Boolean(id),
  });
}

export function useCustomerStatement(id: string) {
  return useQuery({
    queryKey: DISTRIBUTOR_KEYS.customerStatement(id),
    queryFn: () => distributorCustomersService.getStatement(id),
    enabled: Boolean(id),
    staleTime: 60_000,
  });
}

export function useCreateCustomer() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: distributorCustomersService.createCustomer,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['distributor', 'customers'] });
    },
  });
}

export function useUpdateCustomer() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: Parameters<typeof distributorCustomersService.updateCustomer>[1] }) =>
      distributorCustomersService.updateCustomer(id, payload),
    onSuccess: (_, { id }) => {
      qc.invalidateQueries({ queryKey: ['distributor', 'customers'] });
      qc.invalidateQueries({ queryKey: DISTRIBUTOR_KEYS.customer(id) });
    },
  });
}

export function useDeleteCustomer() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => distributorCustomersService.deleteCustomer(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['distributor', 'customers'] });
    },
  });
}

export function useRecordPayment(customerId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (payload: Parameters<typeof distributorCustomersService.recordPayment>[1]) =>
      distributorCustomersService.recordPayment(customerId, payload),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['distributor', 'customers'] });
      qc.invalidateQueries({ queryKey: DISTRIBUTOR_KEYS.customerStatement(customerId) });
      qc.invalidateQueries({ queryKey: ['distributor', 'finance'] });
    },
  });
}

// ─── Sales Hooks ──────────────────────────────────────────────────────────────

export function useDistributorSales(
  params: PaginationParams & { customerId?: string; isCredit?: boolean; categoryValue?: number } = {},
) {
  return useQuery({
    queryKey: DISTRIBUTOR_KEYS.sales(params),
    queryFn: () => distributorSalesService.getSales(params),
    placeholderData: keepPreviousData,
    staleTime: 30_000,
  });
}

export function useCreateSale() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: distributorSalesService.createSale,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['distributor', 'sales'] });
      qc.invalidateQueries({ queryKey: ['distributor', 'finance'] });
      qc.invalidateQueries({ queryKey: ['distributor', 'customers'] });
    },
  });
}

// ─── Pricing Hooks ────────────────────────────────────────────────────────────

export function useDistributorPricing() {
  return useQuery({
    queryKey: DISTRIBUTOR_KEYS.pricing,
    queryFn: () => distributorPricingService.getPricing(),
    staleTime: 5 * 60_000,
  });
}

export function useUpdatePricing() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: distributorPricingService.updatePricing,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: DISTRIBUTOR_KEYS.pricing });
    },
  });
}

export function useResetPricing() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: distributorPricingService.resetPricing,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: DISTRIBUTOR_KEYS.pricing });
    },
  });
}

// ─── Finance Hooks ────────────────────────────────────────────────────────────

export function useFinanceDashboard() {
  return useQuery({
    queryKey: DISTRIBUTOR_KEYS.financeDashboard,
    queryFn: () => distributorFinanceService.getDashboard(),
    staleTime: 60_000,
    refetchInterval: 2 * 60_000,
  });
}

export function useFinanceProfits(
  params: { period?: string; startDate?: string; endDate?: string } = { period: 'month' },
) {
  return useQuery({
    queryKey: DISTRIBUTOR_KEYS.profits(params),
    queryFn: () => distributorFinanceService.getProfits(params),
    staleTime: 5 * 60_000,
  });
}

export function useFinanceExpenses(
  params: { period?: string; category?: string } = { period: 'month' },
) {
  return useQuery({
    queryKey: DISTRIBUTOR_KEYS.expenses(params),
    queryFn: () => distributorFinanceService.getExpenses(params),
    staleTime: 60_000,
  });
}

export function useCreateExpense() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: distributorFinanceService.createExpense,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['distributor', 'finance'] });
    },
  });
}

export function useCapitalHistory(params: PaginationParams = {}) {
  return useQuery({
    queryKey: DISTRIBUTOR_KEYS.capital(params),
    queryFn: () => distributorFinanceService.getCapitalHistory(params),
    placeholderData: keepPreviousData,
  });
}

export function useCreateCapital() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: distributorFinanceService.createCapital,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['distributor', 'finance'] });
    },
  });
}

export function useTopCustomers(params: { period?: string } = { period: 'month' }) {
  return useQuery({
    queryKey: DISTRIBUTOR_KEYS.topCustomers(params),
    queryFn: () => distributorFinanceService.getTopCustomers(params),
    staleTime: 5 * 60_000,
  });
}
