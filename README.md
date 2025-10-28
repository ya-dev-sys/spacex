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

## Endpoints

- `POST /auth/login` - Authentification
- `GET /dashboard/kpis` - KPIs globaux
- `GET /dashboard/stats/yearly` - Stats par année
- `GET /dashboard/launches` - Liste des lancements
- `POST /admin/resync` - Resync avec l'API SpaceX (admin)

## Développement

```bash
./mvnw spring-boot:run
```

## Tests

```bash
./mvnw test
```
