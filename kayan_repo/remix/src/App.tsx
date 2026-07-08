import React, { useState, useEffect, useMemo, ReactNode, useRef, ChangeEvent } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { 
  Calculator, 
  Zap, 
  Info, 
  History, 
  Moon, 
  Sun, 
  Plus, 
  Minus, 
  CheckCircle2,
  Phone,
  Settings2,
  Trash2,
  ArrowRightLeft,
  Download,
  BarChart3,
  TrendingUp,
  CalendarDays,
  Volume2,
  VolumeX,
  Share2,
  FileText,
  Check,
  RotateCcw,
  Upload,
  DownloadCloud,
  FileImage,
  Sparkles,
  Printer
} from 'lucide-react';
import { 
  BarChart, 
  Bar, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  ResponsiveContainer, 
  LineChart, 
  Line,
  Cell,
  Legend
} from 'recharts';
import { CalculatorType, CardCategory, SaleRecord, DailySummary, ShopAccount, ShopTransaction } from './types';
import { INITIAL_PRICES } from './constants';
import { 
  playSuccessSound, 
  downloadInvoicePDF, 
  downloadInvoicePNG, 
  downloadDailyReportPDF, 
  handleWhatsAppSingleShare, 
  handleSMSSingleShare, 
  handleWhatsAppReportShare, 
  handleSMSReportShare,
  downloadShopStatementPDF
} from './utils/invoiceHelpers';

// --- Sub Components ---

const Header = ({ title, toggleDark, isDark }: { title: string; toggleDark: () => void; isDark: boolean }) => (
  <header className="sticky top-0 z-30 w-full bg-white/80 dark:bg-slate-950/80 backdrop-blur-md border-b border-slate-100 dark:border-slate-800 px-4 py-4 flex items-center justify-between transition-colors">
    <h1 className="text-xl font-black text-slate-950 dark:text-white font-sans">{title}</h1>
    <button 
      onClick={toggleDark}
      className="p-2.5 rounded-full bg-slate-100 dark:bg-slate-900 border border-slate-200/50 dark:border-slate-800 text-slate-700 dark:text-slate-300 active:scale-90 transition-all cursor-pointer hover:bg-slate-200 dark:hover:bg-slate-800/80"
    >
      {isDark ? <Sun size={19} /> : <Moon size={19} />}
    </button>
  </header>
);

const CardItem = ({ 
  category, 
  quantity, 
  updateQuantity 
}: { 
  category: CardCategory; 
  quantity: number; 
  updateQuantity: (id: number, delta: number) => void;
  key?: number;
}) => (
  <div className="flex items-center justify-between p-4 bg-white dark:bg-slate-900 rounded-2xl border border-slate-100 dark:border-slate-800 shadow-sm mb-3">
    <div className="flex flex-col">
      <span className="font-extrabold text-slate-950 dark:text-white text-md">{category.label}</span>
      <span className="text-xs text-slate-500 dark:text-slate-400 font-bold mt-0.5">السعر: {category.price} ريال</span>
    </div>
    <div className="flex items-center gap-3">
      <button 
        onClick={() => updateQuantity(category.id, -1)}
        className="w-10 h-10 flex items-center justify-center rounded-xl bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-200 active:scale-90 cursor-pointer hover:bg-red-50 dark:hover:bg-red-950/20 active:text-red-500 transition-colors"
      >
        <Minus size={18} />
      </button>
      <input 
        type="number"
        value={quantity === 0 ? '' : quantity}
        onChange={(e) => {
          const val = parseInt(e.target.value) || 0;
          updateQuantity(category.id, val - quantity);
        }}
        placeholder="0"
        className="w-12 text-center text-lg font-black bg-transparent outline-none dark:text-white"
      />
      <button 
        onClick={() => updateQuantity(category.id, 1)}
        className="w-10 h-10 flex items-center justify-center rounded-xl bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-200 active:scale-90 cursor-pointer hover:bg-green-50 dark:hover:bg-green-950/20 active:text-green-500 transition-colors"
      >
        <Plus size={18} />
      </button>
    </div>
  </div>
);

const AboutPage = () => (
  <div className="p-6 flex flex-col items-center text-center space-y-6">
    <div className="w-24 h-24 bg-blue-500/10 dark:bg-blue-500/20 rounded-3xl flex items-center justify-center text-blue-500 shadow-inner">
      <Sparkles size={52} strokeWidth={1.5} />
    </div>
    <div className="space-y-1">
      <h2 className="text-2xl font-black text-slate-950 dark:text-white">تطبيق شبكة الدحشة</h2>
      <p className="text-sm font-bold text-slate-400">حساب مبيعات الكروت وتنظيم الأرباح المعتمد</p>
      <p className="text-xs text-slate-500">الإصدار 1.2.0 • 2026</p>
    </div>
    
    <div className="w-full bg-white dark:bg-slate-900 p-6 rounded-3xl border border-slate-100 dark:border-slate-800 space-y-4">
      <div className="flex flex-col items-center gap-1">
        <span className="text-sm text-slate-400 font-bold">برمجةوتطوير</span>
        <span className="text-lg font-black text-slate-900 dark:text-white">أحمد المنتصر</span>
      </div>
      <div className="h-px bg-slate-100 dark:bg-slate-800 w-full"></div>
      <a 
        href="tel:773086403"
        className="flex items-center justify-center gap-3 w-full p-4 bg-green-50 dark:bg-green-950/20 text-green-600 dark:text-green-400 rounded-2xl active:scale-95 transition-transform font-bold"
      >
        <Phone size={20} />
        <span className="font-sans font-black">773086403</span>
      </a>
    </div>
  </div>
);

const ShopsPage = ({
  shops,
  setShops,
  showToast
}: {
  shops: ShopAccount[];
  setShops: React.Dispatch<React.SetStateAction<ShopAccount[]>>;
  showToast: (m: string, t?: 'success' | 'error' | 'info') => void;
}) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [newShopName, setNewShopName] = useState('');
  const [selectedShopId, setSelectedShopId] = useState<string | null>(null);
  
  const [paymentAmount, setPaymentAmount] = useState<number | string>('');
  const [paymentNotes, setPaymentNotes] = useState('');
  const [showPaymentModal, setShowPaymentModal] = useState(false);

  const handleOpenOrCreate = () => {
    const name = newShopName.trim();
    if (!name) {
      showToast('يرجى كتابة اسم البقالة أولاً!', 'error');
      return;
    }

    const existing = shops.find(s => s.name.toLowerCase() === name.toLowerCase());
    if (existing) {
      setSelectedShopId(existing.id);
      setNewShopName('');
      showToast(`تم فتح حساب بقالة ${existing.name} بنجاح`, 'success');
    } else {
      const today = new Date().toISOString().split('T')[0];
      const newAcc: ShopAccount = {
        id: 'ACC-' + Math.floor(1000 + Math.random() * 9000),
        name,
        totalSales: 0,
        totalPayments: 0,
        currentBalance: 0,
        createdAt: today,
        transactions: []
      };
      setShops(prev => [...prev, newAcc]);
      setSelectedShopId(newAcc.id);
      setNewShopName('');
      showToast(`تم إنشاء حساب جديد بنجاح لبقالة ${name}`, 'success');
    }
  };

  const handleRecordPayment = () => {
    const amount = Number(paymentAmount);
    if (!amount || amount <= 0) {
      showToast('يرجى إدخال مبلغ صحيح أكبر من الصفر', 'error');
      return;
    }

    if (!selectedShopId) return;

    setShops(prev => {
      return prev.map(s => {
        if (s.id === selectedShopId) {
          const today = new Date().toISOString().split('T')[0];
          const newTx: ShopTransaction = {
            id: Math.random().toString(36).substring(2, 9),
            date: today,
            type: 'payment',
            amount: amount,
            notes: paymentNotes.trim() || 'سداد نقدي مستلم من العميل'
          };
          const updatedPayments = s.totalPayments + amount;
          return {
            ...s,
            totalPayments: updatedPayments,
            currentBalance: s.totalSales - updatedPayments,
            transactions: [...(s.transactions || []), newTx]
          };
        }
        return s;
      });
    });

    setPaymentAmount('');
    setPaymentNotes('');
    setShowPaymentModal(false);
    showToast('تم تسجيل الدفعة المسددة بنجاح وتحديث الحساب', 'success');
  };

  const handleDeleteShop = (id: string, name: string) => {
    if (confirm(`هل أنت متأكد من حذف حساب "${name}" بالكامل؟ سيتم مسح المعاملات المسجلة له.`)) {
      setShops(prev => prev.filter(s => s.id !== id));
      if (selectedShopId === id) setSelectedShopId(null);
      showToast(`تم حذف حساب ${name} بنجاح`, 'info');
    }
  };

  const filteredShops = useMemo(() => {
    const q = searchQuery.trim().toLowerCase();
    if (!q) return shops;
    return shops.filter(s => s.name.toLowerCase().includes(q));
  }, [shops, searchQuery]);

  const selectedShop = useMemo(() => {
    return shops.find(s => s.id === selectedShopId) || null;
  }, [shops, selectedShopId]);

  return (
    <div className="p-4 space-y-5">
      <div className="bg-white dark:bg-slate-900 p-5 rounded-3xl border border-slate-100 dark:border-slate-800 shadow-sm space-y-4">
        <div>
          <label className="text-xs font-extrabold text-slate-400 dark:text-slate-500 block mb-1.5 font-sans">إدخال اسم بقالة جديدة أو موجودة لتصفح حسابها</label>
          <div className="flex gap-2">
            <input 
              type="text"
              value={newShopName}
              onChange={(e) => setNewShopName(e.target.value)}
              placeholder="مثال: بقالة البركة السعيدة"
              className="flex-1 px-4 py-3 bg-slate-50 dark:bg-slate-950 text-slate-950 dark:text-white border border-slate-200 dark:border-slate-800 rounded-2xl text-sm font-bold outline-none focus:border-blue-500 transition-colors text-right"
            />
            <button
              onClick={handleOpenOrCreate}
              className="px-5 py-3 bg-blue-600 hover:bg-blue-700 text-white font-extrabold rounded-2xl text-xs flex items-center gap-1.5 cursor-pointer active:scale-95 transition-all shadow-md"
            >
              <span>فتح / إنشاء</span>
            </button>
          </div>
        </div>
      </div>

      {shops.length > 0 && (
        <div className="relative">
          <input 
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="🔍 بحث سريع عن حساب بقالة بالاسم..."
            className="w-full px-5 py-3.5 bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-800 rounded-2xl text-sm font-bold outline-none text-slate-950 dark:text-white placeholder:text-slate-400 focus:border-slate-300 transition-colors text-right"
          />
        </div>
      )}

      <AnimatePresence mode="wait">
        {selectedShop ? (
          <motion.div
            key={selectedShop.id}
            initial={{ opacity: 0, y: 15 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -15 }}
            className="bg-white dark:bg-slate-900 p-5 rounded-3xl border border-slate-100 dark:border-slate-800 shadow-sm space-y-5"
          >
            <div className="flex items-center justify-between border-b border-slate-100 dark:border-slate-800 pb-4">
              <div className="space-y-0.5 text-right w-full">
                <span className="text-[10px] text-slate-400 font-extrabold uppercase">البصمة المالية للبقالة</span>
                <h3 className="text-lg font-black text-slate-900 dark:text-white">{selectedShop.name}</h3>
                <span className="text-xs text-slate-500 block">رقم الحساب: {selectedShop.id}</span>
              </div>
              <button 
                onClick={() => setSelectedShopId(null)}
                className="px-3 py-1.5 bg-slate-100 dark:bg-slate-800 text-slate-500 hover:text-slate-700 dark:text-slate-400 rounded-xl text-xs font-bold transition-colors cursor-pointer shrink-0"
              >
                رجوع للقائمة
              </button>
            </div>

            <div className="grid grid-cols-3 gap-2 text-right">
              <div className="bg-slate-50 dark:bg-slate-950 p-3 rounded-2xl flex flex-col items-center justify-center text-center">
                <span className="text-[10px] text-slate-400 font-extrabold mb-1">المبيعات</span>
                <span className="text-sm font-black text-slate-900 dark:text-white font-sans">{selectedShop.totalSales}</span>
                <span className="text-[9px] text-slate-400 mt-0.5">ريال</span>
              </div>
              <div className="bg-slate-50 dark:bg-slate-950 p-3 rounded-2xl flex flex-col items-center justify-center text-center">
                <span className="text-[10px] text-slate-400 font-extrabold mb-1">المدفوعات</span>
                <span className="text-sm font-black text-slate-900 dark:text-white font-sans">{selectedShop.totalPayments}</span>
                <span className="text-[9px] text-emerald-500 font-bold mt-0.5">تم السداد</span>
              </div>
              <div className="bg-red-50/50 dark:bg-red-950/10 p-3 rounded-2xl flex flex-col items-center justify-center text-center border border-red-100/50 dark:border-red-950/20">
                <span className="text-[10px] text-red-500/80 font-extrabold mb-1">الرصيد المتبقي</span>
                <span className="text-sm font-black text-red-600 dark:text-red-400 font-sans">{selectedShop.currentBalance}</span>
                <span className="text-[9px] text-red-500/80 mt-0.5 font-bold uppercase">مستحق</span>
              </div>
            </div>

            <div className="flex gap-2.5 pt-2">
              <button
                onClick={() => downloadShopStatementPDF(selectedShop)}
                className="flex-1 py-3 bg-red-600 hover:bg-red-700 text-white text-xs font-black rounded-2xl flex items-center justify-center gap-1.5 cursor-pointer active:scale-95 transition-all shadow-md shadow-red-500/10"
              >
                <FileText size={15} />
                <span>تحميل كشف PDF</span>
              </button>
              <button
                onClick={() => setShowPaymentModal(true)}
                className="flex-1 py-3 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-black rounded-2xl flex items-center justify-center gap-1.5 cursor-pointer active:scale-95 transition-all shadow-md shadow-emerald-500/10"
              >
                <ArrowRightLeft size={15} />
                <span>تسجيل دفعة مستلمة</span>
              </button>
            </div>

            <div className="space-y-3 pt-3 border-t border-slate-100 dark:border-slate-800 text-right">
              <h4 className="text-xs font-extrabold text-slate-400 dark:text-slate-500">حركة وسجل العمليات الحسابية التفصيلية</h4>
              
              <div className="space-y-2 max-h-64 overflow-y-auto pr-1">
                {(selectedShop.transactions || []).length === 0 ? (
                  <div className="text-center py-6 text-slate-400 text-xs font-bold bg-slate-50/50 dark:bg-slate-950/50 rounded-2xl border border-dashed border-slate-100 dark:border-slate-800">
                    لم تسجل أي عمليات أو دفعات بعد لهذا الحساب.
                  </div>
                ) : (
                  [...selectedShop.transactions].reverse().map((tx) => (
                    <div 
                      key={tx.id} 
                      className="p-3 bg-slate-50 dark:bg-slate-950 rounded-2xl flex justify-between items-center text-xs"
                    >
                      <span className={`text-md font-sans font-black ${tx.type === 'sale' ? 'text-red-600 dark:text-red-400' : 'text-emerald-600 dark:text-emerald-400'}`}>
                        {tx.type === 'sale' ? '+' : '-'}{tx.amount} ريال
                      </span>
                      <div className="space-y-1 text-right">
                        <div className="flex items-center justify-end gap-2">
                          <span className="text-[10px] text-slate-400 font-bold font-sans">{tx.date}</span>
                          <span className={`px-2 py-0.5 rounded-full font-black text-[9px] ${tx.type === 'sale' ? 'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400' : 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400'}`}>
                            {tx.type === 'sale' ? 'فاتورة بيع' : 'دفعة مسددة'}
                          </span>
                        </div>
                        <p className="font-extrabold text-slate-600 dark:text-slate-400 max-w-[190px] truncate">{tx.notes}</p>
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>
          </motion.div>
        ) : (
          <motion.div
            key="list"
            initial={{ opacity: 0, y: 15 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -15 }}
            className="space-y-3"
          >
            <div className="flex items-center justify-between px-1">
              <h3 className="text-xs font-extrabold text-slate-400 dark:text-slate-500 select-none w-full text-right">قائمة حسابات البقالات والعملاء ({filteredShops.length})</h3>
            </div>

            {filteredShops.length === 0 ? (
              <div className="text-center py-12 bg-white dark:bg-slate-900 rounded-3xl border border-slate-100 dark:border-slate-800 shadow-sm p-6 space-y-2">
                <p className="text-slate-400 text-sm font-bold">لا توجد حسابات مسجلة حالياً تطابق البحث.</p>
                <p className="text-slate-500 text-xs">اكتب اسم البقالة في الحقل للتصفح والفتح التلقائي.</p>
              </div>
            ) : (
              <div className="grid grid-cols-1 gap-3">
                {filteredShops.map(shop => (
                  <div 
                    key={shop.id}
                    className="p-4 bg-white dark:bg-slate-900 rounded-3xl border border-slate-100 dark:border-slate-800 shadow-sm flex items-center justify-between"
                  >
                    <button
                      onClick={() => handleDeleteShop(shop.id, shop.name)}
                      className="p-2.5 bg-red-50 hover:bg-red-100 dark:bg-red-950/20 dark:hover:bg-red-950/40 text-red-500 rounded-xl transition-all cursor-pointer active:scale-90"
                      title="حذف الحساب"
                    >
                      <Trash2 size={15} />
                    </button>

                    <div 
                      onClick={() => setSelectedShopId(shop.id)}
                      className="flex-1 pr-4 text-right cursor-pointer"
                    >
                      <h4 className="text-sm font-black text-slate-950 dark:text-white hover:text-blue-500 dark:hover:text-blue-400 transition-colors">{shop.name}</h4>
                      <div className="flex justify-end gap-3 text-[10px] text-slate-400 font-bold font-sans mt-0.5">
                        <span>الرصيد: {shop.currentBalance} ريال</span>
                        <span>•</span>
                        <span>مبيعات: {shop.totalSales}</span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </motion.div>
        )}
      </AnimatePresence>

      <AnimatePresence>
        {showPaymentModal && selectedShop && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
            <motion.div 
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setShowPaymentModal(false)}
              className="absolute inset-0 bg-slate-950/60 backdrop-blur-sm"
            />
            
            <motion.div 
              initial={{ opacity: 0, scale: 0.95, y: 15 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.95, y: 15 }}
              className="relative w-full max-w-sm bg-white dark:bg-slate-900 rounded-3xl border border-slate-100 dark:border-slate-800 shadow-2xl p-6 overflow-hidden space-y-4"
            >
              <div className="text-right">
                <h3 className="text-lg font-black text-slate-950 dark:text-white">تسجيل دفعة مسددة</h3>
                <p className="text-xs text-slate-400 mt-1">تنزيل الرصيد المستحق للبقالة: {selectedShop.name}</p>
              </div>

              <div className="space-y-3 pt-2 text-right">
                <div>
                  <label className="text-[11px] font-extrabold text-slate-400 block mb-1">قيمة المبلغ المسدد (ريال)</label>
                  <input 
                    type="number"
                    value={paymentAmount}
                    onChange={(e) => setPaymentAmount(e.target.value)}
                    placeholder="مثال: 5000"
                    className="w-full px-4 py-3 bg-slate-50 dark:bg-slate-950 text-slate-950 dark:text-white border border-slate-200 dark:border-slate-800 rounded-xl text-sm font-sans font-black outline-none focus:border-emerald-500 transition-colors text-right"
                  />
                </div>
                <div>
                  <label className="text-[11px] font-extrabold text-slate-400 block mb-1">تفاصيل أو ملاحظة (اختياري)</label>
                  <input 
                    type="text"
                    value={paymentNotes}
                    onChange={(e) => setPaymentNotes(e.target.value)}
                    placeholder="مثال: سداد نقدي مستلم كاش"
                    className="w-full px-4 py-3 bg-slate-50 dark:bg-slate-950 text-slate-950 dark:text-white border border-slate-200 dark:border-slate-800 rounded-xl text-sm font-bold outline-none focus:border-emerald-500 transition-colors text-right"
                  />
                </div>
              </div>

              <div className="flex gap-2.5 pt-4">
                <button
                  onClick={handleRecordPayment}
                  className="flex-1 py-3 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-black rounded-xl cursor-pointer active:scale-95 transition-all shadow-md shadow-emerald-500/10"
                >
                  حفظ الدفعة
                </button>
                <button
                  onClick={() => setShowPaymentModal(false)}
                  className="flex-1 py-3 bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400 text-xs font-black rounded-xl cursor-pointer hover:bg-slate-200"
                >
                  إلغاء
                </button>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </div>
  );
};

// --- Main App ---

export default function App() {
  const [activeTab, setActiveTab] = useState<'regular' | 'pro' | 'shops' | 'about'>('regular');
  const [isDark, setIsDark] = useState(() => {
    if (typeof window !== 'undefined') {
      return localStorage.getItem('theme') === 'dark' || 
        (!localStorage.getItem('theme') && window.matchMedia('(prefers-color-scheme: dark)').matches);
    }
    return false;
  });
  
  // Custom prices storage
  const [prices, setPrices] = useState<Record<CalculatorType, CardCategory[]>>(() => {
    const saved = localStorage.getItem('custom_prices');
    return saved ? JSON.parse(saved) : INITIAL_PRICES;
  });

  const [quantities, setQuantities] = useState<Record<number, number>>({
    100: 0, 200: 0, 250: 0, 300: 0, 500: 0
  });

  const [pricesEditType, setPricesEditType] = useState<CalculatorType>(CalculatorType.REGULAR);
  const [receivedAmount, setReceivedAmount] = useState<number | string>('');
  const [shopName, setShopName] = useState<string>(() => {
    if (typeof window !== 'undefined') {
      return localStorage.getItem('last_shop_name') || '';
    }
    return '';
  });
  const [showReport, setShowReport] = useState(false);
  const [showSettingsModal, setShowSettingsModal] = useState(false);

  // Success Overlay / Invoice sharing state
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [lastSaleSummary, setLastSaleSummary] = useState<{
    items: { label: string; category: number; quantity: number; price: number; total: number }[];
    totalAmount: number;
    receivedAmount: number;
    remainingAmount: number;
    type: CalculatorType;
    date: string;
    shopName?: string;
  } | null>(null);

  // Toast banner state
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' | 'info' } | null>(null);

  // Sound Config
  const [isSoundEnabled, setIsSoundEnabled] = useState<boolean>(() => {
    if (typeof window !== 'undefined') {
      const saved = localStorage.getItem('sound_enabled');
      return saved === null ? true : saved === 'true';
    }
    return true;
  });

  // Thermal Print Mode Config
  const [isThermalMode, setIsThermalMode] = useState<boolean>(() => {
    if (typeof window !== 'undefined') {
      const saved = localStorage.getItem('thermal_mode_enabled');
      return saved === 'true';
    }
    return false;
  });

  // Backup Element reference
  const fileInputRef = useRef<HTMLInputElement>(null);

  // Persistence for Reports
  const [salesHistory, setSalesHistory] = useState<SaleRecord[]>(() => {
    const saved = localStorage.getItem('sales_history');
    return saved ? JSON.parse(saved) : [];
  });

  // Shops Database State
  const [shops, setShops] = useState<ShopAccount[]>(() => {
    if (typeof window !== 'undefined') {
      const saved = localStorage.getItem('grocery_accounts');
      return saved ? JSON.parse(saved) : [];
    }
    return [];
  });

  useEffect(() => {
    document.documentElement.classList.toggle('dark', isDark);
    localStorage.setItem('theme', isDark ? 'dark' : 'light');
  }, [isDark]);

  useEffect(() => {
    localStorage.setItem('sales_history', JSON.stringify(salesHistory));
  }, [salesHistory]);

  useEffect(() => {
    localStorage.setItem('grocery_accounts', JSON.stringify(shops));
  }, [shops]);

  useEffect(() => {
    localStorage.setItem('custom_prices', JSON.stringify(prices));
  }, [prices]);

  useEffect(() => {
    localStorage.setItem('sound_enabled', String(isSoundEnabled));
  }, [isSoundEnabled]);

  useEffect(() => {
    localStorage.setItem('thermal_mode_enabled', String(isThermalMode));
  }, [isThermalMode]);

  const currentType = activeTab === 'pro' ? CalculatorType.PRO : CalculatorType.REGULAR;
  const currentPrices = prices[currentType];

  const totalAmount = useMemo(() => {
    return currentPrices.reduce((sum, cat) => sum + (quantities[cat.id] || 0) * cat.price, 0);
  }, [quantities, currentPrices]);

  const remainingAmount = useMemo(() => {
    const received = Number(receivedAmount) || 0;
    return received > 0 ? (received - totalAmount) : 0;
  }, [receivedAmount, totalAmount]);

  const isConfirmEnabled = totalAmount > 0 && Number(receivedAmount) >= totalAmount;

  const showToast = (message: string, type: 'success' | 'error' | 'info' = 'success') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 3000);
  };

  const updateQuantity = (id: number, delta: number) => {
    setQuantities(prev => ({
      ...prev,
      [id]: Math.max(0, (prev[id] || 0) + delta)
    }));
  };

  const handleConfirm = () => {
    if (!isConfirmEnabled) return;

    const today = new Date().toISOString().split('T')[0];
    
    // Process items elements
    const soldItems = currentPrices
      .filter(p => (quantities[p.id] || 0) > 0)
      .map(p => ({
        label: p.label,
        category: p.id,
        quantity: quantities[p.id],
        price: p.price,
        total: (quantities[p.id] || 0) * p.price
      }));

    const activeShop = shopName.trim();

    const newRecords: SaleRecord[] = soldItems.map(item => ({
      date: today,
      type: currentType,
      category: item.category,
      quantity: item.quantity,
      total: item.total,
      shopName: activeShop || 'بقالة عامة'
    }));

    setSalesHistory(prev => [...prev, ...newRecords]);

    if (activeShop) {
      localStorage.setItem('last_shop_name', activeShop);
      
      // Update or create grocery account
      setShops(prev => {
        const existingIdx = prev.findIndex(s => s.name.toLowerCase() === activeShop.toLowerCase());
        const timestamp = new Date().toISOString().split('T')[0];
        const newTx: ShopTransaction = {
          id: Math.random().toString(36).substring(2, 9),
          date: timestamp,
          type: 'sale',
          amount: totalAmount,
          notes: `فاتورة كروت مبيعات بقيمة ${totalAmount} ريال`
        };
        
        if (existingIdx > -1) {
          const updated = [...prev];
          const oldAccount = updated[existingIdx];
          const updatedTx = [...(oldAccount.transactions || []), newTx];
          const updatedSales = oldAccount.totalSales + totalAmount;
          const updatedPayments = oldAccount.totalPayments;
          
          updated[existingIdx] = {
            ...oldAccount,
            totalSales: updatedSales,
            currentBalance: updatedSales - updatedPayments,
            transactions: updatedTx
          };
          return updated;
        } else {
          const newAccount: ShopAccount = {
            id: 'ACC-' + Math.floor(1000 + Math.random() * 9000),
            name: activeShop,
            totalSales: totalAmount,
            totalPayments: 0,
            currentBalance: totalAmount,
            createdAt: timestamp,
            transactions: [newTx]
          };
          return [...prev, newAccount];
        }
      });
    }

    // Construct invoice summary state
    const summary = {
      items: soldItems,
      totalAmount,
      receivedAmount: Number(receivedAmount),
      remainingAmount,
      type: currentType,
      date: today,
      shopName: activeShop || 'بقالة عامة'
    };

    setLastSaleSummary(summary);
    setShowSuccessModal(true);

    // Audio beep confirmation
    playSuccessSound(isSoundEnabled);

    // Reset quantities
    setQuantities({ 100: 0, 200: 0, 250: 0, 300: 0, 500: 0 });
    setReceivedAmount('');
  };

  const handlePriceChange = (id: number, newPrice: number) => {
    setPrices(prev => ({
      ...prev,
      [pricesEditType]: prev[pricesEditType].map(p => p.id === id ? { ...p, price: newPrice } : p)
    }));
  };

  const handleResetPrices = () => {
    if (confirm('هل أنت متأكد من إعادة تعيين جميع الأسعار إلى الافتراضية؟')) {
      setPrices(INITIAL_PRICES);
      showToast('تمت إعادة تعيين الأسعار للافتراضية', 'info');
    }
  };

  const clearHistory = () => {
    if (confirm('هل أنت متأكد من مسح جميع بيانات وسجل المبيعات نهائياً؟')) {
      setSalesHistory([]);
      showToast('تم تهيئة ومسح سجل المبيعات', 'info');
    }
  };

  const handleBackupExport = () => {
    const backupObj = {
      salesHistory,
      shops,
      prices,
      isSoundEnabled,
      version: '1.2.0',
      exportDate: new Date().toISOString()
    };
    
    const dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(backupObj, null, 2));
    const link = document.createElement('a');
    link.setAttribute("href", dataStr);
    link.setAttribute("download", `dahsha_backup_${new Date().toISOString().split('T')[0]}.json`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    showToast('تم تصدير ملف النسخة الاحتياطية بنجاح!', 'success');
  };

  const handleBackupImport = (event: ChangeEvent<HTMLInputElement>) => {
    const fileReader = new FileReader();
    if (event.target.files && event.target.files[0]) {
      fileReader.readAsText(event.target.files[0], "UTF-8");
      fileReader.onload = (e) => {
        try {
          const target = e.target;
          if (!target || !target.result) return;
          const parsed = JSON.parse(target.result as string);
          
          if (parsed.salesHistory && Array.isArray(parsed.salesHistory)) {
            setSalesHistory(parsed.salesHistory);
            if (parsed.shops && Array.isArray(parsed.shops)) {
              setShops(parsed.shops);
            }
            if (parsed.prices) {
              setPrices(parsed.prices);
            }
            if (parsed.isSoundEnabled !== undefined) {
              setIsSoundEnabled(parsed.isSoundEnabled);
            }
            showToast('تم استيراد واستعادة النسخة الاحتياطية بنجاح!', 'success');
            setShowSettingsModal(false);
          } else {
            showToast('ملف النسخ الاحتياطي غير صالح!', 'error');
          }
        } catch (err) {
          showToast('حدث خطأ أثناء قراءة وفحص ملف البيانات.', 'error');
        }
      };
    }
  };

  const dailyReport = useMemo(() => {
    const today = new Date().toISOString().split('T')[0];
    const todaySales = salesHistory.filter(s => s.date === today);
    
    const summary = {
      [CalculatorType.REGULAR]: { 
        100: { quantity: 0, amount: 0 }, 
        200: { quantity: 0, amount: 0 }, 
        250: { quantity: 0, amount: 0 }, 
        300: { quantity: 0, amount: 0 }, 
        500: { quantity: 0, amount: 0 } 
      },
      [CalculatorType.PRO]: { 
        100: { quantity: 0, amount: 0 }, 
        200: { quantity: 0, amount: 0 }, 
        250: { quantity: 0, amount: 0 }, 
        300: { quantity: 0, amount: 0 }, 
        500: { quantity: 0, amount: 0 } 
      },
      totalAmount: 0
    };

    todaySales.forEach(s => {
      // @ts-ignore
      if (summary[s.type][s.category]) {
        // @ts-ignore
        summary[s.type][s.category].quantity += s.quantity;
        // @ts-ignore
        summary[s.type][s.category].amount += s.total;
      }
      summary.totalAmount += s.total;
    });

    return summary;
  }, [salesHistory]);

  const [trendView, setTrendView] = useState<'weekly' | 'monthly'>('weekly');

  const trends = useMemo(() => {
    const dates = [...new Set(salesHistory.map(s => s.date))].sort() as string[];
    const last7Days = dates.slice(-7);
    const last30Days = dates.slice(-30);

    const getTrendData = (targetDates: string[]) => {
      return targetDates.map(date => {
        const daySales = salesHistory.filter(s => s.date === date);
        const regularTotal = daySales.filter(s => s.type === CalculatorType.REGULAR).reduce((sum, s) => sum + s.total, 0);
        const proTotal = daySales.filter(s => s.type === CalculatorType.PRO).reduce((sum, s) => sum + s.total, 0);
        return {
          date: date.split('-').slice(1).join('/'), // MM/DD
          العادية: regularTotal,
          Pro: proTotal,
          إجمالي: regularTotal + proTotal
        };
      });
    };

    return {
      weekly: getTrendData(last7Days),
      monthly: getTrendData(last30Days)
    };
  }, [salesHistory]);

  const categoryData = useMemo(() => {
    const today = new Date().toISOString().split('T')[0];
    const todaySales = salesHistory.filter(s => s.date === today);
    
    return [
      { name: '100', العادية: todaySales.find(s => s.type === CalculatorType.REGULAR && s.category === 100)?.quantity || 0, PRO: todaySales.find(s => s.type === CalculatorType.PRO && s.category === 100)?.quantity || 0 },
      { name: '200', العادية: todaySales.find(s => s.type === CalculatorType.REGULAR && s.category === 200)?.quantity || 0, PRO: todaySales.find(s => s.type === CalculatorType.PRO && s.category === 200)?.quantity || 0 },
      { name: '250', العادية: todaySales.find(s => s.type === CalculatorType.REGULAR && s.category === 250)?.quantity || 0, PRO: todaySales.find(s => s.type === CalculatorType.PRO && s.category === 250)?.quantity || 0 },
      { name: '300', العادية: todaySales.find(s => s.type === CalculatorType.REGULAR && s.category === 300)?.quantity || 0, PRO: todaySales.find(s => s.type === CalculatorType.PRO && s.category === 300)?.quantity || 0 },
      { name: '500', العادية: todaySales.find(s => s.type === CalculatorType.REGULAR && s.category === 500)?.quantity || 0, PRO: todaySales.find(s => s.type === CalculatorType.PRO && s.category === 500)?.quantity || 0 },
    ];
  }, [salesHistory]);

  return (
    <div className="min-h-screen pb-24 bg-slate-50 dark:bg-slate-950 transition-colors">
      <Header 
        title={
          activeTab === 'regular' 
            ? 'حاسبة مبيعات الكروت' 
            : activeTab === 'pro' 
            ? 'حاسبة فئة برو Pro' 
            : activeTab === 'shops'
            ? 'حسابات البقالات والعملاء'
            : 'حول التطبيق'
        } 
        toggleDark={() => setIsDark(!isDark)}
        isDark={isDark}
      />

      <main className="max-w-md mx-auto">
        <AnimatePresence mode="wait">
          {activeTab === 'about' ? (
            <motion.div
              key="about"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -20 }}
            >
              <AboutPage />
            </motion.div>
          ) : activeTab === 'shops' ? (
            <motion.div
              key="shops"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -20 }}
            >
              <ShopsPage 
                shops={shops}
                setShops={setShops}
                showToast={showToast}
              />
            </motion.div>
          ) : (
            <motion.div
              key={activeTab}
              initial={{ opacity: 0, x: activeTab === 'pro' ? -50 : 50 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: activeTab === 'pro' ? 50 : -50 }}
              className="p-4"
            >
              {/* Toolbar */}
              <div className="flex gap-2.5 mb-4">
                <button 
                  onClick={() => setShowReport(true)}
                  className="flex-1 py-3 px-4 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200/50 dark:border-slate-800 flex items-center justify-center gap-2 text-slate-800 dark:text-slate-200 font-extrabold active:scale-95 transition-all shadow-sm cursor-pointer hover:bg-slate-100 dark:hover:bg-slate-800"
                >
                  <History size={18} className="text-blue-500" />
                  <span>عرض التقرير المالي</span>
                </button>
                <button 
                  onClick={() => setShowSettingsModal(true)}
                  className="p-3.5 rounded-2xl border bg-white dark:bg-slate-900 border-slate-200/50 dark:border-slate-800 text-slate-800 dark:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800 flex items-center justify-center active:scale-90 transition-all cursor-pointer shadow-sm"
                  title="الترميز الإعدادات والنسخ"
                >
                  <Settings2 size={20} className="text-indigo-500" />
                </button>
              </div>

              {/* Card List of prices categories */}
              <div className="space-y-1">
                {currentPrices.map(cat => (
                  <CardItem 
                    key={cat.id} 
                    category={cat} 
                    quantity={quantities[cat.id] || 0} 
                    updateQuantity={updateQuantity} 
                  />
                ))}
              </div>

              {/* Calculation Summary Card */}
              <div className="mt-6 p-6 bg-blue-600 rounded-[2.5rem] text-white shadow-xl shadow-blue-500/20">
                <div className="flex justify-between items-center mb-6">
                  <span className="text-blue-100 text-md font-bold">المجموع الإجمالي</span>
                  <span className="text-3xl font-black font-sans">{totalAmount} <span className="text-sm font-normal">ريال</span></span>
                </div>
                
                <div className="space-y-4">
                  <div className="relative">
                    <span className="absolute right-4 top-3 text-[10px] text-blue-200 font-black">إسم البقالة / العميل (اختياري)</span>
                    <input 
                      type="text" 
                      value={shopName}
                      onChange={(e) => setShopName(e.target.value)}
                      placeholder="بقالة السعادة، المنتصر، النور..."
                      className="w-full pt-7 pb-3 pr-4 pl-4 bg-white/10 border border-white/20 rounded-2xl outline-none focus:bg-white/20 transition-colors placeholder:text-blue-200/50 font-bold text-md text-right"
                    />
                  </div>

                  <div className="relative">
                    <span className="absolute right-4 top-3 text-[10px] text-blue-200 font-black">المبلغ المقبوض للزبون</span>
                    <input 
                      type="number" 
                      value={receivedAmount}
                      onChange={(e) => setReceivedAmount(e.target.value)}
                      placeholder="رياض..."
                      className="w-full pt-7 pb-3 pr-4 pl-12 bg-white/10 border border-white/20 rounded-2xl outline-none focus:bg-white/20 transition-colors placeholder:text-blue-300 font-black text-lg font-sans"
                    />
                    <span className="absolute left-4 top-1/2 -translate-y-1/2 text-xs font-bold text-blue-200 mt-2">ريال</span>
                  </div>

                  <div className="flex justify-between items-center p-4 bg-white/10 rounded-2xl">
                    <span className="text-blue-100 text-sm font-bold">الفائض والمتبقي</span>
                    <span className="text-xl font-black font-sans">{remainingAmount} <span className="text-xs font-normal">ريال</span></span>
                  </div>

                  <button 
                    disabled={!isConfirmEnabled}
                    onClick={handleConfirm}
                    className={`w-full py-5 rounded-2xl font-black text-lg flex items-center justify-center gap-2 transition-all ${
                      isConfirmEnabled 
                      ? 'bg-white text-blue-600 shadow-lg shadow-white/10 active:scale-[0.98] cursor-pointer hover:bg-slate-100' 
                      : 'bg-white/20 text-white/40 cursor-not-allowed'
                    }`}
                  >
                    <CheckCircle2 size={24} />
                    <span>اعتماد البيع والمشاركة</span>
                  </button>
                </div>
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </main>

      {/* App Settings and Database Backup Modal */}
      <AnimatePresence>
        {showSettingsModal && (
          <div className="fixed inset-0 z-50 flex flex-col pt-10">
            <motion.div 
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setShowSettingsModal(false)}
              className="absolute inset-0 bg-slate-950/60 backdrop-blur-sm"
            />
            <motion.div 
              initial={{ y: '100%' }}
              animate={{ y: 0 }}
              exit={{ y: '100%' }}
              transition={{ type: 'spring', damping: 26, stiffness: 300 }}
              className="relative mt-auto w-full bg-white dark:bg-slate-950 rounded-t-[3rem] p-6 max-h-[85vh] overflow-y-auto border-t border-slate-200 dark:border-slate-800 shadow-2xl flex flex-col"
            >
              <div className="w-12 h-1.5 bg-slate-200 dark:bg-slate-800 rounded-full mx-auto mb-6 cursor-pointer" onClick={() => setShowSettingsModal(false)}></div>
              
              <div className="flex justify-between items-center mb-5">
                <h2 className="text-2xl font-black text-slate-900 dark:text-white flex items-center gap-2.5">
                  <Settings2 size={24} className="text-indigo-500" />
                  إعدادات التطبيق
                </h2>
                <button 
                  onClick={() => setShowSettingsModal(false)}
                  className="px-3.5 py-1.5 bg-slate-100 dark:bg-slate-900 text-slate-600 dark:text-slate-400 font-bold rounded-xl text-xs"
                >
                  إغلاق
                </button>
              </div>

              {/* Hidden file input for import */}
              <input 
                type="file" 
                ref={fileInputRef} 
                onChange={handleBackupImport} 
                accept=".json" 
                className="hidden" 
              />

              <div className="space-y-6 pb-6">
                
                {/* 1. Alert Chime Chaffer */}
                <div className="p-4 bg-slate-50 dark:bg-slate-900 rounded-2xl border border-slate-100 dark:border-slate-800 space-y-3">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2.5">
                      {isSoundEnabled ? <Volume2 className="text-green-500" size={20} /> : <VolumeX className="text-slate-400" size={20} />}
                      <div>
                        <h3 className="font-extrabold text-slate-900 dark:text-white text-sm">صوت التنبيه للبيع</h3>
                        <p className="text-xs text-slate-400 mt-0.5">تشغيل رنين كاش عند إتمام كل بيع ناجح</p>
                      </div>
                    </div>
                    <button 
                      onClick={() => {
                        const nextState = !isSoundEnabled;
                        setIsSoundEnabled(nextState);
                        playSuccessSound(nextState);
                        showToast(nextState ? 'تم تفعيل منبّه البيع 🔊' : 'تم كتم منبّه البيع 🔇', 'info');
                      }}
                      className={`relative w-11 h-6 rounded-full transition-colors flex items-center ${isSoundEnabled ? 'bg-green-500 justify-end' : 'bg-slate-200 dark:bg-slate-800 justify-start'}`}
                    >
                      <motion.div layout className="w-5 h-5 rounded-full bg-white mx-0.5 shadow-sm" />
                    </button>
                  </div>
                </div>

                {/* 2. Thermal Printer Toggle Switch */}
                <div className="p-4 bg-slate-50 dark:bg-slate-900 rounded-2xl border border-slate-100 dark:border-slate-800 space-y-3">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2.5">
                      <Printer className={isThermalMode ? "text-amber-500" : "text-slate-400"} size={20} />
                      <div>
                        <h3 className="font-extrabold text-slate-900 dark:text-white text-sm">وضع الطابعة الحرارية (أبيض وأسود)</h3>
                        <p className="text-xs text-slate-400 mt-0.5">تبديل نمط تصميم كشف الـ PDF لصورة حرارية عالية التباين وموفرة للحبر والورق</p>
                      </div>
                    </div>
                    <button 
                      onClick={() => {
                        const nextState = !isThermalMode;
                        setIsThermalMode(nextState);
                        showToast(nextState ? 'تم تفعيل وضع الطباعة الحرارية 🖨️' : 'تم العودة للطباعة الملونة الفاخرة 🎨', 'info');
                      }}
                      className={`relative w-11 h-6 rounded-full transition-colors flex items-center ${isThermalMode ? 'bg-amber-500 justify-end' : 'bg-slate-200 dark:bg-slate-800 justify-start'}`}
                    >
                      <motion.div layout className="w-5 h-5 rounded-full bg-white mx-0.5 shadow-sm" />
                    </button>
                  </div>
                </div>

                {/* 3. File Database backup actions */}
                <div className="p-4 bg-slate-50 dark:bg-slate-900 rounded-2xl border border-slate-100 dark:border-slate-800 space-y-3.5">
                  <h3 className="text-sm font-extrabold text-slate-900 dark:text-white flex items-center gap-2">
                    <DownloadCloud size={16} className="text-blue-500" />
                    النسخ الاحتياطي واستعادة البيانات
                  </h3>
                  <p className="text-xs text-slate-400 leading-relaxed">قم بحفظ سجل مبيعاتك وأسعارك كملف JSON محلي لتتمكن من استيراده بأي وقت دون فقد تراكماتك المالية.</p>
                  
                  <div className="grid grid-cols-2 gap-3.5 pt-1.5">
                    <button 
                      onClick={handleBackupExport}
                      className="py-3 px-3.5 rounded-xl border border-blue-200 dark:border-blue-900/30 bg-blue-50/50 dark:bg-blue-950/20 text-blue-600 dark:text-blue-400 hover:bg-blue-50 dark:hover:bg-blue-950/40 text-xs font-black flex items-center justify-center gap-2 cursor-pointer transition-colors"
                    >
                      <Download size={14} />
                      تصدير النسخة
                    </button>
                    <button 
                      onClick={() => fileInputRef.current?.click()}
                      className="py-3 px-3.5 rounded-xl border border-indigo-200 dark:border-indigo-900/30 bg-indigo-50/50 dark:bg-indigo-950/20 text-indigo-600 dark:text-indigo-400 hover:bg-indigo-50 dark:hover:bg-indigo-950/40 text-xs font-black flex items-center justify-center gap-2 cursor-pointer transition-colors"
                    >
                      <Upload size={14} />
                      استيراد نسخة
                    </button>
                  </div>
                </div>

                {/* 3. Pricing Tarif Tariffs list editor */}
                <div className="p-4 bg-slate-50 dark:bg-slate-900 rounded-2xl border border-slate-100 dark:border-slate-800 space-y-4">
                  <div className="flex justify-between items-center">
                    <h3 className="text-sm font-extrabold text-slate-900 dark:text-white flex items-center gap-2">
                      <Settings2 size={16} className="text-indigo-500" />
                      تعديل تسعيرة الفئات الحالية
                    </h3>
                    <button 
                      onClick={handleResetPrices}
                      className="text-[10px] text-red-500 font-extrabold active:scale-95"
                    >
                      إعادة افتراضي
                    </button>
                  </div>

                  {/* Switch active tariff prices edit list */}
                  <div className="flex bg-slate-200 dark:bg-slate-800 p-1 rounded-xl">
                    <button
                      onClick={() => setPricesEditType(CalculatorType.REGULAR)}
                      className={`flex-1 py-1.5 text-xs font-black rounded-lg transition-all ${pricesEditType === CalculatorType.REGULAR ? 'bg-white dark:bg-slate-700 text-blue-600 dark:text-white shadow-sm' : 'text-slate-400 dark:text-slate-500'}`}
                    >
                      دليل فئات عادية
                    </button>
                    <button
                      onClick={() => setPricesEditType(CalculatorType.PRO)}
                      className={`flex-1 py-1.5 text-xs font-black rounded-lg transition-all ${pricesEditType === CalculatorType.PRO ? 'bg-white dark:bg-slate-700 text-blue-600 dark:text-white shadow-sm' : 'text-slate-400 dark:text-slate-500'}`}
                    >
                      دليل فئات Pro
                    </button>
                  </div>

                  <div className="grid grid-cols-2 gap-3 pb-1">
                    {prices[pricesEditType].map(p => (
                      <div key={p.id} className="flex flex-col gap-1 p-2 bg-white dark:bg-slate-950 rounded-xl border border-slate-100 dark:border-slate-800">
                        <label className="text-[10px] font-black text-slate-400 pr-1">{p.label}</label>
                        <input 
                          type="number"
                          value={p.price}
                          onChange={(e) => handlePriceChange(p.id, parseInt(e.target.value) || 0)}
                          className="bg-transparent text-slate-900 dark:text-white font-extrabold text-sm px-1.5 focus:outline-none"
                        />
                      </div>
                    ))}
                  </div>
                </div>

                {/* 4. Flush and wipe database */}
                <div className="p-4 bg-red-500/5 dark:bg-red-500/10 rounded-2xl border border-red-500/10 space-y-3">
                  <div className="flex items-center justify-between">
                    <div>
                      <h3 className="font-extrabold text-red-600 dark:text-red-400 text-sm">تهيئة وقرغ البيانات</h3>
                      <p className="text-[10px] text-slate-400 mt-0.5">مسح جميع بيانات وسجلات المبيعات المسجلة نهائياً</p>
                    </div>
                    <button 
                      onClick={clearHistory}
                      className="p-2 bg-red-100 hover:bg-red-200 text-red-600 dark:bg-red-950/30 dark:text-red-400 rounded-xl text-xs font-black transition-colors"
                    >
                      تصفير السجل
                    </button>
                  </div>
                </div>

              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Success and Sharing Modal */}
      <AnimatePresence>
        {showSuccessModal && lastSaleSummary && (
          <div className="fixed inset-0 z-50 flex flex-col pt-10">
            <motion.div 
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setShowSuccessModal(false)}
              className="absolute inset-0 bg-slate-950/70 backdrop-blur-sm"
            />
            <motion.div 
              initial={{ y: '100%' }}
              animate={{ y: 0 }}
              exit={{ y: '100%' }}
              transition={{ type: 'spring', damping: 26, stiffness: 300 }}
              className="relative mt-auto w-full bg-white dark:bg-slate-950 rounded-t-[3rem] p-6 max-h-[90vh] overflow-y-auto border-t border-slate-200 dark:border-slate-800 shadow-2xl flex flex-col"
            >
              <div className="w-12 h-1.5 bg-slate-200 dark:bg-slate-800 rounded-full mx-auto mb-5 cursor-pointer" onClick={() => setShowSuccessModal(false)}></div>
              
              <div className="flex flex-col items-center text-center space-y-2 mb-4">
                <div className="w-14 h-14 bg-green-100 dark:bg-green-950/30 text-green-500 rounded-full flex items-center justify-center shadow-inner">
                  <CheckCircle2 size={36} />
                </div>
                <h2 className="text-xl font-black text-slate-900 dark:text-white">تم اعتماد الفاتورة وتسجيل البيع!</h2>
                <p className="text-xs text-slate-400 font-bold">بإمكانك تنزيل الكشف أو مشاركته الفورية الآن</p>
              </div>

              {/* Virtual Receipt Preview Container */}
              <div className="border border-slate-200 dark:border-slate-800 rounded-2xl p-5 bg-slate-50 dark:bg-slate-900 shadow-inner font-sans space-y-3">
                <div className="flex justify-between items-center text-xs text-slate-400 border-b border-dashed border-slate-200 dark:border-slate-800 pb-2.5">
                  <span className="font-extrabold text-blue-500">شـبـكـة الـدحـشـة الـلاسـلـكـيـة</span>
                  <span className="font-bold">{lastSaleSummary.date}</span>
                </div>

                {lastSaleSummary.shopName && (
                  <div className="flex justify-between items-center text-xs font-bold text-slate-500 border-b border-dashed border-slate-200 dark:border-slate-800 pb-2">
                    <span>العميل / البقالة:</span>
                    <span className="text-slate-800 dark:text-slate-200 font-extrabold">{lastSaleSummary.shopName}</span>
                  </div>
                )}

                <div className="space-y-2 py-1.5">
                  {lastSaleSummary.items.map((item, idx) => (
                    <div key={idx} className="flex justify-between items-center text-sm">
                      <span className="font-extrabold text-slate-900 dark:text-white">{item.quantity} أبو {item.category}</span>
                      <span className="font-black text-slate-800 dark:text-slate-300 font-sans">{item.total} ريال</span>
                    </div>
                  ))}
                </div>

                <div className="border-t border-dashed border-slate-200 dark:border-slate-800 pt-3 space-y-1.5">
                  <div className="flex justify-between items-center text-md font-black">
                    <span className="text-slate-900 dark:text-white">المجموع الكلي:</span>
                    <span className="text-blue-600 dark:text-blue-400 font-sans">{lastSaleSummary.totalAmount} ريال</span>
                  </div>
                  <div className="flex justify-between items-center text-xs text-slate-400">
                    <span>المقبوض: {lastSaleSummary.receivedAmount} ريال</span>
                    <span className="text-green-500 font-bold">المتبقي: {lastSaleSummary.remainingAmount} ريال</span>
                  </div>
                </div>
              </div>

              {/* Quick Thermal toggle switch inside receipt popup */}
              <div className="mt-3.5 flex items-center justify-between p-3.5 bg-slate-50 dark:bg-slate-900/40 rounded-2xl border border-slate-100 dark:border-slate-800/85">
                <div className="flex items-center gap-2">
                  <Printer size={18} className={isThermalMode ? "text-amber-500 animate-pulse" : "text-slate-400"} />
                  <span className="text-xs font-extrabold text-slate-800 dark:text-slate-300">نمط الفاتورة الحرارية (أبيض وأسود)</span>
                </div>
                <button
                  onClick={() => setIsThermalMode(!isThermalMode)}
                  className={`relative w-10 h-5 rounded-full transition-colors flex items-center ${isThermalMode ? 'bg-amber-500 justify-end' : 'bg-slate-200 dark:bg-slate-800 justify-start'}`}
                >
                  <span className="w-4 h-4 rounded-full bg-white mx-0.5 shadow-sm inline-block" />
                </button>
              </div>

              {/* Sharing Layout Buttons */}
              <div className="grid grid-cols-2 gap-3 mt-6">
                <button 
                  onClick={() => handleWhatsAppSingleShare(lastSaleSummary, isThermalMode)}
                  className="p-3.5 rounded-2xl bg-slate-100 hover:bg-slate-200 dark:bg-slate-900 dark:hover:bg-slate-800 border border-slate-200/50 dark:border-slate-800 text-slate-900 dark:text-slate-200 flex flex-col items-center gap-1.5 text-xs font-black transition-colors active:scale-95 cursor-pointer"
                >
                  <Share2 size={20} className="text-green-500" />
                  مشاركة عبر الواتساب
                </button>

                <button 
                  onClick={() => handleSMSSingleShare(lastSaleSummary)}
                  className="p-3.5 rounded-2xl bg-slate-100 hover:bg-slate-200 dark:bg-slate-900 dark:hover:bg-slate-800 border border-slate-200/50 dark:border-slate-800 text-slate-900 dark:text-slate-200 flex flex-col items-center gap-1.5 text-xs font-black transition-colors active:scale-95 cursor-pointer"
                >
                  <FileText size={20} className="text-blue-500" />
                  أرسل كـ رسالة نصية (SMS)
                </button>

                <button 
                  onClick={() => {
                    downloadInvoicePDF(lastSaleSummary, isThermalMode);
                    showToast('تم تحميل كشف الـ PDF بنجاح 📁', 'success');
                  }}
                  className="p-3.5 rounded-2xl bg-slate-100 hover:bg-slate-200 dark:bg-slate-900 dark:hover:bg-slate-800 border border-slate-200/50 dark:border-slate-800 text-slate-900 dark:text-slate-200 flex flex-col items-center gap-1.5 text-xs font-black transition-colors active:scale-95 cursor-pointer"
                >
                  <Download size={20} className="text-amber-500" />
                  تنزيل الفاتورة (PDF)
                </button>

                <button 
                  onClick={() => {
                    downloadInvoicePNG(lastSaleSummary, isThermalMode);
                    showToast('تم تحميل كشف الصورة PNG بنجاح 🖼️', 'success');
                  }}
                  className="p-3.5 rounded-2xl bg-slate-100 hover:bg-slate-200 dark:bg-slate-900 dark:hover:bg-slate-800 border border-slate-200/50 dark:border-slate-800 text-slate-900 dark:text-slate-200 flex flex-col items-center gap-1.5 text-xs font-black transition-colors active:scale-95 cursor-pointer"
                >
                  <FileImage size={20} className="text-pink-500" />
                  تنزيل الفاتورة كـ صورة
                </button>
              </div>

              <button 
                onClick={() => setShowSuccessModal(false)}
                className="w-full mt-6 py-4.5 bg-blue-600 hover:bg-blue-700 text-white font-black rounded-2xl active:scale-95 transition-transform text-md"
              >
                فاتورة جديدة ومتابعة العمل
              </button>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Toast Alert */}
      <AnimatePresence>
        {toast && (
          <motion.div
            initial={{ opacity: 0, y: -50, scale: 0.9 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, scale: 0.95 }}
            className={`fixed top-4 left-4 right-4 md:left-auto md:right-4 md:w-80 z-50 p-4 rounded-3xl shadow-lg border flex items-center gap-3 font-bold transition-all text-xs ${
              toast.type === 'success' 
                ? 'bg-green-500 text-white border-green-600 shadow-green-500/10' 
                : toast.type === 'error'
                ? 'bg-red-500 text-white border-red-600 shadow-red-500/10'
                : 'bg-indigo-600 text-white border-indigo-700 shadow-indigo-600/10'
            }`}
          >
            {toast.type === 'success' ? <CheckCircle2 size={16} className="shrink-0" /> : <Info size={16} className="shrink-0" />}
            <span className="flex-1 leading-normal">{toast.message}</span>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Report Modal */}
      <AnimatePresence>
        {showReport && (
          <div className="fixed inset-0 z-50 flex flex-col pt-10">
            <motion.div 
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setShowReport(false)}
              className="absolute inset-0 bg-slate-900/60 backdrop-blur-sm"
            />
            <motion.div 
              initial={{ y: '100%' }}
              animate={{ y: 0 }}
              exit={{ y: '100%' }}
              transition={{ type: 'spring', damping: 25, stiffness: 300 }}
              className="relative mt-auto w-full bg-white dark:bg-slate-950 rounded-t-[3rem] p-6 max-h-[85vh] overflow-y-auto border-t border-slate-200 dark:border-slate-800 shadow-2xl"
            >
              <div className="w-12 h-1.5 bg-slate-200 dark:bg-slate-800 rounded-full mx-auto mb-6 cursor-pointer" onClick={() => setShowReport(false)}></div>
              
              <div className="flex justify-between items-center mb-6">
                <h2 className="text-2xl font-black text-slate-900 dark:text-white flex items-center gap-3">
                  <History size={26} className="text-blue-500" />
                  تقرير اليوم المالي
                </h2>
                <div className="flex gap-2">
                  <button 
                    onClick={() => {
                      downloadDailyReportPDF(dailyReport, salesHistory);
                      showToast('تم حفظ تقرير PDF المالي الشامل للتحميل! 📁', 'success');
                    }}
                    className="p-3.5 bg-blue-50 hover:bg-blue-100 dark:bg-blue-900/20 dark:hover:bg-blue-900/40 text-blue-500 rounded-2xl active:scale-95 transition-all outline-none"
                    title="تنزيل تقرير PDF المالي الشامل لليوم"
                  >
                    <Download size={20} />
                  </button>
                  <button 
                    onClick={() => handleWhatsAppReportShare(dailyReport, salesHistory)}
                    className="p-3.5 bg-green-50 hover:bg-green-100 dark:bg-green-900/20 dark:hover:bg-green-900/40 text-green-500 rounded-2xl active:scale-95 transition-all outline-none"
                    title="مشاركة تقرير المبيعات يومية ع الواتساب"
                  >
                    <Share2 size={20} />
                  </button>
                </div>
              </div>

              <div className="space-y-8">
                {/* Total Summary Header metrics card */}
                <div className="p-6 bg-gradient-to-br from-blue-600 to-indigo-700 rounded-[2rem] text-white shadow-xl shadow-blue-500/20">
                  <span className="text-blue-100 text-xs font-bold">إجمالي دخل ومعاملات اليوم</span>
                  <div className="text-5xl font-black mt-1 flex items-baseline gap-2 font-sans">
                    {dailyReport.totalAmount}
                    <span className="text-sm font-normal opacity-60 font-sans">ريال يمني</span>
                  </div>
                </div>

                {/* Charts Section */}
                <div className="space-y-6">
                  <div className="flex items-center gap-2">
                    <BarChart3 size={20} className="text-blue-500" />
                    <h3 className="font-extrabold text-slate-900 dark:text-white text-sm">توزيع مبيعات اليوم (بالكمية)</h3>
                  </div>
                  <div className="h-64 w-full">
                    <ResponsiveContainer width="100%" height="100%">
                      <BarChart data={categoryData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                        <CartesianGrid strokeDasharray="3 3" vertical={false} stroke={isDark ? "#1e293b" : "#f1f5f9"} />
                        <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: isDark ? "#94a3b8" : "#64748b" }} />
                        <YAxis axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: isDark ? "#94a3b8" : "#64748b" }} />
                        <Tooltip 
                          contentStyle={{ 
                            backgroundColor: isDark ? '#020617' : '#ffffff', 
                            borderRadius: '16px', 
                            border: `1px solid ${isDark ? '#1e293b' : '#f1f5f9'}`,
                            boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.1)' 
                          }}
                        />
                        <Legend iconType="circle" />
                        <Bar dataKey="العادية" fill="#3b82f6" radius={[4, 4, 0, 0]} />
                        <Bar dataKey="PRO" fill="#8b5cf6" radius={[4, 4, 0, 0]} />
                      </BarChart>
                    </ResponsiveContainer>
                  </div>

                  {trends[trendView].length > 0 && (
                    <>
                      <div className="flex flex-col gap-4 pt-4">
                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-2">
                            <TrendingUp size={20} className="text-green-500" />
                            <h3 className="font-extrabold text-slate-900 dark:text-white text-sm">حركة المبيعات الإجمالية</h3>
                          </div>
                          <div className="flex bg-slate-100 dark:bg-slate-900 p-1 rounded-xl">
                            <button 
                              onClick={() => setTrendView('weekly')}
                              className={`px-3 py-1.5 text-xs font-bold rounded-lg transition-all cursor-pointer ${trendView === 'weekly' ? 'bg-white dark:bg-slate-800 text-blue-600 shadow-sm' : 'text-slate-400'}`}
                            >
                              أسبوعي
                            </button>
                            <button 
                              onClick={() => setTrendView('monthly')}
                              className={`px-3 py-1.5 text-xs font-bold rounded-lg transition-all cursor-pointer ${trendView === 'monthly' ? 'bg-white dark:bg-slate-800 text-blue-600 shadow-sm' : 'text-slate-400'}`}
                            >
                              شهري
                            </button>
                          </div>
                        </div>
                        <div className="h-64 w-full">
                          <ResponsiveContainer width="100%" height="100%">
                            <LineChart data={trends[trendView]} margin={{ top: 10, right: 10, left: -10, bottom: 0 }}>
                              <CartesianGrid strokeDasharray="3 3" vertical={false} stroke={isDark ? "#1e293b" : "#f1f5f9"} />
                              <XAxis dataKey="date" axisLine={false} tickLine={false} tick={{ fontSize: 10, fill: isDark ? "#94a3b8" : "#64748b" }} />
                              <YAxis axisLine={false} tickLine={false} tick={{ fontSize: 10, fill: isDark ? "#94a3b8" : "#64748b" }} />
                              <Tooltip 
                                contentStyle={{ 
                                  backgroundColor: isDark ? '#020617' : '#ffffff', 
                                  borderRadius: '16px', 
                                  border: `1px solid ${isDark ? '#1e293b' : '#f1f5f9'}`,
                                  textAlign: 'right'
                                }}
                              />
                               <Line type="monotone" dataKey="إجمالي" stroke="#10b981" strokeWidth={3} dot={{ r: 4, fill: '#10b981' }} activeDot={{ r: 6 }} />
                               <Line type="monotone" dataKey="العادية" stroke="#3b82f6" strokeWidth={2} dot={false} strokeDasharray="5 5" />
                               <Line type="monotone" dataKey="Pro" stroke="#8b5cf6" strokeWidth={2} dot={false} strokeDasharray="5 5" />
                            </LineChart>
                          </ResponsiveContainer>
                        </div>
                      </div>
                    </>
                  )}
                </div>

                {/* Section Details list */}
                {[
                  { type: CalculatorType.REGULAR, label: 'الكروت العادية', color: 'bg-green-500' },
                  { type: CalculatorType.PRO, label: 'كروت فئة Pro', color: 'bg-purple-500' }
                ].map(section => (
                  <div key={section.type} className="space-y-3">
                    <div className="flex items-center gap-2">
                      <div className={`w-1.5 h-4.5 rounded-full ${section.color}`}></div>
                      <h3 className="font-extrabold text-slate-900 dark:text-white text-sm">{section.label}</h3>
                    </div>
                    <div className="grid grid-cols-1 gap-2">
                      {prices[section.type].map(p => {
                        // @ts-ignore
                        const stats = dailyReport[section.type][p.id];
                        if (!stats || stats.quantity === 0) return null;
                        return (
                          <div key={p.id} className="flex justify-between items-center p-4 bg-white dark:bg-slate-900 rounded-2xl border border-slate-100 dark:border-slate-800 shadow-sm">
                            <div className="flex flex-col">
                              <span className="font-bold text-slate-900 dark:text-white">{p.label}</span>
                              <span className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">باع {stats.quantity} كروت</span>
                            </div>
                            <div className="flex flex-col items-end">
                              <span className="text-lg font-black text-blue-600 dark:text-blue-400 font-sans">{stats.amount}</span>
                              <span className="text-[10px] text-slate-400 font-bold uppercase">ريال</span>
                            </div>
                          </div>
                        );
                      })}
                      {Object.values(dailyReport[section.type] as Record<number, { quantity: number; amount: number }>).every(v => v.quantity === 0) && (
                        <div className="text-center py-6 text-slate-400 text-xs font-bold leading-normal bg-slate-50/50 dark:bg-slate-900/50 rounded-2xl border border-dashed border-slate-200 dark:border-slate-800">
                          لم يتم تسجيل بيع كروت من هذه الفئة اليوم
                        </div>
                      )}
                    </div>
                  </div>
                ))}
              </div>

              <button 
                onClick={() => setShowReport(false)}
                className="w-full mt-8 py-5 bg-slate-100 dark:bg-slate-900 text-slate-900 dark:text-white font-black rounded-2xl active:scale-95 transition-transform cursor-pointer hover:bg-slate-200 dark:hover:bg-slate-800"
              >
                إغلاق التقرير اليومي
              </button>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Bottom Navigation */}
      <nav className="fixed bottom-0 left-0 right-0 z-40 bg-white/80 dark:bg-slate-950/80 backdrop-blur-xl border-t border-slate-100 dark:border-slate-800 px-4 py-4 flex items-center justify-between shadow-[0_-10px_30px_-15px_rgba(0,0,0,0.1)] transition-colors">
        <NavButton 
          active={activeTab === 'regular'} 
          onClick={() => setActiveTab('regular')} 
          icon={<Calculator size={22} />} 
          label="العادية"
        />
        <NavButton 
          active={activeTab === 'pro'} 
          onClick={() => setActiveTab('pro')} 
          icon={<Zap size={22} />} 
          label="برو"
        />
        <NavButton 
          active={activeTab === 'shops'} 
          onClick={() => setActiveTab('shops')} 
          icon={<History size={22} />} 
          label="البقالات"
        />
        <NavButton 
          active={activeTab === 'about'} 
          onClick={() => setActiveTab('about')} 
          icon={<Info size={22} />} 
          label="حول"
        />
      </nav>
    </div>
  );
}

function NavButton({ active, onClick, icon, label }: { active: boolean; onClick: () => void; icon: ReactNode; label: string }) {
  return (
    <button 
      onClick={onClick}
      className={`flex flex-col items-center gap-1 transition-all duration-300 relative cursor-pointer ${active ? 'text-blue-500' : 'text-slate-400 dark:text-slate-600'}`}
    >
      <div className={`p-2 rounded-2xl transition-colors ${active ? 'bg-blue-50 dark:bg-blue-900/20' : ''}`}>
        {icon}
      </div>
      <span className={`text-[10px] font-black ${active ? 'opacity-100' : 'opacity-60'}`}>{label}</span>
      {active && (
        <motion.div 
          layoutId="nav-dot"
          className="absolute -top-1 w-1 h-1 bg-blue-500 rounded-full"
        />
      )}
    </button>
  );
}
