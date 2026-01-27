# Guide Rapide - Fonctionnalités de Sécurité Implémentées

## 🎯 Vue d'Ensemble

```
┌─────────────────────────────────────────────────────────────────┐
│                    MACHINE REPAIR MANAGEMENT                     │
│                      SYSTÈME SÉCURISÉ                            │
└─────────────────────────────────────────────────────────────────┘

         Frontend (Next.js)              Backend (Spring Boot)
    ┌──────────────────────┐         ┌──────────────────────┐
    │                      │         │                      │
    │  🔐 Middleware       │────────▶│  🔐 JWT Filter      │
    │  ✓ Auth Check        │  HTTPS  │  ✓ Token Valid      │
    │  ✓ Route Guard       │         │  ✓ Role Check       │
    │                      │         │                      │
    │  🍪 HttpOnly Cookie  │◀────────│  🍪 Secure Cookie   │
    │  ✓ No localStorage   │         │  ✓ HttpOnly         │
    │  ✓ SameSite=Lax      │         │  ✓ SameSite=Lax     │
    │                      │         │                      │
    │  👥 RBAC             │         │  👥 @PreAuthorize   │
    │  ✓ Role Checks       │         │  ✓ ADMIN            │
    │  ✓ Permissions       │         │  ✓ SECRETAIRE       │
    │  ✓ Guards            │         │  ✓ TECHNICIEN       │
    │                      │         │  ✓ CLIENT           │
    └──────────────────────┘         └──────────────────────┘
              │                                  │
              │                                  │
              ▼                                  ▼
    ┌──────────────────────┐         ┌──────────────────────┐
    │  🎨 UI Components    │         │  🛡️ Security Layer   │
    │  ✓ Protected Routes  │         │  ✓ Input Validation │
    │  ✓ Role-based UI     │         │  ✓ Sanitization     │
    └──────────────────────┘         │  ✓ Exception Handle │
                                     │  ✓ SQL Safe (JPA)   │
                                     └──────────────────────┘
                                              │
                                              ▼
                                     ┌──────────────────────┐
                                     │  📧 Email Service    │
                                     │  ✓ Account Created   │
                                     │  ✓ Status Changed    │
                                     │  ✓ Machine Ready     │
                                     └──────────────────────┘
```

## 🔑 Fonctionnalités Clés

### 1. Authentification JWT Sécurisée
```
Login → Backend validates → Generates JWT → Sets HttpOnly Cookie → Frontend receives → Auto-attached to requests
```

**Sécurité:**
- ✅ Cookies HttpOnly (XSS protection)
- ✅ SameSite=Lax (CSRF protection)
- ✅ Expiration 24h
- ✅ HMAC-SHA512 signature

### 2. Contrôle d'Accès (RBAC)

| Rôle        | Créer Client | Modifier Machine | Supprimer User |
|-------------|--------------|------------------|----------------|
| CLIENT      | ❌           | ❌               | ❌             |
| TECHNICIEN  | ❌           | ✅               | ❌             |
| SECRETAIRE  | ✅           | ✅               | ❌             |
| ADMIN       | ✅           | ✅               | ✅             |

### 3. Protection Anti-Injection

**SQL Injection:** ✅ PROTECTED
```java
// ❌ DANGEREUX (pas utilisé)
String query = "SELECT * FROM users WHERE id = " + userId;

// ✅ SÉCURISÉ (utilisé partout)
@Query("SELECT u FROM User u WHERE u.id = :userId")
User findUserById(@Param("userId") Long userId);
```

**XSS:** ✅ PROTECTED
```java
// Sanitisation automatique
InputSanitizer.sanitizeHtml(userInput);
```

### 4. Validation des Entrées

**Mot de passe:**
```java
@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$")
@Size(min = 8, max = 100)
private String password;
```
- ✅ Min 8 caractères
- ✅ 1 majuscule minimum
- ✅ 1 minuscule minimum
- ✅ 1 chiffre minimum

**Email:**
```java
@Email
@Size(max = 255)
private String email;
```

**Téléphone:**
```java
@Pattern(regexp = "^[+]?[0-9]{8,15}$")
private String numero;
```

### 5. Notifications Email Automatiques

```
Événement                    → Notification
─────────────────────────────────────────────
Client créé                  → Email identifiants
Machine statut changé        → Email mise à jour
Machine TERMINE              → Email prête
```

## 🚀 Utilisation Rapide

### Backend - Sécuriser un Endpoint

```java
@GetMapping("/admin-only")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> adminEndpoint() {
    // Accessible uniquement par ADMIN
    return ResponseEntity.ok(data);
}
```

### Frontend - Protéger une Route

```tsx
// Option 1: Middleware automatique (déjà actif)
// Routes /dashboard/* sont automatiquement protégées

// Option 2: Composant ProtectedRoute
<ProtectedRoute allowedRoles={["ADMIN", "SECRETAIRE"]}>
  <AdminDashboard />
</ProtectedRoute>

// Option 3: Vérification manuelle
import { canCreateClient } from '@/lib/rbac'

if (canCreateClient(user)) {
  // Afficher le bouton créer client
}
```

### Créer un Utilisateur (Backend)

```java
// Le mot de passe est automatiquement haché
UserRequest request = new UserRequest();
request.setPassword("SecurePass123");
// ...
User user = userService.createUser(request);
// Password stored: $2a$10$... (BCrypt hash)
```

### Login (Frontend)

```tsx
const { login } = useAuth()

await login(identifiant, password)
// Cookie auth_token automatiquement défini
// Pas besoin de gérer le token manuellement
```

## 🔍 Vérification de Sécurité

### Checklist Développeur

Avant de déployer du code :

- [ ] Endpoint protégé avec @PreAuthorize ?
- [ ] DTO avec validation (@Valid, @NotBlank, etc.) ?
- [ ] Requête DB utilise JPA/paramètres ?
- [ ] Données utilisateur sanitisées ?
- [ ] Messages d'erreur génériques (pas de fuite info) ?
- [ ] Tests passent ?
- [ ] CodeQL scan effectué ?

### Commandes Utiles

```bash
# Backend - Compiler
cd backend
mvn clean compile

# Backend - Tests
mvn test

# Backend - Scan sécurité
# (via CodeQL dans la PR)

# Frontend - Build
cd client-portal
npm run build

# Frontend - Dev
npm run dev
```

## 📖 Documentation Complète

Pour plus de détails, consultez :

1. **SECURITY_IMPROVEMENTS.md** - Guide technique complet
   - Configuration détaillée
   - Exemples de code
   - Recommandations production

2. **IMPLEMENTATION_SUMMARY.md** - Résumé exécutif
   - Vue d'ensemble
   - Checklist déploiement
   - Résultats tests

## 🆘 Dépannage Rapide

### Cookie auth_token non défini
```
Cause: Cookie.setSecure(true) avec HTTP
Solution: Utiliser HTTPS ou setSecure(false) en dev
```

### 403 Forbidden sur endpoint
```
Cause: Rôle insuffisant
Vérifier: @PreAuthorize et rôle utilisateur
```

### Validation échoue
```
Cause: Contrainte DTO non respectée
Vérifier: Messages d'erreur dans la réponse
```

### Email non envoyé
```
Cause: Configuration SMTP incorrecte
Vérifier: application.properties (mail settings)
```

## 📞 Support

En cas de question :
1. Consulter cette documentation
2. Vérifier les logs (backend: console, frontend: navigateur)
3. Tester en isolation
4. Contacter l'équipe

---

**Version:** 1.0.0  
**Dernière mise à jour:** 2026-01-27  
**Statut:** ✅ PRODUCTION READY
