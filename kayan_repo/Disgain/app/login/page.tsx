'use client';

import React, { useState } from 'react';
import { Eye, EyeOff, Building, Phone, Fingerprint } from 'lucide-react';

export default function LoginScreen() {
  const [activeTab, setActiveTab] = useState<'location' | 'business'>('location');
  const [showPassword, setShowPassword] = useState(false);
  const [phone, setPhone] = useState('');
  const [location, setLocation] = useState('');
  const [password, setPassword] = useState('');

  return (
    <main className="min-h-screen bg-background flex flex-col">
      <div className="px-6 pt-8 pb-6 text-center">
        <div className="mb-4">
          <div className="inline-block">
            <div className="text-4xl font-bold text-foreground">
              كيان
              <span className="text-primary mr-2">→</span>
            </div>
            <div className="text-sm text-muted-foreground">Kayan</div>
          </div>
        </div>

        <h1 className="text-2xl font-bold text-foreground mb-1">مرحباً بعودتك</h1>
        <p className="text-sm text-muted-foreground">قم بتسجيل الدخول</p>
      </div>

      <div className="px-6 pb-6">
        <div className="flex gap-3 bg-card rounded-xl p-1 border border-border">
          <button
            onClick={() => setActiveTab('location')}
            className={`flex-1 flex items-center justify-center gap-2 py-2 px-3 rounded-lg transition-all font-semibold ${
              activeTab === 'location'
                ? 'bg-muted text-foreground'
                : 'text-muted-foreground hover:text-foreground'
            }`}
          >
            <Phone size={18} />
            <span className="hidden sm:inline">موقع</span>
          </button>
          <button
            onClick={() => setActiveTab('business')}
            className={`flex-1 flex items-center justify-center gap-2 py-2 px-3 rounded-lg transition-all font-semibold ${
              activeTab === 'business'
                ? 'bg-muted text-foreground'
                : 'text-muted-foreground hover:text-foreground'
            }`}
          >
            <Building size={18} />
            <span className="hidden sm:inline">نقطة بيع</span>
          </button>
        </div>
      </div>

      <div className="flex-1 px-6 space-y-6">
        <div>
          <label className="text-sm text-muted-foreground mb-2 block">
            رقم الهاتف
          </label>
          <input
            type="tel"
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
            placeholder="أدخل رقم الهاتف"
            className="w-full bg-card border border-border rounded-xl px-4 py-3 text-foreground placeholder-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
          />
        </div>

        <div>
          <label className="text-sm text-muted-foreground mb-2 block">
            كلمة المرور
          </label>
          <div className="relative">
            <input
              type={showPassword ? 'text' : 'password'}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="أدخل كلمة المرور"
              className="w-full bg-card border border-border rounded-xl px-4 py-3 text-foreground placeholder-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
            />
            <button
              onClick={() => setShowPassword(!showPassword)}
              className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
            >
              {showPassword ? (
                <EyeOff size={20} />
              ) : (
                <Eye size={20} />
              )}
            </button>
          </div>
        </div>

        <button className="w-full flex items-center justify-between bg-card border border-border rounded-xl px-4 py-3 text-muted-foreground hover:text-foreground transition-colors">
          <span className="text-sm font-medium">استخدم بصمة الإصبع</span>
          <Fingerprint size={20} className="text-primary" />
        </button>

        <div className="text-right">
          <button className="text-sm text-muted-foreground hover:text-primary transition-colors font-medium">
            نسيت كلمة المرور؟
          </button>
        </div>
      </div>

      <div className="px-6 pb-8 space-y-4">
        <button className="w-full bg-primary hover:bg-primary/90 text-primary-foreground font-bold py-3 px-4 rounded-2xl transition-all active:scale-95">
          تسجيل الدخول حنقطة بيع
        </button>

        <div className="text-center">
          <span className="text-muted-foreground text-sm">
            لا تملك حساب؟{' '}
          </span>
          <button className="text-primary font-semibold text-sm hover:underline">
            إنشاء حساب
          </button>
        </div>

        <div className="grid grid-cols-3 gap-3 pt-4">
          <button className="bg-card border border-border rounded-xl py-3 flex flex-col items-center gap-1 hover:bg-muted transition-colors">
            <Phone size={20} className="text-primary" />
            <span className="text-xs font-medium text-foreground text-center">
              الرقم المجاني
            </span>
          </button>
          <button className="bg-card border border-border rounded-xl py-3 flex flex-col items-center gap-1 hover:bg-muted transition-colors">
            <Building size={20} className="text-primary" />
            <span className="text-xs font-medium text-foreground text-center">
              نقاط الخدمة
            </span>
          </button>
          <button className="bg-card border border-border rounded-xl py-3 flex flex-col items-center gap-1 hover:bg-muted transition-colors">
            <Phone size={20} className="text-primary" />
            <span className="text-xs font-medium text-foreground text-center">
              خدمة العملاء
            </span>
          </button>
        </div>
      </div>
    </main>
  );
}
