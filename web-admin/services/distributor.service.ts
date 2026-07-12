import apiClient from '@/lib/api-client';
import type { DistributorCustomer, DistributorSale, DistributorPricing } from '@/types';
import type { PaginationParams, PaginatedResult, DateRangeParams } from './modules.service';

// ─── Distributor Customers Service ────────────────────────────────────────────

export interface CustomerWithDebt extends DistributorCustomer {
  currentDebt: number;
  debtRatio: number;
}

export interface CustomerStatement {
  customer: DistributorCustomer;
  sales: DistributorSale[];
  payments: { id: string; amount: number; paymentMethod: string; createdAt: string }[];
  totalSales: number;
  totalPaid: number;
  balance: number;
}

export const distributorCustomersService = {
  /** GET /api/distributor/customers */
  async getCustomers(params?: PaginationParams): Promise<PaginatedResult<CustomerWithDebt>> {
    const { data } = await apiClient.get('/distributor/customers', { params });
    return data;
  },

  /** GET /api/distributor/customers/:id */
  async getCustomerById(id: string): Promise<CustomerWithDebt> {
    const { data } = await apiClient.get(`/distributor/customers/${id}`);
    return data;
  },

  /** POST /api/distributor/customers */
  async createCustomer(payload: {
    name: string;
    phone?: string;
    address?: string;
    creditLimit?: number;
  }): Promise<DistributorCustomer> {
    const { data } = await apiClient.post('/distributor/customers', payload);
    return data;
  },

  /** PUT /api/distributor/customers/:id */
  async updateCustomer(
    id: string,
    payload: { name?: string; phone?: string; address?: string; creditLimit?: number },
  ): Promise<DistributorCustomer> {
    const { data } = await apiClient.put(`/distributor/customers/${id}`, payload);
    return data;
  },

  /** DELETE /api/distributor/customers/:id */
  async deleteCustomer(id: string): Promise<void> {
    await apiClient.delete(`/distributor/customers/${id}`);
  },

  /** GET /api/distributor/customers/:id/statement */
  async getStatement(id: string): Promise<CustomerStatement> {
    const { data } = await apiClient.get(`/distributor/customers/${id}/statement`);
    return data;
  },

  /** POST /api/distributor/customers/:id/payments */
  async recordPayment(
    customerId: string,
    payload: { amount: number; paymentMethod: string; notes?: string },
  ): Promise<void> {
    await apiClient.post(`/distributor/customers/${customerId}/payments`, payload);
  },

  /** GET /api/distributor/customers/alerts */
  async getAlerts(): Promise<{ type: string; severity: string; message: string; data?: any }[]> {
    const { data } = await apiClient.get('/distributor/customers/alerts');
    return data;
  },
};

// ─── Distributor Sales Service ────────────────────────────────────────────────

export const distributorSalesService = {
  /** GET /api/distributor/sales */
  async getSales(
    params?: PaginationParams & { customerId?: string; isCredit?: boolean; categoryValue?: number },
  ): Promise<PaginatedResult<DistributorSale>> {
    const { data } = await apiClient.get('/distributor/sales', { params });
    return data;
  },

  /** POST /api/distributor/sales */
  async createSale(payload: {
    customerId?: string;
    categoryValue: number;
    cardType: string;
    quantity: number;
    receivedAmount?: number;
    isCredit?: boolean;
    notes?: string;
  }): Promise<DistributorSale> {
    const { data } = await apiClient.post('/distributor/sales', payload);
    return data;
  },

  /** GET /api/distributor/sales/:id */
  async getSaleById(id: string): Promise<DistributorSale> {
    const { data } = await apiClient.get(`/distributor/sales/${id}`);
    return data;
  },
};

// ─── Distributor Pricing Service ──────────────────────────────────────────────

export const distributorPricingService = {
  /** GET /api/distributor/pricing */
  async getPricing(): Promise<DistributorPricing[]> {
    const { data } = await apiClient.get('/distributor/pricing');
    return data;
  },

  /** PUT /api/distributor/pricing */
  async updatePricing(payload: {
    categoryValue: number;
    cardType: string;
    buyPrice: number;
    sellPrice: number;
  }): Promise<DistributorPricing> {
    const { data } = await apiClient.put('/distributor/pricing', payload);
    return data;
  },

  /** POST /api/distributor/pricing/reset */
  async resetPricing(): Promise<void> {
    await apiClient.post('/distributor/pricing/reset');
  },
};

// ─── Distributor Finance Service ──────────────────────────────────────────────

export interface FinanceDashboard {
  today: { sales: number; profit: number; expenses: number; netProfit: number };
  month: { sales: number; profit: number; expenses: number; netProfit: number };
  capital: { total: number; available: number };
  totalReceivables: number;
}

export interface ExpenseItem {
  id: string;
  category: string;
  amount: number;
  description?: string;
  expenseDate: string;
}

export interface CapitalEntry {
  id: string;
  type: 'injection' | 'withdrawal';
  amount: number;
  description?: string;
  createdAt: string;
}

export const distributorFinanceService = {
  /** GET /api/distributor/finance/dashboard */
  async getDashboard(): Promise<FinanceDashboard> {
    const { data } = await apiClient.get('/distributor/finance/dashboard');
    return data;
  },

  /** GET /api/distributor/finance/profits */
  async getProfits(
    params?: DateRangeParams & { period?: 'today' | 'week' | 'month' | 'year' | 'custom' },
  ): Promise<any> {
    const { data } = await apiClient.get('/distributor/finance/profits', { params });
    return data;
  },

  /** GET /api/distributor/finance/cash-flow */
  async getCashFlow(params?: DateRangeParams & { period?: string }): Promise<any> {
    const { data } = await apiClient.get('/distributor/finance/cash-flow', { params });
    return data;
  },

  /** GET /api/distributor/finance/receivables */
  async getReceivables(params?: PaginationParams): Promise<PaginatedResult<any>> {
    const { data } = await apiClient.get('/distributor/finance/receivables', { params });
    return data;
  },

  /** POST /api/distributor/finance/expenses */
  async createExpense(payload: {
    category: string;
    amount: number;
    description?: string;
    expenseDate?: string;
  }): Promise<ExpenseItem> {
    const { data } = await apiClient.post('/distributor/finance/expenses', payload);
    return data;
  },

  /** GET /api/distributor/finance/expenses */
  async getExpenses(
    params?: DateRangeParams & { period?: string; category?: string },
  ): Promise<any> {
    const { data } = await apiClient.get('/distributor/finance/expenses', { params });
    return data;
  },

  /** POST /api/distributor/finance/capital */
  async createCapital(payload: {
    type: 'injection' | 'withdrawal';
    amount: number;
    description?: string;
  }): Promise<CapitalEntry> {
    const { data } = await apiClient.post('/distributor/finance/capital', payload);
    return data;
  },

  /** GET /api/distributor/finance/capital */
  async getCapitalHistory(params?: PaginationParams): Promise<PaginatedResult<CapitalEntry>> {
    const { data } = await apiClient.get('/distributor/finance/capital', { params });
    return data;
  },

  /** GET /api/distributor/finance/top-customers */
  async getTopCustomers(params?: DateRangeParams & { period?: string }): Promise<any[]> {
    const { data } = await apiClient.get('/distributor/finance/top-customers', { params });
    return data;
  },

  /** GET /api/distributor/finance/top-products */
  async getTopProducts(params?: DateRangeParams & { period?: string }): Promise<any[]> {
    const { data } = await apiClient.get('/distributor/finance/top-products', { params });
    return data;
  },
};
