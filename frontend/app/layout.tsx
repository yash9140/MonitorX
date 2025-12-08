import type { Metadata } from 'next';
import './globals.css';

export const metadata: Metadata = {
    title: 'MonitorX - API Monitoring & Observability',
    description: 'Monitor your APIs with real-time insights and alerts',
};

export default function RootLayout({
    children,
}: {
    children: React.ReactNode;
}) {
    return (
        <html lang="en">
            <body>{children}</body>
        </html>
    );
}
