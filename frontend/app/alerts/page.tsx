'use client';

import { useEffect, useState } from 'react';
import { getAlerts, type Alert, type AlertsFilter, AlertType } from '@/lib/alerts';
import { format } from 'date-fns';

export default function AlertsPage() {
    const [alerts, setAlerts] = useState<Alert[]>([]);
    const [totalPages, setTotalPages] = useState(0);
    const [loading, setLoading] = useState(true);
    const [filter, setFilter] = useState<AlertsFilter>({
        page: 0,
        size: 20,
    });

    useEffect(() => {
        loadAlerts();
    }, [filter]);

    const loadAlerts = async () => {
        setLoading(true);
        try {
            const response = await getAlerts(filter);
            setAlerts(response.content);
            setTotalPages(response.totalPages);
        } catch (err) {
            console.error('Failed to load alerts:', err);
        } finally {
            setLoading(false);
        }
    };

    const getAlertTypeColor = (alertType: AlertType) => {
        switch (alertType) {
            case AlertType.SLOW_API:
                return 'bg-yellow-100 text-yellow-800 border-yellow-200';
            case AlertType.BROKEN_API:
                return 'bg-red-100 text-red-800 border-red-200';
            case AlertType.RATE_LIMIT:
                return 'bg-blue-100 text-blue-800 border-blue-200';
            default:
                return 'bg-gray-100 text-gray-800 border-gray-200';
        }
    };

    const getAlertTypeIcon = (alertType: AlertType) => {
        switch (alertType) {
            case AlertType.SLOW_API:
                return '🐌';
            case AlertType.BROKEN_API:
                return '❌';
            case AlertType.RATE_LIMIT:
                return '⚡';
            default:
                return '🔔';
        }
    };

    return (
        <div className="space-y-6 animate-fade-in">
            <div className="mb-8">
                <h1 className="text-4xl font-bold text-slate-100 mb-2">Alerts</h1>
                <p className="text-slate-400">Monitor and manage system alerts</p>
            </div>

            <div>
                <h1 className="text-3xl font-bold text-gray-900 mb-2">Alerts</h1>
                <p className="text-gray-600">View and filter system alerts</p>
            </div>

            {/* Filters */}
            <div className="bg-white rounded-lg shadow-md p-6">
                <h3 className="text-lg font-semibold mb-4">Filters</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">Alert Type</label>
                        <select
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500"
                            onChange={(e) => setFilter({ ...filter, alertType: e.target.value || undefined, page: 0 })}
                        >
                            <option value="">All Types</option>
                            <option value="SLOW_API">Slow API</option>
                            <option value="BROKEN_API">Broken API</option>
                            <option value="RATE_LIMIT">Rate Limit</option>
                        </select>
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">Service Name</label>
                        <input
                            type="text"
                            placeholder="Enter service name"
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500"
                            onChange={(e) => setFilter({ ...filter, serviceName: e.target.value || undefined, page: 0 })}
                        />
                    </div>
                </div>
            </div>

            {/* Alerts List */}
            <div className="space-y-4">
                {loading ? (
                    <div className="bg-white rounded-lg shadow-md p-8 text-center">
                        <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
                    </div>
                ) : alerts.length === 0 ? (
                    <div className="bg-white rounded-lg shadow-md p-8 text-center text-gray-500">
                        No alerts found
                    </div>
                ) : (
                    alerts.map((alert) => (
                        <div key={alert.id} className="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow">
                            <div className="flex items-start justify-between">
                                <div className="flex items-start space-x-4">
                                    <div className="text-3xl">{getAlertTypeIcon(alert.alertType)}</div>
                                    <div>
                                        <div className="flex items-center space-x-2 mb-2">
                                            <span className={`px-3 py-1 rounded-full text-xs font-semibold border ${getAlertTypeColor(alert.alertType)}`}>
                                                {alert.alertType.replace('_', ' ')}
                                            </span>
                                            <span className="text-sm text-gray-500">
                                                {format(new Date(alert.timestamp), 'MMM dd, yyyy HH:mm:ss')}
                                            </span>
                                        </div>
                                        <h3 className="text-lg font-semibold text-gray-900 mb-1">
                                            {alert.serviceName} - {alert.endpoint}
                                        </h3>
                                        <p className="text-gray-600">{alert.reason}</p>
                                    </div>
                                </div>
                            </div>
                        </div>
                    ))
                )}
            </div>

            {/* Pagination */}
            {totalPages > 1 && (
                <div className="bg-white rounded-lg shadow-md px-6 py-4 flex items-center justify-between">
                    <div className="text-sm text-gray-700">
                        Page {(filter.page || 0) + 1} of {totalPages}
                    </div>
                    <div className="flex gap-2">
                        <button
                            onClick={() => setFilter({ ...filter, page: (filter.page || 0) - 1 })}
                            disabled={(filter.page || 0) === 0}
                            className="px-4 py-2 bg-white border border-gray-300 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                            Previous
                        </button>
                        <button
                            onClick={() => setFilter({ ...filter, page: (filter.page || 0) + 1 })}
                            disabled={(filter.page || 0) >= totalPages - 1}
                            className="px-4 py-2 bg-white border border-gray-300 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                            Next
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}
