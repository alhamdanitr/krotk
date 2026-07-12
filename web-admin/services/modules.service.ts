import apiClient from '@/lib/api-client';
import type {
  Card,
  PaginatedResponse,
  WalletConfig,
  Deposit,
  Transaction,
} from '@/types';

// ─── Pagination Params ────────────────────────────────────────────────────────

export interface PaginationParams {
  page?: number;
  limit?: number;
  search?: string;
  sortBy?: string;
  sortOrder?: 'asc' | 'desc';
}

export interface PaginatedResult<T> {
  data: T[];
  meta: {
    total: number;
    page: number;
    limit: number;
    totalPages: number;
  };
}

// ─── Cards Service ────────────────────────────────────────────────────────────

export const cardsService = {
  /** GET /api/cards — paginated with optional isUsed filter */
  async getCards(
    params: PaginationParams & { isUsed?: boolean },
  ): Promise<PaginatedResult<Card>> {
    const { data } = await apiClient.get('/cards', { params });
    return data;
  },

  /** GET /api/inventory — stock grouped by category */
  async getStock(): Promise<{ categoryValue: number; count: number; isLow: boolean }[]> {
    const { data } = await apiClient.get('/inventory');
    return data;
  },

  /** GET /api/cards?isUsed=false — available cards only */
  async getAvailableCards(params?: PaginationParams): Promise<PaginatedResult<Card>> {
    const { data } = await apiClient.get('/cards', {
      params: { ...params, isUsed: false },
    });
    return data;
  },
};

// ─── Deposits Service ─────────────────────────────────────────────────────────

export const depositsService = {
  /** GET /api/deposits — paginated */
  async getDeposits(params?: PaginationParams): Promise<PaginatedResult<Deposit>> {
    const { data } = await apiClient.get('/deposits', { params });
    return data;
  },
};

// ─── Transactions Service ─────────────────────────────────────────────────────

export const transactionsService = {
  /** GET /api/transactions — paginated */
  async getTransactions(params?: PaginationParams): Promise<PaginatedResult<Transaction>> {
    const { data } = await apiClient.get('/transactions', { params });
    return data;
  },
};

// ─── Wallets Service ──────────────────────────────────────────────────────────

export const walletsService = {
  /** GET /api/wallets — all wallet configs */
  async getWallets(): Promise<WalletConfig[]> {
    const { data } = await apiClient.get('/wallets');
    return data;
  },

  /** GET /api/notifications/templates */
  async getSmsTemplates(): Promise<{ id: string; templateText: string }[]> {
    const { data } = await apiClient.get('/notifications/templates');
    return data;
  },
};

// ─── Reports Service ──────────────────────────────────────────────────────────

export interface DateRangeParams {
  startDate?: string;
  endDate?: string;
}

export interface ProfitsReport {
  totalSales: number;
  estimatedProfit: number;
  margin: string;
}

export interface SummaryReport {
  deposits: { totalAmount: number; count: number };
  sales: { totalAmount: number; count: number };
}

export const reportsService = {
  /** GET /api/reports/profits */
  async getProfits(params?: DateRangeParams): Promise<ProfitsReport> {
    const { data } = await apiClient.get('/reports/profits', { params });
    return data;
  },

  /** GET /api/reports/summary */
  async getSummary(params?: DateRangeParams): Promise<SummaryReport> {
    const { data } = await apiClient.get('/reports/summary', { params });
    return data;
  },
};
