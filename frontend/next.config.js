/** @type {import('next').NextConfig} */
const nextConfig = {
    reactStrictMode: true,

    // Enable standalone output for Docker deployment
    output: 'standalone',

    // Environment variables
    env: {
        NEXT_PUBLIC_API_URL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080',
    },

    // Image optimization for production
    images: {
        domains: ['localhost'],
        unoptimized: process.env.NODE_ENV === 'development',
    },

    // Disable telemetry in production
    telemetry: {
        disabled: true,
    },

    // Compression
    compress: true,

    // Production optimizations
    poweredByHeader: false,
    generateEtags: true,
}

module.exports = nextConfig
