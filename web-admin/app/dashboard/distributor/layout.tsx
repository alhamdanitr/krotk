'use client';

import React from 'react';
import { LayoutDashboard, Users, ShoppingCart, Tag, Landmark } from 'lucide-react';
import { PageHeader, Tabs } from '@/components';

const DISTRIBUTOR_TABS = [
  { label: 'الملخص', href: '/dashboard/distributor', icon: <LayoutDashboard className="h-4 w-4" /> },
  { label: 'العملاء', href: '/dashboard/distributor/customers', icon: <Users className="h-4 w-4" /> },
  { label: 'المبيعات', href: '/dashboard/distributor/sales', icon: <ShoppingCart className="h-4 w-4" /> },
  { label: 'التسعيرة', href: '/dashboard/distributor/pricing', icon: <Tag className="h-4 w-4" /> },
  { label: 'المالية', href: '/dashboard/distributor/finance', icon: <Landmark className="h-4 w-4" /> },
];

export default function DistributorLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="space-y-6">
      <PageHeader
        title="إدارة الموزع"
        description="نظام إدارة الموزعين، المبيعات، العملاء، والمالية"
      />
      
      <Tabs items={DISTRIBUTOR_TABS} />
      
      <div className="pt-2">
        {children}
      </div>
    </div>
  );
}
