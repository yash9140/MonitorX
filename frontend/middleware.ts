import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

export function middleware(request: NextRequest) {
    const token = request.cookies.get('monitorx_token')?.value;
    const { pathname } = request.nextUrl;

    // Protected paths
    const protectedPaths = ['/dashboard', '/logs', '/alerts', '/issues'];
    const isProtectedPath = protectedPaths.some(path => pathname.startsWith(path));

    // If accessing protected path without token, redirect to login
    if (isProtectedPath && !token) {
        const loginUrl = new URL('/login', request.url);
        loginUrl.searchParams.set('redirect', pathname);
        return NextResponse.redirect(loginUrl);
    }

    // If accessing login/signup with valid token, redirect to dashboard
    if ((pathname === '/login' || pathname === '/signup') && token) {
        return NextResponse.redirect(new URL('/dashboard', request.url));
    }

    return NextResponse.next();
}

export const config = {
    matcher: ['/dashboard/:path*', '/logs/:path*', '/alerts/:path*', '/issues/:path*', '/login', '/signup'],
};
