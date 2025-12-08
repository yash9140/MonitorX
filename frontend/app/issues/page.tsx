'use client';

import { useEffect, useState } from 'react';
import { getIssues, resolveIssue, type Issue, type IssuesFilter, IssueStatus, IssueType } from '@/lib/issues';
import { getCurrentUser } from '@/lib/auth';
import { format } from 'date-fns';

export default function IssuesPage() {
    const [issues, setIssues] = useState<Issue[]>([]);
    const [totalPages, setTotalPages] = useState(0);
    const [loading, setLoading] = useState(true);
    const [selectedIssue, setSelectedIssue] = useState<Issue | null>(null);
    const [filter, setFilter] = useState<IssuesFilter>({
        status: 'OPEN',
        page: 0,
        size: 20,
    });
    const [resolving, setResolving] = useState<string | null>(null);

    useEffect(() => {
        loadIssues();
    }, [filter]);

    const loadIssues = async () => {
        setLoading(true);
        try {
            const response = await getIssues(filter);
            setIssues(response.content);
            setTotalPages(response.totalPages);
        } catch (err) {
            console.error('Failed to load issues:', err);
        } finally {
            setLoading(false);
        }
    };

    const handleResolve = async (issueId: string) => {
        const user = getCurrentUser();
        if (!user) return;

        setResolving(issueId);
        try {
            await resolveIssue(issueId, user.username);
            loadIssues(); // Reload the list
            setSelectedIssue(null);
        } catch (err) {
            console.error('Failed to resolve issue:', err);
            alert('Failed to resolve issue. Please try again.');
        } finally {
            setResolving(null);
        }
    };

    const getIssueTypeColor = (issueType: IssueType) => {
        switch (issueType) {
            case IssueType.SLOW_API:
                return 'bg-yellow-100 text-yellow-800 border-yellow-200';
            case IssueType.BROKEN_API:
                return 'bg-red-100 text-red-800 border-red-200';
            case IssueType.RATE_LIMIT:
                return 'bg-blue-100 text-blue-800 border-blue-200';
            default:
                return 'bg-gray-100 text-gray-800 border-gray-200';
        }
    };

    const getStatusColor = (status: IssueStatus) => {
        return status === IssueStatus.OPEN
            ? 'bg-green-100 text-green-800 border-green-200'
            : 'bg-gray-100 text-gray-800 border-gray-200';
    };

    return (
        <div className="space-y-6 animate-fade-in">
            <div>
                <h1 className="text-3xl font-bold text-gray-900 mb-2">Issues</h1>
                <p className="text-gray-600">Track and resolve API issues</p>
            </div>

            {/* Filters */}
            <div className="bg-white rounded-lg shadow-md p-6">
                <h3 className="text-lg font-semibold mb-4">Filters</h3>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">Status</label>
                        <select
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500"
                            value={filter.status || ''}
                            onChange={(e) => setFilter({ ...filter, status: e.target.value || undefined, page: 0 })}
                        >
                            <option value="">All</option>
                            <option value="OPEN">Open</option>
                            <option value="RESOLVED">Resolved</option>
                        </select>
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">Issue Type</label>
                        <select
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500"
                            onChange={(e) => setFilter({ ...filter, issueType: e.target.value || undefined, page: 0 })}
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

            {/* Issues List */}
            <div className="space-y-4">
                {loading ? (
                    <div className="bg-white rounded-lg shadow-md p-8 text-center">
                        <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
                    </div>
                ) : issues.length === 0 ? (
                    <div className="bg-white rounded-lg shadow-md p-8 text-center text-gray-500">
                        No issues found
                    </div>
                ) : (
                    issues.map((issue) => (
                        <div key={issue.id} className="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow">
                            <div className="flex items-start justify-between">
                                <div className="flex-1">
                                    <div className="flex items-center space-x-2 mb-3">
                                        <span className={`px-3 py-1 rounded-full text-xs font-semibold border ${getIssueTypeColor(issue.issueType)}`}>
                                            {issue.issueType.replace('_', ' ')}
                                        </span>
                                        <span className={`px-3 py-1 rounded-full text-xs font-semibold border ${getStatusColor(issue.status)}`}>
                                            {issue.status}
                                        </span>
                                        <span className="text-sm text-gray-500">
                                            Hit count: {issue.hitCount}
                                        </span>
                                    </div>
                                    <h3 className="text-lg font-semibold text-gray-900 mb-2">
                                        {issue.serviceName} - {issue.endpoint}
                                    </h3>
                                    <div className="grid grid-cols-2 gap-4 text-sm text-gray-600">
                                        <div>
                                            <span className="font-medium">First seen:</span>{' '}
                                            {format(new Date(issue.firstSeenAt), 'MMM dd, yyyy HH:mm')}
                                        </div>
                                        <div>
                                            <span className="font-medium">Last seen:</span>{' '}
                                            {format(new Date(issue.lastSeenAt), 'MMM dd, yyyy HH:mm')}
                                        </div>
                                        {issue.resolvedAt && (
                                            <>
                                                <div>
                                                    <span className="font-medium">Resolved at:</span>{' '}
                                                    {format(new Date(issue.resolvedAt), 'MMM dd, yyyy HH:mm')}
                                                </div>
                                                <div>
                                                    <span className="font-medium">Resolved by:</span> {issue.resolvedBy}
                                                </div>
                                            </>
                                        )}
                                    </div>
                                </div>
                                {issue.status === IssueStatus.OPEN && (
                                    <button
                                        onClick={() => handleResolve(issue.id)}
                                        disabled={resolving === issue.id}
                                        className="ml-4 px-4 py-2 bg-green-600 hover:bg-green-700 disabled:bg-gray-400 text-white text-sm font-medium rounded-lg transition-colors disabled:cursor-not-allowed"
                                    >
                                        {resolving === issue.id ? 'Resolving...' : 'Mark Resolved'}
                                    </button>
                                )}
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
