# 🔐 Guide Rapide - Connexion au Système

## 📍 Informations Importantes

### ⚠️ PROBLÈME COURANT
Si vous voyez l'erreur **"Identifiant ou mot de passe incorrect"**, consultez:
- **SOLUTION_RAPIDE.md** - Solution immédiate
- **TROUBLESHOOTING_LOGIN.md** - Guide de dépannage complet

---

## 🎯 Accès Rapide

### Au Premier Démarrage

Quand vous démarrez le serveur backend, deux comptes sont créés automatiquement:

```bash
cd backend
mvn spring-boot:run
```

**Console affiche:**
```
✔ Administrateur créé automatiquement !
   Email: admin@repair.com
   Mot de passe: admin123
   
✔ Client de test créé automatiquement !
   Identifiant: CLT-00001
   Email: client.test@example.com
   Mot de passe: test123
```

---

## 🔑 Comptes Disponibles

### 1. Compte Administrateur

**Pour:** Gérer les clients, machines, utilisateurs

**Connexion:**
```
URL: POST http://localhost:8080/api/auth/login

Body:
{
  "email": "admin@repair.com",
  "password": "admin123"
}
```

**Permet de:**
- Créer/modifier/supprimer des clients
- Créer/modifier/supprimer des machines
- Créer/modifier/supprimer des utilisateurs (staff)
- Voir toutes les données

---

### 2. Compte Client de Test

**Pour:** Tester le portail client (client-portal)

**Connexion:**
```
URL: POST http://localhost:8080/api/auth/client/login

Body:
{
  "identifiant": "CLT-00001",
  "password": "test123"
}

OU avec email:
{
  "identifiant": "client.test@example.com",
  "password": "test123"
}
```

**Permet de:**
- Voir ses propres machines
- Changer son mot de passe
- Consulter l'état de ses réparations

---

## 📖 Documentation Complète

### Guides Disponibles

1. **API_TESTING_POSTMAN.md**
   - Comment tester avec Postman
   - Tous les endpoints disponibles
   - Collection Postman à importer

2. **SOLUTION_RAPIDE.md**
   - Solution immédiate au problème de connexion
   - Étapes rapides pour se connecter

3. **TROUBLESHOOTING_LOGIN.md**
   - Guide complet de dépannage
   - Diagnostic étape par étape
   - Solutions pour tous les cas d'erreur

4. **SECURITY_IMPROVEMENTS.md**
   - Détails techniques de sécurité
   - Améliorations implémentées

5. **QUICK_START_SECURITY.md**
   - Guide de sécurité rapide
   - Diagrammes et exemples

---

## 🚀 Utilisation Typique

### Scénario 1: Tester le Portail Client

```bash
# 1. Démarrer le serveur
cd backend
mvn spring-boot:run

# 2. Dans Postman, se connecter comme client
POST http://localhost:8080/api/auth/client/login
{
  "identifiant": "CLT-00001",
  "password": "test123"
}

# 3. Récupérer ses machines
GET http://localhost:8080/api/auth/client/1/machines
```

### Scénario 2: Créer un Nouveau Client

```bash
# 1. Se connecter en admin
POST http://localhost:8080/api/auth/login
{
  "email": "admin@repair.com",
  "password": "admin123"
}

# 2. Créer un client
POST http://localhost:8080/api/clients
{
  "nom": "Dupont",
  "prenom": "Jean",
  "adresse": "123 Rue de Paris",
  "numero": "0601020304",
  "email": "jean.dupont@example.com"
}

# Réponse: Le système génère automatiquement:
# - Un identifiant (ex: CLT-00002)
# - Un mot de passe aléatoire
# - Et envoie un email (si configuré)
```

### Scénario 3: Résoudre un Problème de Connexion

```bash
# 1. Vérifier les logs du serveur
# Rechercher les messages:
# - "Client non trouvé..."
# - "Mot de passe incorrect..."
# - "Compte désactivé..."

# 2. Vérifier la base de données
mysql -u root -p
USE machine_repair_db;
SELECT id, identifiant, nom, email, active FROM clients;

# 3. Si le client n'existe pas, le créer via admin
# Ou utiliser le client de test (CLT-00001 / test123)
```

---

## 🔧 Configuration

### Base de Données

Le système utilise MySQL. Configuration dans `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/machine_repair_db
spring.datasource.username=root
spring.datasource.password=
```

**Important:** MySQL doit être démarré et la base `machine_repair_db` sera créée automatiquement.

### Email (Optionnel)

Pour envoyer des emails avec les identifiants:

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=votre.email@gmail.com
spring.mail.password=votre_app_password
```

**Note:** Pour Gmail, utilisez un "App Password" (pas votre mot de passe normal).

---

## ❓ FAQ

**Q: J'ai l'erreur "Identifiant ou mot de passe incorrect"**
A: Consultez SOLUTION_RAPIDE.md pour la solution

**Q: Le mot de passe envoyé par email ne fonctionne pas**
A: Vérifiez que le client existe et que l'email a bien été envoyé (logs du serveur)

**Q: Comment créer un nouveau client?**
A: Connectez-vous en admin et utilisez POST /api/clients

**Q: Où sont stockés les mots de passe?**
A: Dans la base de données, hashés avec BCrypt (sécurisé)

**Q: Comment changer un mot de passe?**
A: Utilisez POST /api/auth/client/{id}/change-password

**Q: Les cookies ne fonctionnent pas**
A: Assurez-vous que Postman accepte les cookies (Settings)

**Q: Comment voir tous les clients?**
A: Connectez-vous en admin et utilisez GET /api/clients

---

## 📞 Support

Si vous avez des problèmes:

1. **Vérifiez les logs du serveur** - Ils sont très détaillés maintenant
2. **Consultez TROUBLESHOOTING_LOGIN.md** - Solutions pour tous les cas
3. **Vérifiez la base de données** - Le client existe-t-il?
4. **Redémarrez le serveur** - Les données de test seront recréées

---

## ✅ Checklist de Démarrage

- [ ] MySQL est installé et démarré
- [ ] Backend est démarré (`mvn spring-boot:run`)
- [ ] Les messages de création de comptes apparaissent dans la console
- [ ] Connexion admin testée (admin@repair.com / admin123)
- [ ] Connexion client testée (CLT-00001 / test123)
- [ ] Postman configuré avec la collection importée

---

**Dernière mise à jour:** 2026-01-27  
**Version:** 1.0.0  
**Statut:** ✅ Opérationnel avec comptes de test
