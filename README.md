# `README.md`

# React + Spring Boot + Postgres + Nginx (JWT) — Monorepo

A full-stack template with:

- **Frontend**: React (Vite) served by Nginx
- **Backend**: Spring Boot (JWT auth)
- **DB**: PostgreSQL
- **Reverse Proxy**: Nginx → forwards `/api/*` to Spring
- **Orchestration**: Docker Compose

Here’s a properly formatted and readable version of the **Repo Structure** section for your README:

## Repo Structure

```

react-springboot-app/
├── backend/                    # Spring Boot API
│   ├── src/main/java/...       # Source code (JwtAuthFilter, JwtService, SecurityConfig, etc.)
│   ├── src/main/resources/     # application.properties (uses environment variables)
│   └── pom.xml                 # Maven build configuration
│
├── frontend/                   # React app (Vite)
│   ├── src/...                 # React components, hooks, etc.
│   ├── index.html              # Vite entry point
│   └── package.json            # Node dependencies and scripts
│
├── web/                        # Nginx reverse proxy (optional)
│   └── default.conf            # Nginx site configuration
│
├── docker-compose.yml           # Multi-service orchestration
├── Dockerfile.api               # Spring Boot API Dockerfile
├── Dockerfile.web               # Frontend/Nginx Dockerfile
├── example.env                  # Sample environment file (copy to .env)
├── deploy.sh                    # Script to build and deploy containers
├── README.md                    # Project documentation
└── .gitignore                   # Git ignore patterns

```



````

## Prerequisites

- Docker + Docker Compose
- (Optional for local dev) Java 17, Maven, Node 18+
- Create a **`.env`** from `example.env` and fill secrets

### Environment (.env)

> **Do not commit/ship real secrets.** Keep `.env` local; commit `example.env` instead.

```env
# JWT: Base64-encoded 32+ bytes (HS256)
JWT_SECRET=REPLACE_WITH_BASE64_SECRET

# Postgres
POSTGRES_DB=appdb
POSTGRES_USER=appuser
POSTGRES_PASSWORD=apppass
POSTGRES_HOST=db
POSTGRES_PORT=5432

# Spring boot binds to 8080 inside the container
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/appdb
SPRING_DATASOURCE_USERNAME=appuser
SPRING_DATASOURCE_PASSWORD=apppass
````

To generate a strong Base64 secret:

```bash
openssl rand -base64 48
```

## Build & Run (Docker, recommended)

From repo root:

```bash
# 1) Ensure .env exists with correct vars
cp example.env .env   # then edit .env

# 2) Build images
docker compose build

# 3) Start stack
docker compose up -d

# 4) Check containers
docker compose ps
docker compose logs -f api --tail=120
```

### Access

* **Frontend (Nginx)**: [http://localhost/](http://localhost/)
* **API (direct)**: [http://localhost:8080](http://localhost:8080)
* **Postgres**: localhost:5432 (inside compose: `db:5432`)

## First Test (JWT)

```bash
# login → token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"Bhaskar","password":"welcome1"}' | jq -r .token)

# secure endpoint
curl -i http://localhost:8080/api/secure/me -H "Authorization: Bearer $TOKEN"
```

Expected: `HTTP/1.1 200` and JSON with `username` and `authorities`.

## Nginx Proxying

The Nginx site config (used by the `web` image) must forward the auth header:

```nginx
location /api/ {
  proxy_pass http://api:8080;
  proxy_http_version 1.1;
  proxy_set_header Host $host;
  proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
  proxy_set_header X-Forwarded-Proto $scheme;
  proxy_set_header Connection "";
  proxy_set_header Authorization $http_authorization;  # important
}
```

## Local Development (optional)

### Backend (host machine)

```bash
cd backend
mvn spring-boot:run
# API on http://localhost:8080
```

### Frontend (host machine)

```bash
cd frontend
npm install
npm run dev
# App on http://localhost:5173
# Configure Vite proxy or set VITE_API_BASE to http://localhost:8080
```

## Useful Commands

```bash
# Rebuild only api or web
docker compose build api
docker compose build web

# Restart services
docker compose up -d api
docker compose up -d web

# Tail logs
docker compose logs -f api
docker compose logs -f web
docker compose logs -f db

# Stop & remove
docker compose down

# Clean dangling images/volumes (careful!)
docker image prune -f
docker volume ls
docker volume prune -f
```

## Frontend Build Notes

* `frontend/dist/` is created during `npm run build`. In Docker, we build inside the `web` image; locally it’s ignored by git.
* If you build locally and want Nginx to serve it, copy `frontend/dist/*` into the image or mount into `/usr/share/nginx/html`.

## Backend Notes

* `PasswordEncoder` bean = `BCryptPasswordEncoder`.
* `DaoAuthenticationProvider` must set both `UserDetailsService` and `PasswordEncoder`.
* JWT: we sign with HS256 using the Base64 secret from `.env` (`app.jwt.secret=${JWT_SECRET}` in `application.properties`).

## Common Issues & Fixes

* **401 on `/api/secure/me`**

  * Ensure the frontend sends `Authorization: Bearer <token>`.
  * Ensure Nginx forwards `Authorization` header.
  * If calling API directly, verify `TOKEN` shell variable is set.

* **403 via Nginx but 200 direct**

  * Missing `proxy_set_header Authorization $http_authorization;` in Nginx.

* **App fails to start: PasswordEncoder bean missing**

  * Add one `@Bean PasswordEncoder` and wire it into `DaoAuthenticationProvider`.

* **JWT secret too short / DecodingException**

  * Use Base64-encoded 32+ bytes (HS256). Regenerate with `openssl rand -base64 48`.

* **CORS errors in browser**

  * Enable CORS in `SecurityConfig` and allow `Authorization` header.
  * Preflight: permit `OPTIONS /**`.

## Cleanup Before Commit/Transfer

```bash
# Frontend
rm -rf frontend/node_modules frontend/dist

# Backend
rm -rf backend/target backend/.gradle backend/build

# Docker (stop and clean up)
docker compose down
docker image prune -f
```

> Keep `.env` **out of git**. Share `example.env` for teammates.
