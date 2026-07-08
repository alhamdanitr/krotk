'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import {
  Landmark,
  CreditCard,
  Heart,
  Share2,
  Send,
  DollarSign,
  Wallet,
  TrendingUp,
  ShoppingCart,
  Zap,
  Phone,
  Gift,
  Gamepad2,
  AlertCircle,
} from 'lucide-react';
import { BottomNavigation } from '@/components/navigation/BottomNavigation';
import { ServiceCard } from '@/components/cards/ServiceCard';
import { AccountCard } from '@/components/cards/AccountCard';
import { TransactionItem } from '@/components/cards/TransactionItem';
import { BottomSheet } from '@/components/modals/BottomSheet';

export default function Home() {
  const router = useRouter();
  const [showBottomSheet, setShowBottomSheet] = useState(false);
  const [activeCard, setActiveCard] = useState(0);

  const navItems = [
    {
      id: 'profile',
      label: 'الملف',
      icon: <Landmark size={24} />,
      active: false,
    },
    {
      id: 'reports',
      label: 'التقارير',
      icon: <TrendingUp size={24} />,
      active: false,
    },
    {
      id: 'home',
      label: 'الرئيسية',
      icon: <Wallet size={24} />,
      active: true,
    },
    {
      id: 'services',
      label: 'الخدمات',
      icon: <ShoppingCart size={24} />,
      active: false,
    },
    {
      id: 'main',
      label: 'الرئيسية',
      icon: <Wallet size={24} />,
      active: false,
    },
  ];

  const handleNavigation = (id: string) => {
    if (id === 'profile') router.push('/profile');
    else if (id === 'reports') router.push('/reports');
    else if (id === 'services') router.push('/services');
    else if (id === 'home') router.push('/');
  };

  const accountCards = [
    {
      accountName: 'حساب ريال يمني',
      accountNumber: '•••••',
      balance: '1,234.50',
      holderName: 'جارالله صالح احمد الكيودي',
    },
    {
      accountName: 'حساب دولار',
      accountNumber: '•••••',
      balance: '567.89',
      holderName: 'جارالله صالح احمد الكيودي',
    },
  ];

  const services = [
    { icon: <Landmark size={32} />, title: 'الشيك والسحاد' },
    { icon: <TrendingUp size={32} />, title: 'حوالات محلية' },
    { icon: <Send size={32} />, title: 'تحويلات مالية' },
    { icon: <CreditCard size={32} />, title: 'سحب نقدي' },
    { icon: <Wallet size={32} />, title: 'دفع المشتريات' },
    { icon: <Zap size={32} />, title: 'شراء أوتالث' },
    { icon: <Heart size={32} />, title: 'المفضلة' },
    { icon: <Gamepad2 size={32} />, title: 'خدمات ترفيه' },
    { icon: <AlertCircle size={32} />, title: 'المدفوعات' },
  ];

  const transactions = [
    {
      icon: <Landmark size={20} />,
      title: 'الشيك والسحاد',
      date: '[01:43] 28/06/2026',
      amount: '2,400',
      amountType: 'negative' as const,
    },
    {
      icon: <Send size={20} />,
      title: 'تحويل مشترك',
      date: '[01:25] 28/06/2026',
      amount: '2,400',
      amountType: 'positive' as const,
    },
    {
      icon: <Landmark size={20} />,
      title: 'تحويل مشترك',
      date: '[23:16] 27/06/2026',
      amount: '3,000',
      amountType: 'negative' as const,
    },
    {
      icon: <Landmark size={20} />,
      title: 'تحويل مشترك',
      date: '[21:18] 27/06/2026',
      amount: '1,000',
      amountType: 'negative' as const,
    },
  ];

  const bottomSheetActions = [
    { icon: <Wallet size={32} />, title: 'دفع لاجير' },
    { icon: <Send size={32} />, title: 'الدفع لاجير' },
    { icon: <Heart size={32} />, title: 'المفضلة' },
    { icon: <Landmark size={32} />, title: 'نقاط الخدمة' },
    { icon: <Wallet size={32} />, title: 'مسح الخدود' },
    { icon: <AlertCircle size={32} />, title: 'المدفوعات' },
  ];

  return (
    <main className="min-h-screen bg-background pb-24">
      <div className="bg-card p-6 sticky top-0 z-10 border-b border-border">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-foreground">صباح الخير</h1>
            <p className="text-sm text-muted-foreground">جارالله</p>
          </div>
          <div className="flex items-center gap-3">
            <button className="w-10 h-10 rounded-full bg-muted flex items-center justify-center hover:bg-muted/80">
              <Landmark size={20} />
            </button>
            <button className="w-10 h-10 rounded-full bg-muted flex items-center justify-center hover:bg-muted/80">
              <CreditCard size={20} />
            </button>
          </div>
        </div>
      </div>

      <div className="p-6 space-y-4">
        <div className="overflow-x-auto -mx-6 px-6 scrollbar-hide">
          <div className="flex gap-4 pb-2">
            {accountCards.map((card, idx) => (
              <div
                key={idx}
                className="min-w-[calc(100%-48px)] flex-shrink-0"
              >
                <AccountCard
                  {...card}
                  isActive={activeCard === idx}
                  onClick={() => setActiveCard(idx)}
                />
              </div>
            ))}
          </div>
        </div>

        <div className="flex justify-center gap-2">
          {accountCards.map((_, idx) => (
            <button
              key={idx}
              onClick={() => setActiveCard(idx)}
              className={`w-2 h-2 rounded-full transition-all ${
                activeCard === idx ? 'bg-primary w-8' : 'bg-muted-foreground/30'
              }`}
            />
          ))}
        </div>
      </div>

      <div className="px-6 py-4">
        <div className="bg-card rounded-xl p-4 border border-border/50 flex items-center gap-3">
          <Gift size={24} className="text-primary flex-shrink-0" />
          <div className="flex-1">
            <p className="font-semibold text-foreground text-sm">مع خدمة فريق</p>
            <p className="text-xs text-muted-foreground">قسم التالزورة بين الحسابات بسهولة</p>
          </div>
        </div>
      </div>

      <div className="px-6 py-4 space-y-4">
        <h2 className="text-lg font-bold text-foreground">تخصيص خدمات</h2>
        <div className="grid grid-cols-3 gap-3">
          {services.map((service, idx) => (
            <ServiceCard key={idx} {...service} />
          ))}
        </div>
      </div>

      <div className="px-6 py-4 space-y-4">
        <h2 className="text-lg font-bold text-foreground">العمليات</h2>
        <div className="space-y-3">
          {transactions.map((transaction, idx) => (
            <TransactionItem key={idx} {...transaction} />
          ))}
        </div>
      </div>

      <BottomNavigation
        items={navItems}
        onItemClick={handleNavigation}
        onFabClick={() => setShowBottomSheet(true)}
      />

      <BottomSheet
        isOpen={showBottomSheet}
        onClose={() => setShowBottomSheet(false)}
        title="الخيارات"
      >
        <div className="grid grid-cols-2 gap-3">
          {bottomSheetActions.map((action, idx) => (
            <button
              key={idx}
              className="bg-muted rounded-lg p-4 flex flex-col items-center justify-center gap-2 hover:bg-muted/80 transition-colors"
            >
              <div className="text-primary">{action.icon}</div>
              <span className="text-xs font-semibold text-foreground text-center leading-tight">
                {action.title}
              </span>
            </button>
          ))}
        </div>
      </BottomSheet>
    </main>
  );
}
