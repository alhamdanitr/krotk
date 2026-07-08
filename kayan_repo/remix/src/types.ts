export enum CalculatorType {
  REGULAR = 'REGULAR',
  PRO = 'PRO'
}

export interface CardCategory {
  id: number;
  label: string;
  price: number;
}

export interface SaleRecord {
  date: string;
  type: CalculatorType;
  category: number;
  quantity: number;
  total: number;
  shopName?: string;
}

export interface ShopTransaction {
  id: string;
  date: string;
  type: 'sale' | 'payment';
  amount: number;
  notes: string;
}

export interface ShopAccount {
  id: string;
  name: string;
  totalSales: number;
  totalPayments: number;
  currentBalance: number; // outstanding amount they owe (totalSales - totalPayments)
  createdAt: string;
  transactions: ShopTransaction[];
}

export interface DailySummary {
  [key: string]: {
    [CalculatorType.REGULAR]: { [category: number]: number };
    [CalculatorType.PRO]: { [category: number]: number };
    totalAmount: number;
  }
}
