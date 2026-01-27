/**
 * Role-Based Access Control (RBAC) utilities
 * Gestion des permissions et accès basés sur les rôles
 */

export type Role = "CLIENT" | "ADMIN" | "SECRETAIRE" | "TECHNICIEN"

export interface User {
  id: number
  role: Role
  email?: string
  identifiant?: string
}

/**
 * Vérifie si l'utilisateur a un rôle spécifique
 */
export function hasRole(user: User | null, role: Role): boolean {
  if (!user) return false
  return user.role === role
}

/**
 * Vérifie si l'utilisateur a l'un des rôles spécifiés
 */
export function hasAnyRole(user: User | null, roles: Role[]): boolean {
  if (!user) return false
  return roles.includes(user.role)
}

/**
 * Vérifie si l'utilisateur est un administrateur
 */
export function isAdmin(user: User | null): boolean {
  return hasRole(user, "ADMIN")
}

/**
 * Vérifie si l'utilisateur est un client
 */
export function isClient(user: User | null): boolean {
  return hasRole(user, "CLIENT")
}

/**
 * Vérifie si l'utilisateur est un secrétaire
 */
export function isSecretaire(user: User | null): boolean {
  return hasRole(user, "SECRETAIRE")
}

/**
 * Vérifie si l'utilisateur est un technicien
 */
export function isTechnicien(user: User | null): boolean {
  return hasRole(user, "TECHNICIEN")
}

/**
 * Vérifie si l'utilisateur a les permissions pour créer un client
 */
export function canCreateClient(user: User | null): boolean {
  return hasAnyRole(user, ["ADMIN", "SECRETAIRE"])
}

/**
 * Vérifie si l'utilisateur a les permissions pour modifier un client
 */
export function canUpdateClient(user: User | null): boolean {
  return hasAnyRole(user, ["ADMIN", "SECRETAIRE"])
}

/**
 * Vérifie si l'utilisateur a les permissions pour supprimer un client
 */
export function canDeleteClient(user: User | null): boolean {
  return hasRole(user, "ADMIN")
}

/**
 * Vérifie si l'utilisateur a les permissions pour créer une machine
 */
export function canCreateMachine(user: User | null): boolean {
  return hasAnyRole(user, ["ADMIN", "SECRETAIRE"])
}

/**
 * Vérifie si l'utilisateur a les permissions pour modifier une machine
 */
export function canUpdateMachine(user: User | null): boolean {
  return hasAnyRole(user, ["ADMIN", "SECRETAIRE", "TECHNICIEN"])
}

/**
 * Vérifie si l'utilisateur a les permissions pour supprimer une machine
 */
export function canDeleteMachine(user: User | null): boolean {
  return hasRole(user, "ADMIN")
}

/**
 * Vérifie si l'utilisateur a les permissions pour voir toutes les machines
 */
export function canViewAllMachines(user: User | null): boolean {
  return hasAnyRole(user, ["ADMIN", "SECRETAIRE", "TECHNICIEN"])
}

/**
 * Vérifie si l'utilisateur a les permissions pour créer un utilisateur (staff)
 */
export function canCreateUser(user: User | null): boolean {
  return hasRole(user, "ADMIN")
}

/**
 * Vérifie si l'utilisateur a les permissions pour modifier un utilisateur (staff)
 */
export function canUpdateUser(user: User | null): boolean {
  return hasRole(user, "ADMIN")
}

/**
 * Vérifie si l'utilisateur a les permissions pour supprimer un utilisateur (staff)
 */
export function canDeleteUser(user: User | null): boolean {
  return hasRole(user, "ADMIN")
}
