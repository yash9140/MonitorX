interface StatsCardProps {
    title: string;
    value: string | number;
    icon: string;
    color?: 'blue' | 'green' | 'yellow' | 'red' | 'indigo' | 'sky';
}

export default function StatsCard({ title, value, icon, color = 'blue' }: StatsCardProps) {
    const colorGradients = {
        blue: 'from-blue-500 to-sky-500',
        green: 'from-emerald-500 to-teal-500',
        yellow: 'from-amber-500 to-orange-500',
        red: 'from-red-500 to-rose-500',
        indigo: 'from-indigo-500 to-purple-500',
        sky: 'from-sky-500 to-cyan-500',
    };

    return (
        <div className="glass-panel p-6 card-hover group">
            <div className="flex items-center justify-between">
                <div className="flex-1">
                    <p className="text-sm font-medium text-slate-400 mb-2">{title}</p>
                    <p className="text-3xl md:text-4xl font-bold text-slate-100 group-hover:scale-105 transition-transform">
                        {value}
                    </p>
                </div>
                <div className={`w-14 h-14 rounded-xl bg-gradient-to-br ${colorGradients[color]} flex items-center justify-center shadow-lg ${color === 'blue' ? 'shadow-blue-500/30' : color === 'green' ? 'shadow-emerald-500/30' : color === 'yellow' ? 'shadow-amber-500/30' : 'shadow-red-500/30'} group-hover:scale-110 transition-transform`}>
                    <span className="text-2xl">{icon}</span>
                </div>
            </div>
        </div>
    );
}
