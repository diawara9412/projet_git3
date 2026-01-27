import { NextResponse } from "next/server"
import type { NextRequest } from "next/server"

/**
 * Middleware de sécurité pour Next.js
 * Vérifie l'authentification et les autorisations pour les routes protégées
 */
export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl
  
  // Récupérer le cookie d'authentification
  const authToken = request.cookies.get("auth_token")
  
  // Routes protégées nécessitant une authentification
  const protectedRoutes = ["/dashboard"]
  
  // Vérifier si la route actuelle est protégée
  const isProtectedRoute = protectedRoutes.some((route) => pathname.startsWith(route))
  
  // Si la route est protégée et qu'il n'y a pas de token
  if (isProtectedRoute && !authToken) {
    // Rediriger vers la page de connexion
    const loginUrl = new URL("/", request.url)
    return NextResponse.redirect(loginUrl)
  }
  
  // Si l'utilisateur est authentifié et essaie d'accéder à la page de connexion
  if (pathname === "/" && authToken) {
    // Rediriger vers le dashboard
    const dashboardUrl = new URL("/dashboard", request.url)
    return NextResponse.redirect(dashboardUrl)
  }
  
  return NextResponse.next()
}

// Configuration des routes à surveiller par le middleware
export const config = {
  matcher: [
    /*
     * Match all request paths except for the ones starting with:
     * - api (API routes)
     * - _next/static (static files)
     * - _next/image (image optimization files)
     * - favicon.ico (favicon file)
     */
    "/((?!api|_next/static|_next/image|favicon.ico).*)",
  ],
}
