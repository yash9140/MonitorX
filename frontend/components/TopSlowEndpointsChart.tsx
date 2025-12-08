'use client';

import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import type { TopEndpoint } from '@/lib/stats';

interface TopSlowEndpointsChartProps {
    data: TopEndpoint[];
}

export default function TopSlowEndpointsChart({ data }: TopSlowEndpointsChartProps) {
    const chartData = data.map((item) => ({
        name: `${item.serviceName}:${item.endpoint.substring(0, 20)}...`,
        latency: Math.round(item.averageLatency),
        fullName: `${item.serviceName}${item.endpoint}`,
    }));

    return (
        <div className="glass-panel p-6">
            <h3 className="text-xl font-semibold text-slate-100 mb-6">Top 10 Slow Endpoints</h3>
            {chartData.length === 0 ? (
                <div className=" flex items-center justify-center h-64 text-slate-400">
                    No slow endpoints detected
                </div>
            ) : (
                <ResponsiveContainer width="100%" height={300}>
                    <BarChart data={chartData}>
                        <CartesianGrid strokeDasharray="3 3" stroke="#334155" opacity={0.2} />
                        <XAxis
                            dataKey="name"
                            angle={-45}
                            textAnchor="end"
                            height={100}
                            fontSize={12}
                            stroke="#64748b"
                        />
                        <YAxis
                            label={{ value: 'Latency (ms)', angle: -90, position: 'insideLeft', style: { fill: '#64748b' } }}
                            stroke="#64748b"
                        />
                        <Tooltip
                            content={({ active, payload }) => {
                                if (active && payload && payload.length) {
                                    return (
                                        <div className="glass-panel p-3 border border-white/20">
                                            <p className="text-sm font-medium text-slate-200">{payload[0].payload.fullName}</p>
                                            <p className="text-sm text-amber-400">Latency: {payload[0].value}ms</p>
                                        </div>
                                    );
                                }
                                return null;
                            }}
                        />
                        <Bar
                            dataKey="latency"
                            fill="url(#colorGradient)"
                            radius={[8, 8, 0, 0]}
                        />
                        <defs>
                            <linearGradient id="colorGradient" x1="0" y1="0" x2="0" y2="1">
                                <stop offset="0%" stopColor="#f59e0b" stopOpacity={0.8} />
                                <stop offset="100%" stopColor="#d97706" stopOpacity={0.6} />
                            </linearGradient>
                        </defs>
                    </BarChart>
                </ResponsiveContainer>
            )}
        </div>
    );
}
