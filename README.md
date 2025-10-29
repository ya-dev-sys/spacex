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
git clone https://github.com/ya-dev-sys/spacex.git
cd spacex
```

2. Configurer les variables d'environnement :

```bash
cp .env.example .env
```

Éditez le fichier `.env` avec vos propres valeurs :

- `SERVER_PORT` : Port du serveur backend (défaut: 8080)
- `POSTGRES_USER` : Utilisateur PostgreSQL (ex: spacex)
- `POSTGRES_PASSWORD` : Mot de passe PostgreSQL (choisir un mot de passe fort)
- `POSTGRES_DB` : Nom de la base de données (ex: spacex_dashboard)
- `JWT_SECRET` : Clé secrète pour JWT (au moins 256 bits / 32 caractères)

3. Installation avec Docker (recommandé) :

```bash
# Construire et démarrer les conteneurs
docker compose up -d

# Vérifier les logs
docker compose logs -f backend

# Pour arrêter
docker compose down

# Pour supprimer toutes les données (volumes)
docker compose down -v
```

4. Installation locale (sans Docker) :

Prérequis:

- Java 21 (JDK)
- PostgreSQL 16
- Redis 7.4
- Maven 3.9+ (ou utilisez ./mvnw)

```bash
# Installer PostgreSQL
# Sur Ubuntu/Debian:
sudo apt update
sudo apt install postgresql-16

# Démarrer PostgreSQL
sudo systemctl start postgresql

# Créer la base de données et l'utilisateur
sudo -u postgres psql
postgres=# CREATE DATABASE spacex_dashboard;
postgres=# CREATE USER spacex WITH ENCRYPTED PASSWORD 'votre_mot_de_passe';
postgres=# GRANT ALL PRIVILEGES ON DATABASE spacex_dashboard TO spacex;

# Installer Redis
# Sur Ubuntu/Debian:
sudo apt install redis-server
sudo systemctl start redis-server

# Compiler et lancer l'application
./mvnw clean package
java -jar target/laucncher-0.0.1-SNAPSHOT.jar
```

5. Vérifier l'installation :

```bash
# L'API devrait être accessible sur:
curl http://localhost:8080/actuator/health

# Vous devriez voir:
{"status":"UP"}
```

6. Comptes par défaut :

L'application crée automatiquement deux comptes :

- Admin: `admin@example.com` / `admin123`
- User: `user@example.com` / `user123`

Changez ces mots de passe en production !

7. En cas de problèmes :

- Vérifiez les logs: `docker compose logs -f` ou `tail -f logs/spacex-backend.log`
- Assurez-vous que tous les ports requis sont libres (8080, 5432, 6379)
- Vérifiez les connexions PostgreSQL et Redis
- En mode local, assurez-vous que votre JDK est bien Java 21+

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
