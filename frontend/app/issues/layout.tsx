'use client';

import Sidebar from '@/components/Sidebar';
import { ReactNode } from 'react';

export default function IssuesLayout({
    children,
}: {
    children: ReactNode;
}) {
    return (
        <div className="min-h-screen bg-[#0A0D12]">
            <Sidebar />
            <div className="ml-64 min-h-screen">
                <main className="p-8">{children}</main>
            </div>
        </div>
    );
}
