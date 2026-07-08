'use client';

import React from 'react';
import { useRouter } from 'next/navigation';
import {
  Landmark,
  Send,
  Wallet,
  Heart,
  Gift,
  ShoppingCart,
  FileText,
  TrendingUp,
  Zap,
  Phone,
  DollarSign,
  Gamepad2,
  Users,
  Award,
  AlertCircle,
  Shield,
} from 'lucide-react';
import { BottomNavigation } from '@/components/navigation/BottomNavigation';
import { ServiceCard } from '@/components/cards/ServiceCard';

export default function ServicesScreen() {
  const router = useRouter();

  const handleNavigation = (id: string) => {
    if (id === 'profile') router.push('/profile');
    else if (id === 'reports') router.push('/reports');
    else if (id === 'services') router.push('/services');
    else if (id === 'home') router.push('/');
  };

  const navItems = [
    { id: 'profile', label: 'الملف', icon: <Landmark size={24} />, active: false },
    { id: 'reports', label: 'التقارير', icon: <FileText size={24} />, active: false },
    { id: 'home', label: 'الرئيسية', icon: <Wallet size={24} />, active: false },
    { id: 'services', label: 'الخدمات', icon: <ShoppingCart size={24} />, active: true },
    { id: 'main', label: 'الرئيسية', icon: <Wallet size={24} />, active: false },
  ];

  const allServices = [
    { icon: <Landmark size={32} />, title: 'الشيك والسحاد' },
    { icon: <Send size={32} />, title: 'تحويل إلى مشترك' },
    { icon: <Wallet size={32} />, title: 'دفع المشتريات' },
    { icon: <Zap size={32} />, title: 'طلب سحب نقدي' },
    { icon: <Send size={32} />, title: 'تحويل إلى مشترك' },
    { icon: <Wallet size={32} />, title: 'دفع المشتريات' },
    { icon: <Send size={32} />, title: 'استلام حوالة عن شبكات محلية' },
    { icon: <TrendingUp size={32} />, title: 'استلام حوالة' },
    { icon: <Send size={32} />, title: 'إرسال حوالة عن شبكات محلية تحويل' },
    { icon: <Zap size={32} />, title: 'إرسال حوالة عن شبكات محلية' },
    { icon: <Landmark size={32} />, title: 'الشيك والسحاد' },
    { icon: <Send size={32} />, title: 'تحويل بين حسابتي (محمرغ)' },
    { icon: <Shield size={32} />, title: 'القطع حوالة' },
    { icon: <Award size={32} />, title: 'حالة حوالة' },
    { icon: <TrendingUp size={32} />, title: 'تحويل إلى محافظ أخرى' },
    { icon: <Gamepad2 size={32} />, title: 'طلب استلام حوالة دولية' },
    { icon: <Gift size={32} />, title: 'بطاقات' },
    { icon: <DollarSign size={32} />, title: 'سحب من الصرافة التي كاك بنك' },
    { icon: <Wallet size={32} />, title: 'سحب من الصرافة' },
    { icon: <Users size={32} />, title: 'YKB سحب من الصرافة التي' },
    { icon: <ShoppingCart size={32} />, title: 'كود شراء' },
    { icon: <Gift size={32} />, title: 'بطاقات' },
    { icon: <Heart size={32} />, title: 'تطبيقات' },
    { icon: <Users size={32} />, title: 'تسديد الرواتب للتحويل الوصول' },
    { icon: <Award size={32} />, title: 'ترعات' },
    { icon: <AlertCircle size={32} />, title: 'البطاقات الرقمية' },
  ];

  return (
    <main className="min-h-screen bg-background pb-24">
      <div className="bg-card p-6 sticky top-0 z-10 border-b border-border">
        <h1 className="text-2xl font-bold text-foreground">تخصيص خدمات</h1>
      </div>

      <div className="px-6 py-6 space-y-4">
        <div className="grid grid-cols-3 gap-3">
          {allServices.map((service, idx) => (
            <ServiceCard key={idx} {...service} />
          ))}
        </div>
      </div>

      <BottomNavigation
        items={navItems}
        onItemClick={handleNavigation}
        showFab={false}
      />
    </main>
  );
}
