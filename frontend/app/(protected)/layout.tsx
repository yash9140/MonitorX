'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { logout, getCurrentUser } from '@/lib/auth';
import { useEffect, useState } from 'react';

export default function ProtectedLayout({
    children,
}: {
    children: React.ReactNode;
}) {
    const pathname = usePathname();
    const [user, setUser] = useState<any>(null);

    useEffect(() => {
        setUser(getCurrentUser());
    }, []);

    const navigation = [
        { name: 'Dashboard', href: '/dashboard', icon: '📊' },
        { name: 'Logs', href: '/logs', icon: '📝' },
        { name: 'Alerts', href: '/alerts', icon: '🚨' },
        { name: 'Issues', href: '/issues', icon: '🐛' },
    ];

    return (
        <div className="min-h-screen bg-[#0A0D12]">
            {/* Sidebar */}
            <div className="fixed inset-y-0 left-0 w-64 glass-panel border-r border-white/10 backdrop-blur-2xl z-50">
                <div className="flex flex-col h-full">
                    {/* Logo */}
                    <div className="px-6 py-6 border-b border-white/10">
                        <h1 className="text-2xl font-bold gradient-text">MonitorX</h1>
                        {user && (
                            <p className="text-sm text-slate-400 mt-2">Welcome, {user.username}</p>
                        )}
                    </div>

                    {/* Navigation */}
                    <nav className="flex-1 px-4 py-6 space-y-1">
                        {navigation.map((item) => {
                            const isActive = pathname === item.href;
                            return (
                                <Link
                                    key={item.name}
                                    href={item.href}
                                    className={`flex items-center px-4 py-3 rounded-xl text-sm font-medium transition-all ${
                                        isActive
                                            ? 'bg-gradient-to-r from-indigo-500/20 to-sky-500/20 text-white border border-indigo-500/30 shadow-lg shadow-indigo-500/10'
                                            : 'text-slate-400 hover:text-slate-200 hover:bg-white/5'
                                    }`}
                                >
                                    <span className="mr-3 text-xl">{item.icon}</span>
                                    {item.name}
                                </Link>
                            );
                        })}
                    </nav>

                    {/* Logout */}
                    <div className="px-4 py-4 border-t border-white/10">
                        <button
                            onClick={logout}
                            className="w-full flex items-center px-4 py-3 rounded-xl text-sm font-medium text-red-400 hover:text-red-300 hover:bg-red-500/10 transition-all border border-transparent hover:border-red-500/30"
                        >
                            <span className="mr-3 text-xl">🚪</span>
                            Logout
                        </button>
                    </div>
                </div>
            </div>

            {/* Main content */}
            <div className="ml-64 min-h-screen">
                <main className="p-8">{children}</main>
            </div>
        </div>
    );
}
