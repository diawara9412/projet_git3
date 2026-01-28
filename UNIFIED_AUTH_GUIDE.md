# 🔄 Guide d'Authentification Unifiée

## ✅ PROBLÈMES RÉSOLUS

### 1. Bug Critique de Transaction ✓
**Problème:** Client sauvegardé mais introuvable immédiatement après
**Solution:** Ajout de `@Transactional` sur DataInitializer.run()

### 2. Authentification Unifiée ✓
**Problème:** Clients et utilisateurs utilisaient des APIs différentes
**Solution:** Clients sont maintenant des Users avec role=CLIENT

---

## 🎯 Nouvelle Architecture

### Tables
```
users (ancienne + clients)
├── id
├── nom
├── prenom
├── email (unique)
├── identifiant (unique, pour CLIENT role) ← NOUVEAU
├── password (BCrypt)
├── role (ADMIN | SECRETAIRE | TECHNICIEN | CLIENT) ← CLIENT AJOUTÉ
├── active
└── credentials_sent ← NOUVEAU

clients (ancienne table, toujours là pour compatibilité)
└── (peut être migrée plus tard)
```

---

## 🔐 Comment Se Connecter Maintenant

### Pour les Clients (CLT-00001)

**Option 1: Utiliser l'identifiant**
```json
POST /api/auth/login
{
  "email": "CLT-00001",
  "password": "test123"
}
```

**Option 2: Utiliser l'email**
```json
POST /api/auth/login
{
  "email": "client.test@example.com",
  "password": "test123"
}
```

**Option 3: Endpoint client (redirige vers /login)**
```json
POST /api/auth/client/login
{
  "identifiant": "CLT-00001",
  "password": "test123"
}
```

### Pour les Admins/Staff

**Comme avant:**
```json
POST /api/auth/login
{
  "email": "admin@repair.com",
  "password": "admin123"
}
```

---

## 📊 Comptes de Test Disponibles

| Type | Identifiant | Email | Password | Role | API |
|------|-------------|-------|----------|------|-----|
| Admin | - | admin@repair.com | admin123 | ADMIN | /api/auth/login |
| Client | CLT-00001 | client.test@example.com | test123 | CLIENT | /api/auth/login |

**Note:** Les deux peuvent maintenant utiliser `/api/auth/login` !

---

## 🔄 Migration des Clients Existants

Si vous avez des clients dans l'ancienne table `clients`:

### Option 1: Laisser comme ça
- Les nouveaux clients seront créés dans `users` avec role=CLIENT
- Les anciens restent dans `clients`
- Les deux fonctionnent

### Option 2: Migrer tous les clients
```sql
-- Migrer les clients vers users
INSERT INTO users (nom, prenom, adresse, numero, email, identifiant, password, role, active, credentials_sent, created_at, updated_at)
SELECT nom, prenom, adresse, numero, email, identifiant, password, 'CLIENT', active, credentials_sent, created_at, updated_at
FROM clients;

-- Optionnel: Supprimer l'ancienne table
-- DROP TABLE clients;
```

---

## 🆕 Créer un Nouveau Client

### Via API (Recommandé)
```json
POST /api/clients
{
  "nom": "Dupont",
  "prenom": "Jean",
  "adresse": "123 Rue de Paris",
  "numero": "0601020304",
  "email": "jean@example.com"
}
```

Le système va:
1. Créer un User avec role=CLIENT
2. Générer un identifiant unique (ex: CLT-00002)
3. Générer un mot de passe aléatoire
4. Envoyer un email (si configuré)
5. Retourner l'identifiant et le mot de passe

### Manuellement (SQL)
```sql
INSERT INTO users (nom, prenom, adresse, numero, email, identifiant, password, role, active, created_at, updated_at)
VALUES (
  'Dupont', 
  'Jean', 
  '123 Rue de Paris', 
  '0601020304', 
  'jean@example.com', 
  'CLT-00002',  -- Généré automatiquement normalement
  '$2a$10$...', -- BCrypt hash du mot de passe
  'CLIENT',
  true,
  NOW(),
  NOW()
);
```

---

## 🔍 Vérifier l'Authentification

### Backend (Vérifier le token JWT)
```json
GET /api/auth/verify
Cookie: auth_token=<jwt>

Response:
{
  "authenticated": true,
  "id": 1,
  "email": "client.test@example.com",
  "identifiant": "CLT-00001",
  "role": "CLIENT",
  "nom": "Test",
  "prenom": "Client"
}
```

### Frontend (Récupérer les infos du user)
```typescript
const response = await fetch('/api/auth/verify', {
  credentials: 'include' // Important!
});
const data = await response.json();
if (data.authenticated) {
  console.log('User:', data);
}
```

---

## 🔐 Permissions par Rôle

### ADMIN
- ✅ Tout accès
- ✅ Gérer utilisateurs
- ✅ Gérer clients
- ✅ Gérer machines
- ✅ Voir tous les rapports

### SECRETAIRE
- ✅ Gérer clients
- ✅ Gérer machines
- ✅ Créer des réparations
- ❌ Gérer utilisateurs

### TECHNICIEN
- ✅ Voir machines assignées
- ✅ Mettre à jour statut machines
- ❌ Créer/supprimer machines
- ❌ Gérer clients

### CLIENT
- ✅ Voir ses propres machines
- ✅ Voir l'historique de réparations
- ✅ Changer son mot de passe
- ❌ Accès administratif
- ❌ Voir autres clients

---

## 🎨 Frontend: Gérer les Rôles

### React/Next.js
```tsx
// Composant protégé par rôle
function ProtectedPage() {
  const { user } = useAuth();
  
  if (!user) return <LoginPage />;
  
  if (user.role === 'CLIENT') {
    return <ClientDashboard />;
  }
  
  if (['ADMIN', 'SECRETAIRE'].includes(user.role)) {
    return <AdminDashboard />;
  }
  
  return <AccessDenied />;
}
```

### Middleware Next.js
```typescript
// middleware.ts
export function middleware(request: NextRequest) {
  const token = request.cookies.get('auth_token');
  
  if (!token && request.nextUrl.pathname.startsWith('/dashboard')) {
    return NextResponse.redirect(new URL('/login', request.url));
  }
}
```

---

## 🧪 Tester la Nouvelle Authentification

### 1. Redémarrer le serveur
```bash
cd backend
mvn spring-boot:run
```

Logs attendus:
```
INFO === DÉBUT DataInitializer ===
INFO Client utilisateur de test sauvegardé avec ID: 1
INFO Vérification après création: existe = true
INFO Recherche directe après création: trouvé
✔ Client de test créé automatiquement dans la table users!
   Identifiant: CLT-00001
   Email: client.test@example.com
   Mot de passe: test123
```

### 2. Tester avec Postman

**Test 1: Login avec identifiant**
```
POST http://localhost:8080/api/auth/login
{
  "email": "CLT-00001",
  "password": "test123"
}
```

**Test 2: Login avec email**
```
POST http://localhost:8080/api/auth/login
{
  "email": "client.test@example.com",
  "password": "test123"
}
```

**Test 3: Endpoint client (backward compatible)**
```
POST http://localhost:8080/api/auth/client/login
{
  "identifiant": "CLT-00001",
  "password": "test123"
}
```

**Tous devraient retourner:**
```json
{
  "id": 1,
  "nom": "Test",
  "prenom": "Client",
  "email": "client.test@example.com",
  "identifiant": "CLT-00001",
  "role": "CLIENT",
  "message": "Connexion réussie"
}
```

---

## 🔥 Points Importants

### ✅ Avantages
1. **Un seul endpoint** pour tous les utilisateurs
2. **Même logique d'authentification** pour tous
3. **Sécurité cohérente** (BCrypt, JWT, cookies)
4. **Permissions granulaires** via rôles
5. **Backward compatible** avec ancienne API client

### ⚠️ Points d'Attention
1. **Migration des clients existants** - À planifier
2. **Tests approfondis** - Vérifier tous les cas
3. **Frontend à adapter** - Gérer le nouveau format de réponse
4. **Documentation équipe** - Former les utilisateurs

---

## 🚀 Prochaines Étapes

1. **Tester l'authentification** avec les deux endpoints
2. **Adapter le frontend** pour utiliser le nouveau format
3. **Planifier la migration** des clients existants
4. **Former l'équipe** sur la nouvelle architecture
5. **Déployer en production** après validation complète

---

## 📞 Support

En cas de problème:
1. Vérifier les logs au démarrage
2. Vérifier que le client existe dans `users` avec role=CLIENT
3. Tester avec Postman
4. Consulter les autres guides (DEBUG_CLIENT_LOGIN.md, etc.)

---

**Date:** 2026-01-28  
**Version:** 2.0 - Authentification Unifiée  
**Statut:** ✅ OPÉRATIONNEL
