# Améliorations de Sécurité - Machine Repair Management System

Ce document décrit toutes les améliorations de sécurité apportées au système de gestion de réparations de machines.

## Table des matières

- [Backend (Spring Boot)](#backend-spring-boot)
- [Frontend (Next.js Client Portal)](#frontend-nextjs-client-portal)
- [Tests et Validation](#tests-et-validation)
- [Recommandations pour la Production](#recommandations-pour-la-production)

---

## Backend (Spring Boot)

### 1. Authentification et Contrôle d'Accès (RBAC)

#### Annotations @PreAuthorize
Tous les endpoints critiques sont maintenant protégés par des annotations de sécurité au niveau des méthodes :

**UserController** :
- `@PreAuthorize("hasAnyRole('ADMIN', 'SECRETAIRE')")` pour la lecture
- `@PreAuthorize("hasRole('ADMIN')")` pour création, modification, suppression

**MachineController** :
- `@PreAuthorize("isAuthenticated()")` pour consultation
- `@PreAuthorize("hasAnyRole('ADMIN', 'SECRETAIRE')")` pour création
- `@PreAuthorize("hasAnyRole('ADMIN', 'SECRETAIRE', 'TECHNICIEN')")` pour modification
- `@PreAuthorize("hasRole('ADMIN')")` pour suppression

**ClientController** :
- Déjà protégé avec @PreAuthorize

#### Configuration de Sécurité
- Headers de sécurité HTTP ajoutés (Content-Security-Policy, Frame-Options)
- Politique de session STATELESS (JWT)
- Points d'entrée publics limités à `/api/auth/**`

### 2. Protection Anti-SQL Injection

✅ **Statut : SÉCURISÉ**

Toutes les requêtes utilisent :
- JPA avec requêtes paramétrées
- Annotation `@Param` pour les requêtes JPQL personnalisées
- Aucune concaténation de chaînes SQL

**Exemples** :
```java
@Query("SELECT m FROM Machine m WHERE " +
       "LOWER(m.marque) LIKE LOWER(CONCAT('%', :keyword, '%'))")
List<Machine> searchMachines(@Param("keyword") String keyword);
```

### 3. Validation Stricte des Entrées

#### DTOs avec Contraintes Renforcées

**ClientRequest** :
- `@Size(min = 2, max = 100)` pour nom et prénom
- `@Pattern(regexp = "^[+]?[0-9]{8,15}$")` pour numéro de téléphone
- `@Email` et `@Size(max = 255)` pour email

**UserRequest** :
- `@Pattern` pour mot de passe (majuscule, minuscule, chiffre)
- `@Size(min = 8, max = 100)` pour mot de passe

**MachineRequest** :
- `@FutureOrPresent` pour date de rendez-vous
- `@DecimalMin(value = "0.0")` pour montant
- `@Positive` pour IDs

**PasswordChangeRequest** :
- Validation de complexité avec `@Pattern`

#### Gestionnaire Global des Exceptions
`GlobalExceptionHandler` centralise la gestion des erreurs :
- Erreurs de validation (MethodArgumentNotValidException)
- Erreurs d'authentification
- Erreurs d'autorisation (AccessDeniedException)
- Messages d'erreur sécurisés (pas de fuite d'informations)

#### Utilitaire de Sanitisation
`InputSanitizer` fournit des méthodes pour :
- `sanitizeHtml()` : Protection XSS
- `sanitizeSql()` : Nettoyage SQL (backup)
- `sanitizeEmail()` : Validation email
- `sanitizePhoneNumber()` : Validation téléphone
- `sanitizeUrl()` : Protection contre URLs malveillantes
- `truncate()` : Limitation de longueur

### 4. Hachage des Mots de Passe

✅ **Statut : SÉCURISÉ**

- `BCryptPasswordEncoder` utilisé partout
- Hash avant stockage dans `UserService` et `ClientService`
- Validation avec `passwordEncoder.matches()` pour l'authentification
- Génération sécurisée de mots de passe avec `SecureRandom`

### 5. Sécurité JWT

#### Token Provider
- Clé de signature HMAC-SHA512
- Expiration configurée (24h par défaut)
- Validation stricte avec gestion des exceptions :
  - SignatureException
  - MalformedJwtException
  - ExpiredJwtException
  - UnsupportedJwtException

#### Cookies Sécurisés
- `HttpOnly=true` (protection XSS)
- `SameSite=Lax` (protection CSRF)
- `Secure=false` (dev) → **À activer en production avec HTTPS**
- `Path=/` pour accessibilité globale
- `MaxAge=86400` (24h)

### 6. Notifications Email

#### EmailService Amélioré
- Envoi d'identifiants lors création de compte client
- **NOUVEAU** : Notifications automatiques lors changement de statut de machine
- **NOUVEAU** : Notification spéciale quand machine prête à récupérer
- Templates Thymeleaf pour emails HTML
- Envoi asynchrone avec `@Async`

#### MachineService
- Détection automatique des changements de statut
- Envoi email si statut modifié
- Gestion des erreurs sans bloquer l'opération

### 7. Headers de Sécurité HTTP

```java
.headers(headers -> headers
    .contentSecurityPolicy(csp -> csp
        .policyDirectives("default-src 'self'; frame-ancestors 'none'"))
    .frameOptions(frame -> frame.deny())
)
```

- **Content-Security-Policy** : Empêche le chargement de ressources non autorisées
- **Frame-Options: DENY** : Protection contre clickjacking

---

## Frontend (Next.js Client Portal)

### 1. Route Guards (Middleware Next.js)

**middleware.ts** :
- Vérification automatique du cookie `auth_token`
- Redirection vers login si non authentifié sur routes protégées
- Redirection vers dashboard si déjà authentifié sur page login
- Matcher pour toutes les routes sauf API, static, images

### 2. Composant ProtectedRoute

```tsx
<ProtectedRoute allowedRoles={["ADMIN", "SECRETAIRE"]}>
  <AdminContent />
</ProtectedRoute>
```

- Vérifie l'authentification
- Vérifie les rôles autorisés
- Affiche loader pendant vérification
- Redirection automatique si non autorisé

### 3. Utilitaires RBAC (rbac.ts)

Fonctions helper pour contrôle d'accès :
- `hasRole()` : Vérifier un rôle spécifique
- `hasAnyRole()` : Vérifier parmi plusieurs rôles
- `isAdmin()`, `isClient()`, `isSecretaire()`, `isTechnicien()`
- `canCreateClient()`, `canUpdateClient()`, `canDeleteClient()`
- `canCreateMachine()`, `canUpdateMachine()`, `canDeleteMachine()`
- Et plus...

### 4. Appels API Sécurisés

**api.ts** :
```typescript
const response = await fetch(`${API_URL}${endpoint}`, {
  ...options,
  credentials: "include", // Toujours inclure cookies
})
```

- Tous les appels incluent `credentials: "include"`
- Cookies HttpOnly envoyés automatiquement
- Gestion centralisée des erreurs

### 5. Stockage Sécurisé

✅ **Statut : SÉCURISÉ**

- **AUCUN** token en localStorage ou sessionStorage
- Tokens uniquement dans cookies HttpOnly
- Protection contre vol de token via XSS
- État utilisateur géré dans contexte React

### 6. AuthContext Amélioré

- Validation côté client avant envoi (identifiant, password requis)
- Vérification automatique de l'auth au chargement
- Rafraîchissement de l'état avec `refreshAuth()`
- Typage strict avec TypeScript et Role type

---

## Tests et Validation

### Tests Backend
```
Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
✓ UserServiceTest (7 tests)
✓ UserEntityTest (3 tests)
✓ ClientEntityTest (2 tests)
✓ MachineEntityTest (3 tests)
```

### Compilation
✅ Backend compile sans erreurs avec Java 17

---

## Recommandations pour la Production

### Backend

1. **Activer HTTPS et Cookies Sécurisés**
   ```java
   cookie.setSecure(true); // Dans AuthController
   ```

2. **Variables d'Environnement**
   - `JWT_SECRET` : Utiliser une clé robuste de 256+ bits
   - `DB_PASSWORD` : Ne pas laisser vide
   - `MAIL_PASSWORD` : Utiliser App Password Gmail

3. **Base de Données**
   ```properties
   spring.datasource.url=jdbc:mysql://host:3306/db?useSSL=true
   spring.jpa.hibernate.ddl-auto=validate # Pas update en prod
   ```

4. **Logging**
   - Activer les logs de sécurité
   - Monitorer les tentatives d'authentification échouées
   - Alertes sur activités suspectes

5. **Rate Limiting**
   - Implémenter limitation de requêtes sur endpoints login
   - Protection contre attaques par force brute

### Frontend

1. **Variables d'Environnement**
   ```
   NEXT_PUBLIC_API_URL=https://api.production.com
   ```

2. **Build Optimization**
   ```bash
   npm run build
   npm start
   ```

3. **Headers de Sécurité (next.config.js)**
   ```javascript
   async headers() {
     return [
       {
         source: '/:path*',
         headers: [
           { key: 'X-Frame-Options', value: 'DENY' },
           { key: 'X-Content-Type-Options', value: 'nosniff' },
         ],
       },
     ]
   }
   ```

---

## Checklist de Déploiement

### Avant la Production

- [ ] Activer HTTPS sur tous les services
- [ ] Changer `cookie.setSecure(true)`
- [ ] Générer nouveau JWT_SECRET
- [ ] Configurer mots de passe DB robustes
- [ ] Configurer CORS pour domaines production uniquement
- [ ] Activer `spring.jpa.hibernate.ddl-auto=validate`
- [ ] Configurer logs de sécurité
- [ ] Tester tous les flux d'authentification
- [ ] Vérifier envoi emails
- [ ] Scanner dépendances (OWASP, Snyk)
- [ ] Effectuer tests de pénétration
- [ ] Configurer backups automatiques
- [ ] Documenter procédures incident

---

## Résumé des Fichiers Modifiés/Créés

### Backend
- ✨ `exception/GlobalExceptionHandler.java` (nouveau)
- ✨ `util/InputSanitizer.java` (nouveau)
- ♻️ `config/SecurityConfig.java` (amélioré)
- ♻️ `controller/UserController.java` (@PreAuthorize)
- ♻️ `controller/MachineController.java` (@PreAuthorize)
- ♻️ `controller/AuthController.java` (cookies)
- ♻️ `service/MachineService.java` (notifications)
- ♻️ `dto/ClientRequest.java` (validation)
- ♻️ `dto/UserRequest.java` (validation)
- ♻️ `dto/MachineRequest.java` (validation)
- ♻️ `dto/PasswordChangeRequest.java` (validation)
- ♻️ `pom.xml` (Java 17)

### Frontend
- ✨ `middleware.ts` (nouveau)
- ✨ `lib/rbac.ts` (nouveau)
- ✨ `components/protected-route.tsx` (nouveau)
- ♻️ `lib/auth-context.tsx` (validation, types)

---

## Support et Maintenance

Pour toute question sur ces améliorations de sécurité :
1. Consulter ce document
2. Vérifier les commentaires dans le code
3. Tester en environnement de développement
4. Contacter l'équipe de sécurité

**Dernière mise à jour** : 2026-01-27
**Version** : 1.0.0
