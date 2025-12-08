'use client';

import { useEffect, useState } from 'react';
import StatsCard from '@/components/StatsCard';
import TopSlowEndpointsChart from '@/components/TopSlowEndpointsChart';
import ErrorRateChart from '@/components/ErrorRateChart';
import { getStatsSummary, getTopSlowEndpoints, getErrorRateTimeSeries } from '@/lib/stats';
import type { StatsResponse, TopEndpoint, TimeSeries } from '@/lib/stats';

export default function DashboardPage() {
    const [stats, setStats] = useState<StatsResponse | null>(null);
    const [topEndpoints, setTopEndpoints] = useState<TopEndpoint[]>([]);
    const [errorRateSeries, setErrorRateSeries] = useState<TimeSeries[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        loadData();
        // Refresh every 30 seconds
        const interval = setInterval(loadData, 30000);
        return () => clearInterval(interval);
    }, []);

    const loadData = async () => {
        try {
            const [statsData, endpoints, timeSeries] = await Promise.all([
                getStatsSummary(),
                getTopSlowEndpoints(10),
                getErrorRateTimeSeries(24),
            ]);

            setStats(statsData);
            setTopEndpoints(endpoints);
            setErrorRateSeries(timeSeries);
            setError('');
        } catch (err: any) {
            setError('Failed to load dashboard data');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="flex items-center justify-center h-96">
                <div className="text-center">
                    <div className="inline-block animate-spin rounded-full h-12 w-12 border-4 border-indigo-500/30 border-t-indigo-500 mb-4"></div>
                    <p className="text-slate-400">Loading dashboard...</p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="glass-panel p-6 border-red-500/30">
                <div className="flex items-center gap-3 text-red-300">
                    <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    {error}
                </div>
            </div>
        );
    }

    return (
        <div className="space-y-8 animate-fade-in">
            {/* Header */}
            <div>
                <h1 className="text-4xl font-bold text-slate-100 mb-2">Dashboard</h1>
                <p className="text-slate-400">Overview of your API monitoring metrics</p>
            </div>

            {/* Stats Cards */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                <StatsCard
                    title="Slow APIs"
                    value={stats?.slowApiCount || 0}
                    icon="🐌"
                    color="yellow"
                />
                <StatsCard
                    title="Broken APIs"
                    value={stats?.brokenApiCount || 0}
                    icon="❌"
                    color="red"
                />
                <StatsCard
                    title="Rate Limit Hits"
                    value={stats?.rateLimitViolations || 0}
                    icon="⚡"
                    color="sky"
                />
                <StatsCard
                    title="Avg Latency"
                    value={`${Math.round(stats?.averageLatency || 0)}ms`}
                    icon="⏱️"
                    color="green"
                />
            </div>

            {/* Charts */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <TopSlowEndpointsChart data={topEndpoints} />
                <ErrorRateChart data={errorRateSeries} />
            </div>
        </div>
    );
}
