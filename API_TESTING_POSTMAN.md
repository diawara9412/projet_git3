# Guide de Test API avec Postman

## 🔐 Connexion Client

### Endpoint de Connexion Client

**URL:** `http://localhost:8080/api/auth/client/login`

**Méthode:** `POST`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "identifiant": "CLT-00001",
  "password": "votre_mot_de_passe"
}
```

OU avec email:
```json
{
  "identifiant": "client@example.com",
  "password": "votre_mot_de_passe"
}
```

### Réponse Succès (200 OK)

```json
{
  "id": 1,
  "identifiant": "CLT-00001",
  "nom": "Doe",
  "prenom": "John",
  "email": "john.doe@example.com",
  "role": "CLIENT",
  "message": "Connexion réussie"
}
```

**Note:** Le token JWT est automatiquement stocké dans un cookie HttpOnly nommé `auth_token`.

### Réponse Erreur (400 Bad Request)

```json
{
  "error": "Identifiant ou mot de passe incorrect"
}
```

---

## 📋 Configuration Postman Complète

### 1. Créer une nouvelle requête

1. Ouvrir Postman
2. Cliquer sur "New" → "HTTP Request"
3. Nommer la requête: "Client Login"

### 2. Configurer la requête

**Method:** Sélectionner `POST`

**URL:** 
```
http://localhost:8080/api/auth/client/login
```

**Headers:**
- Cliquer sur l'onglet "Headers"
- Ajouter:
  - Key: `Content-Type`
  - Value: `application/json`

**Body:**
- Cliquer sur l'onglet "Body"
- Sélectionner "raw"
- Sélectionner "JSON" dans le menu déroulant
- Coller:
```json
{
  "identifiant": "CLT-00001",
  "password": "votre_mot_de_passe"
}
```

### 3. Gérer les Cookies

**Important:** Pour tester avec les cookies:

1. Dans Postman, aller dans "Settings" (icône ⚙️)
2. Chercher "Cookie" ou "Interceptor"
3. Activer "Automatically follow redirects"
4. Les cookies seront automatiquement gérés par Postman

Pour voir les cookies après la connexion:
1. Envoyer la requête
2. Dans la réponse, cliquer sur "Cookies"
3. Vous devriez voir `auth_token` avec le JWT

---

## 🔑 Autres Endpoints Disponibles

### 1. Connexion Admin/Staff (machinemanagementapp)

**URL:** `http://localhost:8080/api/auth/login`

**Body:**
```json
{
  "email": "admin@example.com",
  "password": "admin_password"
}
```

### 2. Vérifier l'Authentification

**URL:** `http://localhost:8080/api/auth/verify`

**Méthode:** `GET`

**Note:** Les cookies doivent être envoyés automatiquement.

**Réponse:**
```json
{
  "authenticated": true,
  "id": 1,
  "role": "CLIENT",
  "email": "client@example.com",
  "identifiant": "CLT-00001"
}
```

### 3. Déconnexion

**URL:** `http://localhost:8080/api/auth/logout`

**Méthode:** `POST`

**Réponse:**
```json
{
  "message": "Déconnexion réussie"
}
```

### 4. Récupérer les Machines du Client

**URL:** `http://localhost:8080/api/auth/client/{clientId}/machines`

**Méthode:** `GET`

**Exemple:** `http://localhost:8080/api/auth/client/1/machines`

**Note:** Nécessite d'être authentifié (cookie auth_token).

### 5. Changer le Mot de Passe

**URL:** `http://localhost:8080/api/auth/client/{clientId}/change-password`

**Méthode:** `POST`

**Body:**
```json
{
  "oldPassword": "ancien_mot_de_passe",
  "newPassword": "Nouveau123",
  "confirmPassword": "Nouveau123"
}
```

---

## 🧪 Scénario de Test Complet

### Test 1: Connexion avec Identifiant

```
POST http://localhost:8080/api/auth/client/login

Body:
{
  "identifiant": "CLT-00001",
  "password": "MotDePasse123"
}

Résultat attendu: 200 OK + Cookie auth_token défini
```

### Test 2: Connexion avec Email

```
POST http://localhost:8080/api/auth/client/login

Body:
{
  "identifiant": "client@example.com",
  "password": "MotDePasse123"
}

Résultat attendu: 200 OK + Cookie auth_token défini
```

### Test 3: Connexion Échouée (Mauvais Mot de Passe)

```
POST http://localhost:8080/api/auth/client/login

Body:
{
  "identifiant": "CLT-00001",
  "password": "mauvais_password"
}

Résultat attendu: 400 Bad Request
{
  "error": "Identifiant ou mot de passe incorrect"
}
```

### Test 4: Validation des Champs

```
POST http://localhost:8080/api/auth/client/login

Body:
{
  "identifiant": "",
  "password": ""
}

Résultat attendu: 400 Bad Request avec détails de validation
```

### Test 5: Vérifier l'Authentification

```
GET http://localhost:8080/api/auth/verify

Note: Le cookie auth_token doit être envoyé automatiquement

Résultat attendu: 200 OK
{
  "authenticated": true,
  "id": 1,
  "role": "CLIENT",
  ...
}
```

---

## 🚨 Dépannage

### Problème: "Connection refused"

**Solution:** Vérifier que le serveur backend est démarré.

```bash
cd backend
mvn spring-boot:run
```

Le serveur devrait démarrer sur le port 8080.

### Problème: Cookie non défini

**Solution:** 
1. Vérifier que Postman accepte les cookies (Settings)
2. Vérifier la réponse dans l'onglet "Cookies" de Postman
3. Le cookie `auth_token` devrait être HttpOnly

### Problème: CORS Error

**Solution:** Le serveur est configuré pour accepter les requêtes de localhost. Si vous utilisez un autre origine, ajoutez-le dans `SecurityConfig.java`.

### Problème: 401 Unauthorized sur endpoints protégés

**Solution:** 
1. Assurez-vous d'avoir fait la connexion d'abord
2. Vérifiez que le cookie `auth_token` est présent
3. Postman devrait envoyer automatiquement le cookie

---

## 📝 Notes Importantes

1. **Cookie HttpOnly:** Le token JWT est stocké dans un cookie HttpOnly pour la sécurité. Vous ne pouvez pas le lire avec JavaScript côté client.

2. **Expiration:** Le token expire après 24 heures (86400000 ms).

3. **SameSite:** Le cookie a l'attribut `SameSite=Lax` pour la protection CSRF.

4. **CORS:** Les origines autorisées sont:
   - http://localhost:3000
   - http://localhost:3001
   - http://localhost:5173
   - http://localhost:5174

5. **Rôles:** Le système supporte 4 rôles:
   - CLIENT
   - TECHNICIEN
   - SECRETAIRE
   - ADMIN

---

## 🔗 Collection Postman

Pour créer une collection Postman complète:

1. Créer une nouvelle collection "Machine Repair API"
2. Ajouter toutes les requêtes ci-dessus
3. Configurer une variable d'environnement `baseUrl` = `http://localhost:8080`
4. Utiliser `{{baseUrl}}/api/auth/client/login` dans les URLs

---

**Date:** 2026-01-27  
**Version API:** 1.0.0  
**Port:** 8080
