'use client';

import React from 'react';
import { useRouter } from 'next/navigation';
import { ChevronRight, List, Calendar, Search, User, FileText, ShoppingCart } from 'lucide-react';
import { BottomNavigation } from '@/components/navigation/BottomNavigation';

export default function ReportsScreen() {
  const router = useRouter();

  const handleNavigation = (id: string) => {
    if (id === 'profile') router.push('/profile');
    else if (id === 'reports') router.push('/reports');
    else if (id === 'services') router.push('/services');
    else if (id === 'home') router.push('/');
  };

  const navItems = [
    { id: 'profile', label: 'الملف', icon: <User size={24} />, active: false },
    { id: 'reports', label: 'التقارير', icon: <FileText size={24} />, active: true },
    { id: 'home', label: 'الرئيسية', icon: <Calendar size={24} />, active: false },
    { id: 'services', label: 'الخدمات', icon: <ShoppingCart size={24} />, active: false },
    { id: 'main', label: 'الرئيسية', icon: <Calendar size={24} />, active: false },
  ];

  const reportOptions = [
    { title: 'آخر العمليات', icon: '📋' },
    { title: 'كشف حساب', icon: '📄' },
    { title: 'بحث مخصص', icon: '🔍' },
    { title: 'بحث بالرقم المرجعي', icon: '📌' },
  ];

  return (
    <main className="min-h-screen bg-background pb-24">
      <div className="bg-card p-6 sticky top-0 z-10 border-b border-border">
        <div className="flex items-center justify-between">
          <h1 className="text-2xl font-bold text-foreground">أخرى</h1>
          <ChevronRight size={24} className="text-foreground" />
        </div>
      </div>

      <div className="px-6 py-4 space-y-3">
        <div className="flex items-center gap-2 bg-card rounded-xl px-4 py-3 border border-border">
          <Search size={20} className="text-muted-foreground" />
          <input
            type="text"
            placeholder="بحث..."
            className="flex-1 bg-transparent text-foreground placeholder-muted-foreground outline-none"
          />
        </div>
      </div>

      <div className="px-6 py-4 space-y-3">
        {reportOptions.map((option, idx) => (
          <button
            key={idx}
            className="w-full bg-card rounded-xl p-4 flex items-center justify-between gap-3 border border-border/50 transition-all hover:shadow-md active:scale-95"
          >
            <div className="flex items-center gap-3 flex-1">
              <div className="w-12 h-12 bg-muted rounded-lg flex items-center justify-center text-xl">
                {option.icon}
              </div>
              <span className="text-sm font-semibold text-foreground">{option.title}</span>
            </div>
            <ChevronRight size={20} className="text-muted-foreground" />
          </button>
        ))}
      </div>

      <BottomNavigation
        items={navItems}
        onItemClick={handleNavigation}
        showFab={false}
      />
    </main>
  );
}
