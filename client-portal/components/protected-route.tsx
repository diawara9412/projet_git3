"use client"

import { useEffect } from "react"
import { useRouter } from "next/navigation"
import { useAuth } from "@/lib/auth-context"
import { type Role, hasAnyRole } from "@/lib/rbac"
import { Loader2 } from "lucide-react"

interface ProtectedRouteProps {
  children: React.ReactNode
  allowedRoles?: Role[]
  requireAuth?: boolean
}

/**
 * Composant de protection de route basé sur les rôles
 * Vérifie l'authentification et les autorisations avant d'afficher le contenu
 */
export function ProtectedRoute({ 
  children, 
  allowedRoles, 
  requireAuth = true 
}: ProtectedRouteProps) {
  const { client, isLoading, isAuthenticated } = useAuth()
  const router = useRouter()

  useEffect(() => {
    // Attendre la fin du chargement
    if (isLoading) return

    // Si l'authentification est requise et que l'utilisateur n'est pas authentifié
    if (requireAuth && !isAuthenticated) {
      router.push("/")
      return
    }

    // Si des rôles spécifiques sont requis
    if (allowedRoles && allowedRoles.length > 0) {
      if (!client || !hasAnyRole(client, allowedRoles)) {
        // Rediriger vers le dashboard avec un message d'erreur
        router.push("/dashboard?error=unauthorized")
        return
      }
    }
  }, [isLoading, isAuthenticated, client, allowedRoles, requireAuth, router])

  // Afficher un loader pendant la vérification
  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    )
  }

  // Si l'authentification est requise mais l'utilisateur n'est pas authentifié
  if (requireAuth && !isAuthenticated) {
    return null
  }

  // Si des rôles spécifiques sont requis mais l'utilisateur n'a pas le bon rôle
  if (allowedRoles && allowedRoles.length > 0) {
    if (!client || !hasAnyRole(client, allowedRoles)) {
      return null
    }
  }

  return <>{children}</>
}
