// =============================================================================
// KUROTEK ADMIN — Central Type Definitions
// =============================================================================

// ─── Auth ────────────────────────────────────────────────────────────────────

export type UserRole = 'super_admin' | 'tenant_admin' | 'operator';

export interface AuthUser {
  id: string;
  username: string;
  role: UserRole;
}

export interface TenantStatus {
  networkName: string;
  status: 'trial' | 'active' | 'suspended';
  expiryDate: string | null;
  remainingDays: number;
  planType: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  user: AuthUser;
  tenant: TenantStatus | null;
}

export interface LoginCredentials {
  username: string;
  password: string;
}

export interface RegisterTenantPayload {
  username: string;
  password: string;
  networkName: string;
  logo?: string;
  country: string;
  currency: string;
}

export interface ActivateSerialPayload {
  serialKey: string;
  activationCode: string;
}

// ─── Dashboard ───────────────────────────────────────────────────────────────

export interface InventoryCategory {
  categoryValue: number;
  count: number;
  isLow: boolean;
}

export interface DashboardOverview {
  smsEngine: {
    today: {
      salesAmount: number;
      salesCount: number;
      depositsAmount: number;
      depositsCount: number;
    };
    month: {
      salesAmount: number;
      salesCount: number;
    };
  };
  distributor: {
    today: {
      salesAmount: number;
      salesCount: number;
      grossProfit: number;
      expenses: number;
      netProfit: number;
    };
    month: {
      salesAmount: number;
      salesCount: number;
      grossProfit: number;
    };
  };
  inventory: {
    totalAvailable: number;
    byCategory: InventoryCategory[];
  };
  pendingApprovals: number;
  totalReceivables: number;
  subscription: {
    planName: string;
    expiryDate: string;
    daysRemaining: number;
    status: string;
  } | null;
}

export type AlertSeverity = 'red' | 'orange' | 'yellow' | 'blue';
export type AlertType =
  | 'pending_approvals'
  | 'low_inventory'
  | 'subscription_expiry'
  | 'subscription_expired'
  | 'credit_over_limit'
  | 'credit_near_limit';

export interface DashboardAlert {
  type: AlertType;
  severity: AlertSeverity;
  message: string;
  data?: { customerId?: string };
}

export interface ChartDataPoint {
  date: string;
  smsSales: number;
  deposits: number;
  distributorSales: number;
  distributorProfit: number;
}

export type ActivityType = 'sms_transaction' | 'distributor_sale';

export interface RecentActivity {
  type: ActivityType;
  date: string;
  amount: number;
  detail: string;
}

// ─── Cards / Inventory ───────────────────────────────────────────────────────

export interface Card {
  id: string;
  tenantId: string;
  categoryId: string;
  categoryValue: number;
  code: string;
  username?: string;
  password?: string;
  isUsed: boolean;
  usedAt?: string;
  createdAt: string;
}

export interface Category {
  id: string;
  tenantId: string;
  value: number;
}

export interface BulkAddCardsPayload {
  categoryValue: number;
  rawText: string;
  formatMode: 'code_only' | 'user_pass';
}

// ─── Deposits ────────────────────────────────────────────────────────────────

export interface Deposit {
  id: string;
  tenantId: string;
  senderPhone: string;
  senderCode?: string;
  amount: number;
  walletType: string;
  isShared: boolean;
  rawSmsText?: string;
  createdAt: string;
}

// ─── Transactions ─────────────────────────────────────────────────────────────

export interface Transaction {
  id: string;
  tenantId: string;
  depositId?: string;
  cardId?: string;
  amount: number;
  recipientPhone: string;
  createdAt: string;
}

// ─── Pending Approvals ────────────────────────────────────────────────────────

export interface PendingApproval {
  id: string;
  tenantId: string;
  depositId: string;
  isAccountCode: boolean;
  status: 'pending' | 'approved' | 'rejected';
  createdAt: string;
}

// ─── Wallets ─────────────────────────────────────────────────────────────────

export interface WalletConfig {
  id: string;
  tenantId: string;
  walletName: string;
  regexPattern: string;
  isEnabled: boolean;
}

// ─── Reports ─────────────────────────────────────────────────────────────────

export interface ReportSummary {
  totalDeposits: number;
  totalTransactions: number;
  totalAmount: number;
}

// ─── Distributor ─────────────────────────────────────────────────────────────

export interface DistributorCustomer {
  id: string;
  tenantId: string;
  name: string;
  phone?: string;
  address?: string;
  creditLimit: number;
  isActive: boolean;
  createdAt: string;
}

export interface DistributorSale {
  id: string;
  tenantId: string;
  customerId?: string;
  categoryValue: number;
  cardType: string;
  quantity: number;
  buyPrice: number;
  sellPrice: number;
  totalAmount: number;
  profit: number;
  receivedAmount: number;
  isCredit: boolean;
  notes?: string;
  createdAt: string;
  customer?: { name: string };
}

export interface DistributorPricing {
  id: string;
  tenantId: string;
  categoryValue: number;
  cardType: string;
  buyPrice: number;
  sellPrice: number;
  isActive: boolean;
}

// ─── API Generic Wrappers ─────────────────────────────────────────────────────

export interface ApiResponse<T> {
  data: T;
  message?: string;
}

export interface PaginatedResponse<T> {
  data: T[];
  total: number;
  page: number;
  limit: number;
}

export interface ApiError {
  statusCode: number;
  message: string;
  error?: string;
}

// ─── UI State ─────────────────────────────────────────────────────────────────

export interface TableColumn<T> {
  key: keyof T | string;
  header: string;
  cell?: (row: T) => React.ReactNode;
  sortable?: boolean;
  className?: string;
}

export type SortDirection = 'asc' | 'desc';

export interface SortState {
  key: string;
  direction: SortDirection;
}
