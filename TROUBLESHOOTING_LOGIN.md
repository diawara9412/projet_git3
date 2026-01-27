# Guide de Dépannage - Problèmes de Connexion

## 🔴 Erreur: "Identifiant ou mot de passe incorrect"

Cette erreur peut avoir plusieurs causes. Suivez ce guide étape par étape.

---

## 📋 Diagnostic Rapide

### Étape 1: Vérifier que le serveur backend est démarré

```bash
cd backend
mvn spring-boot:run
```

Le serveur doit afficher au démarrage:
```
✔ Administrateur créé automatiquement !
   Email: admin@repair.com
   Mot de passe: admin123
   
✔ Client de test créé automatiquement !
   Identifiant: CLT-00001
   Email: client.test@example.com
   Mot de passe: test123
```

### Étape 2: Vérifier les logs

Les logs du serveur vous indiquent la cause exacte:

**Si vous voyez:**
```
Client non trouvé avec identifiant: CLT-00001
```
→ Le client n'existe pas dans la base de données

**Si vous voyez:**
```
Client trouvé: CLT-00001 (client.test@example.com)
Mot de passe incorrect pour le client: CLT-00001
```
→ Le mot de passe est incorrect

**Si vous voyez:**
```
Tentative de connexion sur compte désactivé: CLT-00001
```
→ Le compte a été désactivé

---

## 🔧 Solutions selon le problème

### Problème 1: Client n'existe pas

**Cause:** Le client CLT-00001 n'est pas créé dans la base de données

**Solutions:**

#### Solution A: Utiliser le client de test par défaut

Après le redémarrage du serveur, utilisez:
- **Identifiant:** `CLT-00001`
- **Mot de passe:** `test123`

#### Solution B: Créer un nouveau client via l'API Admin

1. Se connecter en tant qu'admin:
```bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "admin@repair.com",
  "password": "admin123"
}
```

2. Créer un client:
```bash
POST http://localhost:8080/api/clients
Content-Type: application/json

{
  "nom": "Dupont",
  "prenom": "Jean",
  "adresse": "123 Rue de Paris",
  "numero": "0601020304",
  "email": "jean.dupont@example.com",
  "sendCredentials": true
}
```

Le système va:
- Générer un identifiant unique (ex: CLT-00002)
- Générer un mot de passe aléatoire
- Envoyer un email avec les identifiants

#### Solution C: Vérifier la base de données

```sql
-- Se connecter à MySQL
mysql -u root -p

-- Utiliser la base de données
USE machine_repair_db;

-- Voir tous les clients
SELECT id, identifiant, nom, prenom, email, active FROM clients;

-- Vérifier un client spécifique
SELECT * FROM clients WHERE identifiant = 'CLT-00001';
```

---

### Problème 2: Mot de passe incorrect

**Cause:** Le mot de passe que vous utilisez ne correspond pas à celui en base

**Solutions:**

#### Solution A: Utiliser le bon mot de passe

- Si c'est le client de test: utilisez `test123`
- Si vous avez reçu un email: utilisez le mot de passe de l'email
- Le mot de passe est sensible à la casse (majuscules/minuscules)

#### Solution B: Réinitialiser le mot de passe (via admin)

```bash
POST http://localhost:8080/api/clients/{clientId}/resend-credentials
```

Cela va générer un nouveau mot de passe et l'envoyer par email.

#### Solution C: Changer le mot de passe manuellement en base

```sql
-- Générer un hash BCrypt pour "test123"
-- Hash: $2a$10$... (utilisez un outil en ligne ou le code Java)

UPDATE clients 
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMye/1YP6Z0jXBnvW0Lg7xJ3fV2iBm8HLWW'
WHERE identifiant = 'CLT-00001';
```

**⚠️ Note:** Le hash ci-dessus correspond au mot de passe `test123`.

---

### Problème 3: Compte désactivé

**Cause:** Le champ `active` est à `false` dans la base de données

**Solution:**

Réactiver le compte via admin:
```bash
POST http://localhost:8080/api/clients/{clientId}/toggle-status
```

Ou en SQL:
```sql
UPDATE clients SET active = true WHERE identifiant = 'CLT-00001';
```

---

## 🔍 Cas Spécifique: Mot de passe reçu par email (6McMkX5j5U)

Si vous avez reçu le mot de passe `6McMkX5j5U` par email mais qu'il ne fonctionne pas:

### Vérifications:

1. **Le client existe-t-il?**
   - Vérifiez dans les logs au démarrage
   - Ou vérifiez en base de données

2. **Le mot de passe a-t-il été haché?**
   - Quand un client est créé, le mot de passe en clair est envoyé par email
   - Mais en base, il est stocké haché avec BCrypt
   - Le serveur doit comparer le mot de passe en clair avec le hash

3. **Y a-t-il eu une erreur lors de la création?**
   - Vérifiez les logs du serveur lors de la création du client
   - L'email a-t-il bien été envoyé?

### Test de diagnostic:

Créez un nouveau client de test pour voir si le problème persiste:

```bash
POST http://localhost:8080/api/clients
Content-Type: application/json

{
  "nom": "Test",
  "prenom": "Nouveau",
  "adresse": "Test",
  "numero": "0699999999",
  "email": "nouveau.test@example.com"
}
```

Puis essayez de vous connecter avec les identifiants reçus.

---

## 📧 Configuration Email

Si les emails ne sont pas envoyés (et donc vous ne recevez pas le mot de passe):

Vérifier `application.properties`:

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=votre.email@gmail.com
spring.mail.password=votre_app_password
```

**Important:** Pour Gmail, vous devez utiliser un "App Password", pas votre mot de passe normal.

---

## 🧪 Test Complet avec Postman

### 1. Tester la connexion admin

```
POST http://localhost:8080/api/auth/login

{
  "email": "admin@repair.com",
  "password": "admin123"
}
```

✅ Si ça marche → Le serveur et la base fonctionnent

### 2. Créer un client

```
POST http://localhost:8080/api/clients

{
  "nom": "Test",
  "prenom": "Client",
  "adresse": "Test",
  "numero": "0612345678",
  "email": "test@example.com"
}
```

✅ Notez l'identifiant généré (ex: CLT-00002) et le mot de passe dans la réponse (si affiché)

### 3. Tester la connexion client

```
POST http://localhost:8080/api/auth/client/login

{
  "identifiant": "CLT-00001",
  "password": "test123"
}
```

ou

```
POST http://localhost:8080/api/auth/client/login

{
  "identifiant": "client.test@example.com",
  "password": "test123"
}
```

---

## 📞 Support

Si le problème persiste après avoir suivi ce guide:

1. **Vérifiez les logs du serveur** - Ils contiennent des informations détaillées
2. **Vérifiez la base de données** - Assurez-vous que le client existe
3. **Redémarrez le serveur** - Cela recréera les données de test

**Logs importants à vérifier:**
```
Tentative de connexion avec identifiant: ...
Client non trouvé avec identifiant: ...
Client trouvé: ... (...)
Mot de passe incorrect pour le client: ...
Client connecté: ...
```

---

## ✅ Checklist de Dépannage

- [ ] Le serveur backend est démarré
- [ ] MySQL est démarré et la base `machine_repair_db` existe
- [ ] Le client CLT-00001 existe en base (vérifier les logs au démarrage)
- [ ] Le mot de passe utilisé est correct (test123 pour le client de test)
- [ ] Le compte client est actif (active = true)
- [ ] Les logs ne montrent pas d'erreur de connexion à la base
- [ ] La configuration email est correcte (si vous créez des clients)

---

**Dernière mise à jour:** 2026-01-27
