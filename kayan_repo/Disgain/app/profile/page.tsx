'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import {
  User,
  FileText,
  Wallet,
  ShoppingCart,
  Landmark,
  ChevronDown,
  Cloud,
  Monitor,
  Heart,
  Lock,
  HelpCircle,
  Share2,
  Trash2,
  LogOut,
  QrCode,
} from 'lucide-react';
import { BottomNavigation } from '@/components/navigation/BottomNavigation';
import { SettingsMenuItem } from '@/components/cards/SettingsMenuItem';

export default function ProfileScreen() {
  const router = useRouter();
  const [expandedMenu, setExpandedMenu] = useState<string | null>(null);

  const handleNavigation = (id: string) => {
    if (id === 'profile') router.push('/profile');
    else if (id === 'reports') router.push('/reports');
    else if (id === 'services') router.push('/services');
    else if (id === 'home') router.push('/');
  };

  const navItems = [
    { id: 'profile', label: 'الملف', icon: <User size={24} />, active: true },
    { id: 'reports', label: 'التقارير', icon: <FileText size={24} />, active: false },
    { id: 'home', label: 'الرئيسية', icon: <Wallet size={24} />, active: false },
    { id: 'services', label: 'الخدمات', icon: <ShoppingCart size={24} />, active: false },
    { id: 'main', label: 'الرئيسية', icon: <Wallet size={24} />, active: false },
  ];

  const settingsMenus = [
    {
      id: 'update-data',
      icon: <Cloud size={24} />,
      title: 'تحديث بيانات التطبيق',
    },
    {
      id: 'device-mgmt',
      icon: <Monitor size={24} />,
      title: 'إدارة الأجهزة',
    },
    {
      id: 'client-file',
      icon: <FileText size={24} />,
      title: 'ملف العميل',
      expandable: true,
    },
    {
      id: 'favorites',
      icon: <Heart size={24} />,
      title: 'إدارة المفضلة والاشتراكات',
      expandable: true,
    },
    {
      id: 'security',
      icon: <Lock size={24} />,
      title: 'الخصوصية والأمان',
      expandable: true,
    },
    {
      id: 'settings',
      icon: <Monitor size={24} />,
      title: 'إعدادات إضافية',
    },
    {
      id: 'support',
      icon: <HelpCircle size={24} />,
      title: 'الدعم والمساعدة',
      expandable: true,
    },
    {
      id: 'personalize',
      icon: <Cloud size={24} />,
      title: 'تخصيص التطبيق',
      expandable: true,
    },
    {
      id: 'share-app',
      icon: <Share2 size={24} />,
      title: 'شارك تطبيق جيب',
    },
    {
      id: 'delete-wallet',
      icon: <Trash2 size={24} />,
      title: 'طلب الغاء المحفظة',
    },
    {
      id: 'logout',
      icon: <LogOut size={24} />,
      title: 'تسجيل الخروج',
    },
  ];

  const toggleMenu = (id: string) => {
    setExpandedMenu(expandedMenu === id ? null : id);
  };

  return (
    <main className="min-h-screen bg-background pb-24">
      {/* Profile Header */}
      <div className="bg-card p-6 border-b border-border">
        <div className="flex flex-col items-center gap-4 text-center">
          {/* Avatar */}
          <div className="w-24 h-24 bg-gradient-to-br from-primary to-primary/80 rounded-full flex items-center justify-center text-4xl font-bold text-primary-foreground border-4 border-card shadow-lg">
            جك
          </div>

          {/* User Info */}
          <div>
            <h1 className="text-2xl font-bold text-foreground">
              جارالله صالح احمد الكيودي
            </h1>
          </div>

          {/* Account Details */}
          <div className="w-full bg-gradient-to-r from-primary to-primary/90 rounded-2xl p-4 flex items-center justify-between gap-2">
            <div className="flex-1">
              <p className="text-xs text-primary-foreground/80 mb-1">رقم الحساب</p>
              <p className="text-lg font-bold text-primary-foreground">773303455</p>
            </div>
            <div className="h-12 w-px bg-primary-foreground/20" />
            <div className="flex-1">
              <p className="text-xs text-primary-foreground/80 mb-1">الرقم البديل</p>
              <p className="text-lg font-bold text-primary-foreground">463488</p>
            </div>
            <button className="w-12 h-12 bg-dark rounded-lg flex items-center justify-center text-primary-foreground hover:bg-dark/80 transition-colors">
              <QrCode size={20} />
            </button>
          </div>
        </div>
      </div>

      <div className="px-6 py-6 space-y-3">
        {settingsMenus.map((menu) => (
          <SettingsMenuItem
            key={menu.id}
            icon={menu.icon}
            title={menu.title}
            expandable={menu.expandable}
            isOpen={expandedMenu === menu.id}
            onClick={() => setExpandedMenu(expandedMenu === menu.id ? null : menu.id)}
          >
            {menu.expandable && (
              <div className="space-y-2 mt-2">
                <button className="w-full text-right text-sm text-muted-foreground hover:text-foreground transition-colors py-2 px-3 rounded-lg hover:bg-muted">
                  الخيار 1
                </button>
                <button className="w-full text-right text-sm text-muted-foreground hover:text-foreground transition-colors py-2 px-3 rounded-lg hover:bg-muted">
                  الخيار 2
                </button>
              </div>
            )}
          </SettingsMenuItem>
        ))}
      </div>

      <div className="px-6 py-4 text-center text-xs text-muted-foreground">
        v 0.46.5
      </div>

      <BottomNavigation
        items={navItems}
        onItemClick={handleNavigation}
        showFab={false}
      />
    </main>
  );
}
