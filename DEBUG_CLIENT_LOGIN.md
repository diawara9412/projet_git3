# 🔍 Debug Guide - Client Login Issues

## 🔴 Problème Observé

**Symptôme:**
- Les logs montrent que le client CLT-00001 est créé au démarrage ✓
- Mais lors de la connexion, l'erreur "Client non trouvé" apparaît ✗

**Logs d'exemple:**
```
✔ Client de test créé automatiquement !
   Identifiant: CLT-00001
...
WARN  Client non trouvé avec identifiant: CLT-00001
```

---

## 🛠️ Corrections Apportées

### 1. Requête @Query Explicite

**Problème:** La méthode `findByEmailOrIdentifiant` utilisait une requête dérivée Spring Data JPA qui pourrait ne pas fonctionner correctement.

**Solution:** Remplacée par une requête JPQL explicite:

```java
@Query("SELECT c FROM Client c WHERE c.email = :login OR c.identifiant = :login")
Optional<Client> findByEmailOrIdentifiant(@Param("login") String email, @Param("login") String identifiant);
```

### 2. Flush Explicite après Save

**Problème:** Le client pourrait ne pas être immédiatement disponible en lecture après `save()`.

**Solution:** Ajout de `flush()` pour forcer la persistence:

```java
Client saved = clientRepository.save(testClient);
clientRepository.flush(); // Force la persistence
```

### 3. Logs de Debug Améliorés

**Ajout de logs détaillés:**
- Longueur de l'identifiant
- Test de recherche par identifiant seul
- Test de recherche par email seul
- Nombre total de clients dans la base

Cela permet de diagnostiquer exactement où le problème se situe.

### 4. Trim des Espaces Blancs

**Problème:** Des espaces blancs invisibles dans l'identifiant pourraient causer l'échec.

**Solution:** 
```java
String loginValue = request.getIdentifiant().trim();
```

---

## 🧪 Comment Tester

### 1. Redémarrer le Serveur

```bash
cd backend
mvn spring-boot:run
```

**Vérifier les logs au démarrage:**
```
✔ Client de test créé automatiquement !
   Identifiant: CLT-00001
   Email: client.test@example.com
   Mot de passe: test123
INFO  Client de test sauvegardé avec ID: 1
INFO  Vérification: Client CLT-00001 existe = true
```

### 2. Tester la Connexion

**Dans Postman:**
```json
POST http://localhost:8080/api/auth/client/login

{
  "identifiant": "CLT-00001",
  "password": "test123"
}
```

### 3. Analyser les Logs de Debug

Si la connexion échoue, vous verrez maintenant des logs détaillés:

```
DEBUG Tentative de connexion avec identifiant: 'CLT-00001'
DEBUG Longueur de l'identifiant: 9
WARN  Client non trouvé avec identifiant: 'CLT-00001'
WARN  Recherche par identifiant seul: trouvé/non trouvé
WARN  Recherche par email seul: trouvé/non trouvé
WARN  Nombre total de clients dans la base: X
```

---

## 🔍 Diagnostic selon les Logs

### Cas 1: "Client de test sauvegardé" mais "existe = false"

**Cause:** Problème de transaction ou de flush

**Solution:** 
- Vérifier que la base de données est correctement configurée
- Vérifier que MySQL est démarré
- Vérifier `spring.jpa.hibernate.ddl-auto` dans application.properties

### Cas 2: "Recherche par identifiant seul: trouvé" mais "findByEmailOrIdentifiant" échoue

**Cause:** Problème avec la requête @Query

**Solution:** Déjà corrigée avec la requête JPQL explicite

### Cas 3: "Nombre total de clients: 0"

**Cause:** Le client n'a jamais été sauvegardé ou la base a été réinitialisée

**Solution:**
- Vérifier la configuration de la base de données
- Vérifier qu'il n'y a pas d'exceptions au démarrage
- Essayer de redémarrer MySQL et le serveur

### Cas 4: "Longueur de l'identifiant: X" (où X ≠ 9)

**Cause:** Espaces blancs ou caractères invisibles dans l'identifiant

**Solution:** Déjà corrigée avec `.trim()`

---

## 🗃️ Vérifier Directement la Base de Données

### MySQL Console

```sql
-- Se connecter
mysql -u root -p

-- Utiliser la base
USE machine_repair_db;

-- Voir tous les clients
SELECT id, identifiant, nom, prenom, email, active 
FROM clients;

-- Vérifier CLT-00001 spécifiquement
SELECT * FROM clients WHERE identifiant = 'CLT-00001';

-- Compter les clients
SELECT COUNT(*) FROM clients;
```

### Ce qu'on devrait voir:

```
+----+-------------+------+---------+-------------------------+--------+
| id | identifiant | nom  | prenom  | email                   | active |
+----+-------------+------+---------+-------------------------+--------+
|  1 | CLT-00001   | Test | Client  | client.test@example.com |      1 |
+----+-------------+------+---------+-------------------------+--------+
```

---

## 🔧 Solutions Possibles si le Problème Persiste

### Solution 1: Recréer la Base de Données

```sql
DROP DATABASE machine_repair_db;
CREATE DATABASE machine_repair_db;
```

Puis redémarrer le serveur.

### Solution 2: Modifier ddl-auto

Dans `application.properties`:

```properties
# Était: update
spring.jpa.hibernate.ddl-auto=create-drop

# Pour debug, puis remettre à update
```

### Solution 3: Désactiver le Cache

```properties
spring.jpa.properties.hibernate.cache.use_second_level_cache=false
spring.jpa.properties.hibernate.cache.use_query_cache=false
```

### Solution 4: Logs SQL Activés

```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

Cela vous montrera exactement les requêtes SQL exécutées.

---

## ✅ Checklist de Vérification

Avant de signaler un bug, vérifier:

- [ ] MySQL est démarré (`systemctl status mysql`)
- [ ] La base `machine_repair_db` existe
- [ ] Les logs montrent "Client de test sauvegardé avec ID: X"
- [ ] Les logs montrent "Vérification: Client CLT-00001 existe = true"
- [ ] Le serveur a démarré sans erreur
- [ ] Pas d'exception dans les logs
- [ ] L'identifiant envoyé est exactement "CLT-00001" (pas d'espaces)
- [ ] Le mot de passe envoyé est exactement "test123"
- [ ] Content-Type: application/json dans les headers
- [ ] Le body est en format JSON valide

---

## 📊 Logs Attendus en Cas de Succès

**Au démarrage:**
```
INFO  Started MachineManagementApplication in X seconds
✔ Administrateur déjà existant, création ignorée.
✔ Client de test créé automatiquement !
   Identifiant: CLT-00001
   Email: client.test@example.com
   Mot de passe: test123
INFO  Client de test sauvegardé avec ID: 1
INFO  Vérification: Client CLT-00001 existe = true
```

**À la connexion:**
```
DEBUG Tentative de connexion avec identifiant: 'CLT-00001'
DEBUG Longueur de l'identifiant: 9
DEBUG Client trouvé: CLT-00001 (client.test@example.com)
INFO  Client connecté: CLT-00001
```

---

## 🆘 Si Rien ne Fonctionne

1. **Supprimer complètement la base:**
   ```sql
   DROP DATABASE machine_repair_db;
   ```

2. **Supprimer le dossier target:**
   ```bash
   cd backend
   rm -rf target/
   ```

3. **Recompiler from scratch:**
   ```bash
   mvn clean install -DskipTests
   ```

4. **Redémarrer MySQL:**
   ```bash
   sudo systemctl restart mysql
   ```

5. **Redémarrer le serveur:**
   ```bash
   mvn spring-boot:run
   ```

---

**Date:** 2026-01-28  
**Version:** 1.1.0  
**Statut:** Corrections appliquées pour résoudre le problème
