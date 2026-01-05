# 🔒 Documentation du Système d'Authentification JWT

## Vue d'ensemble

Cette application utilise **JWT (JSON Web Token)** pour l'authentification. C'est une approche **stateless** (sans session côté serveur).

## 📋 Architecture de Sécurité

### Composants principaux

1. **JwtUtil** : Génère et valide les tokens JWT
2. **JwtAuthenticationFilter** : Intercepte les requêtes et authentifie l'utilisateur
3. **CustomUserDetailsService** : Charge les utilisateurs depuis la base de données
4. **SecurityConfig** : Configure Spring Security (routes publiques/protégées)
5. **UserServiceImpl** : Gère le login et la création de compte

---

## 🔄 Flow complet d'authentification

### 1. Création de compte (Register)

```
Client                    Controller              Service                 Repository              Database
  |                          |                        |                        |                        |
  |-- POST /api/users/register -->                    |                        |                        |
  |    {username, email, password}                    |                        |                        |
  |                          |                        |                        |                        |
  |                          |--- createUser() ------>|                        |                        |
  |                          |                        |                        |                        |
  |                          |                        |--- hash password ------|                        |
  |                          |                        |   (BCrypt)             |                        |
  |                          |                        |                        |                        |
  |                          |                        |--- save(user) -------->|--- INSERT INTO users ->|
  |                          |                        |                        |                        |
  |<-- 201 Created -----------<--- UserResponse ------|                        |                        |
  |    {id, username, email}                          |                        |                        |
```

**Étapes :**
1. Client envoie username, email, password
2. Service hash le mot de passe avec BCrypt
3. User est sauvegardé en base
4. Retourne les infos (sans le mot de passe)

### 2. Login et obtention du token JWT

```
Client                    Controller              Service                 AuthenticationManager    JwtUtil         Database
  |                          |                        |                            |                    |               |
  |-- POST /api/users/login -->                       |                            |                    |               |
  |    {usernameOrEmail, password}                    |                            |                    |               |
  |                          |                        |                            |                    |               |
  |                          |--- login() ----------->|                            |                    |               |
  |                          |                        |                            |                    |               |
  |                          |                        |--- authenticate() -------->|                    |               |
  |                          |                        |                            |                    |               |
  |                          |                        |                            |--- loadUser() -----|-> SELECT users|
  |                          |                        |                            |                    |               |
  |                          |                        |                            |--- compare hash ---|               |
  |                          |                        |                            |   (BCrypt)         |               |
  |                          |                        |                            |                    |               |
  |                          |                        |<-- authenticated user -----|                    |               |
  |                          |                        |                            |                    |               |
  |                          |                        |--- generateToken() ----------------------->|               |
  |                          |                        |    (username, userId)                      |               |
  |                          |                        |                                           |               |
  |                          |                        |<-- JWT token (eyJhbGc...) <---------------|               |
  |                          |                        |                                                           |
  |<-- 200 OK ---------------<--- LoginResponse -----|                                                           |
  |    {token, userId, username, email}              |                                                           |
```

**Étapes :**
1. Client envoie username/email + password
2. AuthenticationManager :
   - Charge l'utilisateur depuis la base
   - Compare le password fourni avec le hash en base (BCrypt)
   - Lève exception si mauvais credentials
3. Si OK, génère un token JWT contenant :
   - username (dans le "subject")
   - userId (dans un claim custom)
   - date d'expiration (24h par défaut)
4. Retourne le token au client
5. **Client stocke le token** (localStorage, sessionStorage, cookie)

### 3. Requête authentifiée (accès à une route protégée)

```
Client                    JwtAuthFilter          CustomUserDetailsService    SecurityContext    Controller    Service       Database
  |                          |                            |                            |              |              |              |
  |-- GET /api/todos ------->|                            |                            |              |              |              |
  |  Header: Authorization: Bearer eyJhbGc...            |                            |              |              |              |
  |                          |                            |                            |              |              |              |
  |                          |--- extract JWT token ------|                            |              |              |              |
  |                          |--- extract username -------|                            |              |              |              |
  |                          |                            |                            |              |              |              |
  |                          |--- loadUserByUsername() -->|                            |              |              |              |
  |                          |                            |--- SELECT user -----------|--------------|------------->|              |
  |                          |<-- UserDetails ------------|                            |              |              |              |
  |                          |                            |                            |              |              |              |
  |                          |--- validate token ---------|                            |              |              |              |
  |                          |   (signature + expiration) |                            |              |              |              |
  |                          |                            |                            |              |              |              |
  |                          |--- setAuthentication() --->|                            |              |              |              |
  |                          |                            |--- store user context ---->|              |              |              |
  |                          |                            |                            |              |              |              |
  |                          |--- pass to next filter ----|------------------------->|              |              |              |
  |                          |                            |                            |              |              |              |
  |                          |                            |                            |<-- check if authenticated   |              |
  |                          |                            |                            |              |              |              |
  |                          |                            |                            |------------->|              |              |
  |                          |                            |                            |              |              |              |
  |                          |                            |                            |              |--- getAllTodos() -------> SELECT *
  |                          |                            |                            |              |              |              |
  |<-- 200 OK --------------|----------------------------|----------------------------|<-------------|<-------------|              |
  |    [{todo1}, {todo2}]    |                            |                            |              |              |              |
```

**Étapes :**
1. Client envoie requête avec header : `Authorization: Bearer eyJhbGc...`
2. **JwtAuthenticationFilter** intercepte la requête :
   - Extrait le token du header
   - Extrait le username du token
   - Charge l'utilisateur depuis la base
   - Valide le token (signature + expiration + username)
3. Si token valide :
   - Crée un objet Authentication
   - Le stocke dans le SecurityContext
4. Passe au controller
5. **SecurityConfig** vérifie si l'utilisateur est authentifié :
   - Si oui, autorise l'accès
   - Si non, retourne 403 Forbidden
6. Controller traite la requête normalement

### 4. Token expiré ou invalide

```
Client                    JwtAuthFilter          SecurityContext         Client
  |                          |                            |                 |
  |-- GET /api/todos ------->|                            |                 |
  |  Header: Authorization: Bearer <expired_token>        |                 |
  |                          |                            |                 |
  |                          |--- validate token ---------|                 |
  |                          |   ❌ EXPIRED or INVALID    |                 |
  |                          |                            |                 |
  |                          |--- log error               |                 |
  |                          |--- pass to next filter --->|                 |
  |                          |                            |                 |
  |                          |                    ❌ User not authenticated |
  |                          |                            |                 |
  |<-- 403 Forbidden --------|----------------------------|                 |
  |                          |                            |                 |
  |--- POST /api/users/login (re-login) ------------------>                 |
```

**Comportement :**
- Token expiré → 403 Forbidden
- Token invalide → 403 Forbidden
- Pas de token → 403 Forbidden (sauf routes publiques)
- Client doit se reconnecter pour obtenir un nouveau token

---

## 🔑 Structure du JWT

Un JWT est composé de 3 parties séparées par des points :

```
eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huZG9lIiwidXNlcklkIjoxLCJpYXQiOjE3MDk1NjQwMDAsImV4cCI6MTcwOTY1MDQwMH0.signature
     HEADER            .                          PAYLOAD                                        .  SIGNATURE
```

### Header (décodé)
```json
{
  "alg": "HS512",
  "typ": "JWT"
}
```
- `alg` : algorithme de signature (HMAC-SHA512)
- `typ` : type de token (JWT)

### Payload (décodé)
```json
{
  "sub": "johndoe",
  "userId": 1,
  "iat": 1709564000,
  "exp": 1709650400
}
```
- `sub` : subject = username de l'utilisateur
- `userId` : ID de l'utilisateur en base (claim custom)
- `iat` : issued at = date de création du token
- `exp` : expiration = date d'expiration du token

### Signature
```
HMACSHA512(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret_key
)
```
- Garantit que le token n'a pas été modifié
- Seul le serveur peut créer/valider la signature (grâce à la clé secrète)

**⚠️ Important :** Le payload est encodé en Base64, **PAS chiffré** !
Ne jamais mettre d'informations sensibles (mot de passe, numéro de carte bancaire) dans un JWT.

---

## 🛡️ Sécurité

### Avantages du JWT

✅ **Stateless** : Pas de session côté serveur, facilite la scalabilité
✅ **Décentralisé** : Le token contient toutes les infos, pas de lookup en base
✅ **Cross-domain** : Fonctionne entre différents domaines (microservices)
✅ **Mobile-friendly** : Plus simple que les cookies pour les apps mobiles

### Points d'attention

⚠️ **Stockage du token côté client** :
- ❌ localStorage : vulnérable aux attaques XSS
- ✅ httpOnly cookie : plus sécurisé mais nécessite CSRF protection
- ✅ sessionStorage : compromis acceptable pour une SPA

⚠️ **Clé secrète** :
- Doit être longue (minimum 512 bits pour HS512)
- Doit être stockée dans une variable d'environnement
- Ne JAMAIS la commiter dans Git

⚠️ **Expiration** :
- Token de courte durée (24h) + refresh token (recommandé)
- Notre implémentation : 24h (configurable dans application.properties)

⚠️ **Révocation** :
- JWT ne peut pas être révoqué facilement (stateless)
- Solutions :
  - Durée de vie courte
  - Blacklist des tokens (nécessite du stockage côté serveur)
  - Soft delete de l'utilisateur (notre implémentation)

---

## 🧪 Tests avec curl

**Note importante :** Le context-path est `/api` (configuré dans application.properties), donc toutes les routes sont accessibles via `http://localhost:8080/api/...`

### 1. Créer un compte
```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstname": "John",
    "lastname": "Doe",
    "username": "johndoe",
    "email": "john@example.com",
    "password": "password123"
  }'
```

### 2. Se connecter
```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "johndoe",
    "password": "password123"
  }'
```

Réponse :
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "userId": 1,
  "username": "johndoe",
  "email": "john@example.com"
}
```

### 3. Accéder à une route protégée
```bash
curl -X GET http://localhost:8080/api/todos \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

### 4. Créer un todo
```bash
curl -X POST http://localhost:8080/api/todos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..." \
  -d '{
    "description": "Ma première tâche",
    "note": "Important",
    "finalDate": "2026-01-10:18:00:00"
  }'
```

---

## 📁 Fichiers de sécurité

| Fichier | Rôle |
|---------|------|
| `JwtUtil.java` | Génère et valide les tokens JWT |
| `JwtAuthenticationFilter.java` | Intercepte les requêtes et authentifie via JWT |
| `CustomUserDetailsService.java` | Charge les utilisateurs depuis la base |
| `SecurityConfig.java` | Configure Spring Security (routes, filtres) |
| `UserServiceImpl.java` | Gère le login et la création de compte |

---

## 🔧 Configuration

### application.properties
```properties
# Durée de validité du token JWT (en millisecondes)
# 86400000 ms = 24 heures
jwt.expiration = 86400000
```

### En production
```properties
# Utiliser une variable d'environnement pour la clé secrète
jwt.secret = ${JWT_SECRET_KEY}
jwt.expiration = 3600000  # 1 heure recommandé en prod
```

---

## 🚀 Améliorations possibles

1. **Refresh Token** : Token de longue durée pour renouveler l'access token
2. **Roles & Permissions** : Ajouter ROLE_ADMIN, ROLE_USER dans les authorities
3. **Rate Limiting** : Limiter les tentatives de login
4. **2FA** : Authentification à deux facteurs
5. **Token Blacklist** : Pour révoquer les tokens
6. **Audit Logging** : Logger tous les accès et actions sensibles

