import Link from 'next/link';

export default function Home() {
    return (
        <div className="min-h-screen bg-[#0A0D12] relative overflow-hidden">
            {/* Background Gradients */}
            <div className="absolute inset-0 opacity-30">
                <div className="absolute top-0 left-1/4 w-96 h-96 bg-indigo-500 rounded-full blur-[128px]" />
                <div className="absolute bottom-0 right-1/4 w-96 h-96 bg-emerald-500 rounded-full blur-[128px]" />
                <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-sky-500 rounded-full blur-[128px]" />
            </div>

            {/* Content */}
            <div className="relative z-10 min-h-screen flex flex-col">
                {/* Header */}
                <header className="py-6 px-8 border-b border-white/10 backdrop-blur-sm">
                    <div className="max-w-7xl mx-auto flex items-center justify-between">
                        <h2 className="text-2xl font-bold gradient-text">MonitorX</h2>
                        <div className="flex items-center gap-4">
                            <Link href="/login" className="text-slate-300 hover:text-white transition px-4 py-2">
                                Login
                            </Link>
                            <Link href="/signup" className="btn-primary">
                                Get Started
                            </Link>
                        </div>
                    </div>
                </header>

                {/* Hero Section */}
                <main className="flex-1 flex items-center justify-center px-4 py-20">
                    <div className="max-w-5xl mx-auto text-center">
                        <div className="inline-block mb-6">
                            <span className="px-4 py-2 bg-indigo-500/10 border border-indigo-500/30 rounded-full text-indigo-300 text-sm font-medium">
                                ✨ Real-time API Monitoring
                            </span>
                        </div>

                        <h1 className="text-6xl md:text-7xl lg:text-8xl font-bold mb-6 leading-tight">
                            Monitor Your APIs
                            <br />
                            <span className="gradient-text">Like Never Before</span>
                        </h1>

                        <p className="text-xl md:text-2xl text-slate-400 mb-12 max-w-3xl mx-auto">
                            Track performance, detect issues, and gain insights into your API ecosystem
                            with our powerful observability platform.
                        </p>

                        <div className="flex flex-col sm:flex-row gap-4 justify-center items-center mb-16">
                            <Link href="/signup" className="btn-primary text-lg px-8 py-4">
                                Start Monitoring Free
                            </Link>
                            <Link href="/login" className="btn-secondary text-lg px-8 py-4">
                                View Demo
                            </Link>
                        </div>

                        {/* Feature Grid */}
                        <div className="grid md:grid-cols-3 gap-6 mt-20">
                            <div className="glass-panel p-8 card-hover">
                                <div className="w-12 h-12 bg-gradient-to-br from-indigo-500 to-sky-500 rounded-xl flex items-center justify-center mb-4 mx-auto">
                                    <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                                    </svg>
                                </div>
                                <h3 className="text-xl font-semibold text-slate-100 mb-2">Real-time Metrics</h3>
                                <p className="text-slate-400">
                                    Monitor latency, error rates, and throughput in real-time
                                </p>
                            </div>

                            <div className="glass-panel p-8 card-hover">
                                <div className="w-12 h-12 bg-gradient-to-br from-sky-500 to-emerald-500 rounded-xl flex items-center justify-center mb-4 mx-auto">
                                    <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                                    </svg>
                                </div>
                                <h3 className="text-xl font-semibold text-slate-100 mb-2">Smart Alerts</h3>
                                <p className="text-slate-400">
                                    Get notified instantly when issues are detected
                                </p>
                            </div>

                            <div className="glass-panel p-8 card-hover">
                                <div className="w-12 h-12 bg-gradient-to-br from-emerald-500 to-indigo-500 rounded-xl flex items-center justify-center mb-4 mx-auto">
                                    <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                                    </svg>
                                </div>
                                <h3 className="text-xl font-semibold text-slate-100 mb-2">Issue Tracking</h3>
                                <p className="text-slate-400">
                                    Automated issue detection and resolution workflows
                                </p>
                            </div>
                        </div>
                    </div>
                </main>

                {/* Footer */}
                <footer className="py-8 px-8 border-t border-white/10 backdrop-blur-sm">
                    <div className="max-w-7xl mx-auto text-center text-slate-500 text-sm">
                        <p>© 2024 MonitorX. All rights reserved.</p>
                    </div>
                </footer>
            </div>
        </div>
    );
}
