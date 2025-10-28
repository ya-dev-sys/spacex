# SpaceX Dashboard API

Backend REST API pour suivre les lancements SpaceX en temps réel.

## Technologies

- Java 21
- Spring Boot 3.5.7
- Spring Security avec JWT
- PostgreSQL
- Redis pour le cache
- Docker & Docker Compose

## Prérequis

- Java 21+
- Docker & Docker Compose
- Maven (ou utiliser ./mvnw)

## Installation

1. Cloner le repo :

```bash
git clone https://github.com/votre-username/spacex.git
cd spacex
```

2. Configurer les variables d'environnement :

```bash
cp .env.example .env
# Éditer .env avec vos valeurs
```

3. Lancer avec Docker :

```bash
docker compose up -d
```

## Architecture

### Base de données

- PostgreSQL pour le stockage permanent
- Redis pour le cache des données SpaceX
- Migrations automatiques via Hibernate

### Sécurité

- JWT pour l'authentification
- Rôles hiérarchiques (ADMIN > USER)
- Durée de validité des tokens : 24h

### Cache

- Mise en cache des données SpaceX
- Invalidation automatique après 1h
- Possibilité de forcer la resync via l'API admin

## Comptes de test

### Administrateur

```
email: admin@example.com
password: admin123
roles: ADMIN, USER
```

### Utilisateur standard

```
username: user@example.com
password: user123
roles: USER
```

## API Endpoints détaillés

### Authentification

```
POST /auth/login
Body: {
    "email": "string",
    "password": "string"
}
Response: {
    "token": "string",
    "type": "Bearer"
}
```

### Dashboard

```
GET /dashboard/kpis
Header: Authorization: Bearer {token}
Response: {
    "totalLaunches": number,
    "successRate": number,
    "nextLaunch": object
}
```

### Admin

```
POST /admin/resync
Header: Authorization: Bearer {token}
Response: {
    "success": boolean,
    "launchesProcessed": number
}
```

## Commandes utiles

### Génération d'un nouveau token JWT

```bash
curl -X POST http://localhost:8080/auth/login \
-H "Content-Type: application/json" \
-d '{"email":"admin@example.com","password":"admin123"}'
```

### Forcer une resync (ADMIN)

```bash
curl -X POST http://localhost:8080/admin/resync \
-H "Authorization: Bearer {votre-token}"
```

## Logs

Les logs sont stockés dans :

- Console (développement)
- /logs/spacex-backend.log (production)
- /logs/archived/ (historique)

## Monitoring

Endpoints Actuator disponibles :

- /actuator/health - État du système
- /actuator/info - Informations de l'application
