'use client';

import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend, Area, AreaChart } from 'recharts';
import type { TimeSeries } from '@/lib/stats';
import { format } from 'date-fns';

interface ErrorRateChartProps {
    data: TimeSeries[];
}

export default function ErrorRateChart({ data }: ErrorRateChartProps) {
    const chartData = data.map((item) => ({
        time: format(new Date(item.timestamp), 'HH:00'),
        errorRate: (item.errorRate * 100).toFixed(2),
        errorCount: item.errorCount,
        totalCount: item.totalCount,
    }));

    return (
        <div className="glass-panel p-6">
            <h3 className="text-xl font-semibold text-slate-100 mb-6">Error Rate (Last 24 Hours)</h3>
            {chartData.length === 0 ? (
                <div className="flex items-center justify-center h-64 text-slate-400">
                    No data available
                </div>
            ) : (
                <ResponsiveContainer width="100%" height={300}>
                    <AreaChart data={chartData}>
                        <defs>
                            <linearGradient id="errorGradient" x1="0" y1="0" x2="0" y2="1">
                                <stop offset="5%" stopColor="#ef4444" stopOpacity={0.3} />
                                <stop offset="95%" stopColor="#ef4444" stopOpacity={0} />
                            </linearGradient>
                        </defs>
                        <CartesianGrid strokeDasharray="3 3" stroke="#334155" opacity={0.2} />
                        <XAxis
                            dataKey="time"
                            stroke="#64748b"
                            fontSize={12}
                        />
                        <YAxis
                            label={{ value: 'Error Rate (%)', angle: -90, position: 'insideLeft', style: { fill: '#64748b' } }}
                            stroke="#64748b"
                        />
                        <Tooltip
                            content={({ active, payload }) => {
                                if (active && payload && payload.length) {
                                    const data = payload[0].payload;
                                    return (
                                        <div className="glass-panel p-3 border border-white/20">
                                            <p className="text-sm font-medium text-slate-200">Time: {data.time}</p>
                                            <p className="text-sm text-red-400">Error Rate: {data.errorRate}%</p>
                                            <p className="text-sm text-slate-400">Errors: {data.errorCount}/{data.totalCount}</p>
                                        </div>
                                    );
                                }
                                return null;
                            }}
                        />
                        <Legend
                            wrapperStyle={{ color: '#94a3b8' }}
                            iconType="circle"
                        />
                        <Area
                            type="monotone"
                            dataKey="errorRate"
                            stroke="#ef4444"
                            strokeWidth={2}
                            fill="url(#errorGradient)"
                            name="Error Rate (%)"
                        />
                    </AreaChart>
                </ResponsiveContainer>
            )}
        </div>
    );
}
