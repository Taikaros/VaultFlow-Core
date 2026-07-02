<div align="center">

# VaultFlow-Core

**Plataforma B2B de gestión de tarjetas digitales corporativas y billeteras virtuales**

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat&logo=openjdk&logoColor=white)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.5-6DB33F?style=flat&logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Next.js-14-000000?style=flat&logo=nextdotjs&logoColor=white)](https://nextjs.org/)
[![SQLite](https://img.shields.io/badge/SQLite-003B57?style=flat&logo=sqlite&logoColor=white)](https://www.sqlite.org/)
[![Version](https://img.shields.io/badge/version-1.1.0-blue?style=flat)](VERSION)
[![Release](https://img.shields.io/github/v/tag/Taikaros/VaultFlow-Core?style=flat&logo=github&label=tag)](https://github.com/Taikaros/VaultFlow-Core/releases)
[![CI](https://img.shields.io/github/actions/workflow/status/Taikaros/VaultFlow-Core/ci.yml?branch=main&style=flat&logo=githubactions&label=CI)](https://github.com/Taikaros/VaultFlow-Core/actions)
[![License](https://img.shields.io/badge/license-MIT-green?style=flat)](LICENSE)

</div>

---

## 🛠️ Stack

| Capa | Tecnología |
|------|-----------|
| Backend | Java 21, Spring Boot 3.3.5, Maven |
| Frontend | Next.js 14 (App Router), React 18, TypeScript |
| Base de datos | SQLite (desarrollo), H2 (tests) |
| ORM | Spring Data JPA + Hibernate |
| Seguridad | JWT (jjwt 0.12), BCrypt |
| Tiempo real | WebSocket (STOMP sobre SockJS) |
| UI | shadcn/ui (Radix + Tailwind CSS), Recharts |
| Documentación API | SpringDoc OpenAPI (Swagger UI) |
| CI/CD | GitHub Actions (JDK 21 + Node 20) |
| Contenedores | Docker multi-stage |

---

## 📁 Estructura

```
VaultFlow-Core/
├── backend/                        # API REST (Spring Boot)
│   ├── src/main/java/com/vaultflow/
│   │   ├── config/                 # SecurityConfig, WebSocketConfig, DataInitializer
│   │   ├── controller/             # Auth, Wallet, Card, Transaction, User, Dashboard
│   │   ├── dto/                    # Request/Response DTOs
│   │   ├── model/                  # Company, Wallet, Card, Transaction, User
│   │   ├── repository/             # JPA repositories
│   │   ├── security/               # JwtTokenProvider, JwtAuthenticationFilter
│   │   └── service/                # Auth, Wallet, Card, Transaction, User, Notification
│   ├── src/test/                   # 25 tests unitarios + 15 de integración
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                       # Dashboard web (Next.js 14)
│   ├── src/app/
│   │   ├── login/                  # Inicio de sesión
│   │   ├── register/               # Registro de empresa
│   │   └── dashboard/              # Inicio, Tarjetas, Transacciones, Usuarios
│   ├── src/components/ui/          # shadcn/ui components
│   ├── src/context/                # AuthContext, WebSocketProvider
│   ├── src/lib/api.ts              # API client tipado con TypeScript
│   ├── Dockerfile
│   └── package.json
├── specs/
│   ├── api/openapi.yaml            # Contrato OpenAPI 3.1
│   ├── database/schema.sql         # DDL SQLite
│   └── flows/                      # Diagramas de flujo (Mermaid)
├── docker-compose.yml
└── .github/workflows/ci.yml        # Pipeline CI/CD
```

---

## 🚀 Inicio rápido

### Requisitos

- Java 21
- Node.js 20
- Docker (opcional)

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

La API arranca en `http://localhost:8080`.  
Swagger UI disponible en `http://localhost:8080/swagger-ui.html`.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

El dashboard arranca en `http://localhost:3000`.

### Docker

```bash
docker compose up --build
```

### Seed data

Al iniciar con SQLite, si la tabla `companies` está vacía se ejecuta `db/seed.sql`:

| Campo | Valor |
|-------|-------|
| Email | `admin@techcorp.com` |
| Contraseña | `admin123` |
| Wallets | 2 (USD 50.000 + USD 10.000) |
| Tarjetas | 2 activas |
| Transacciones | 1 completada |

---

## 🗄️ Modelo de datos

```
companies 1───* wallets
companies 1───* users
wallets   1───* cards
wallets   1───* transactions (to_wallet_id)
cards     1───* transactions (from_card_id)
```

### Roles de usuario

- **ADMIN** — acceso completo, gestión de usuarios
- **EMPLOYEE** — operaciones básicas (tarjetas, pagos)

---

## 🌿 Estrategia de ramas

```
feature/transferencias ──┐
fix/limite-tarjeta ──────┤
                         ▼
                    [ develop ]  ── PR ──► [ main ] ──► tag v* ──► [ deploy ]
                         ▲
                    release/v1.2.0 ──┘
```

| Rama | Origen | Mergea a | Propósito |
|------|--------|----------|-----------|
| `feature/*` | `develop` | `develop` (PR) | Nueva funcionalidad |
| `fix/*` | `develop` | `develop` (PR) | Corrección de errores |
| `release/*` | `develop` | `main` + `develop` | Preparación de release (opcional) |
| `develop` | — | `main` (PR) | Integración diaria |
| `main` | — | — | Rama oficial de releases |
| `deploy` | `main` | — | Apunta al último tag liberado. Se mueve automáticamente |

### Flujo diario

```bash
# 1. Crear feature/fix desde develop
git checkout develop
git checkout -b feature/mi-feature

# ... trabajar y commitear ...

git push origin feature/mi-feature
# → Crear PR en GitHub hacia develop

# 2. Una vez aprobado el release:
git checkout main
git merge develop
git tag -a v1.2.0 -m "Release v1.2.0 - Breve descripción de cambios"
git push origin v1.2.0
# → CI automático: build, release, y deploy avanza solo
```

> **Nota:** Usamos **annotated tags** (`git tag -a`) en lugar de lightweight tags. Los annotated tags incluyen metadata del autor, fecha y mensaje, y soportan verificación GPG.
> Ver [CONTRIBUTING.md](CONTRIBUTING.md) para más detalles.

---

## 🤖 Workflow CI

El pipeline se define en `.github/workflows/ci.yml` y reacciona a diferentes eventos:

| Evento | Acción |
|--------|--------|
| PR a `main` o `develop` | Ejecuta tests (backend + frontend) |
| Push a `develop` | Ejecuta tests |
| Push de tag `v*` | Build JAR + frontend, crea GitHub Release, avanza rama `deploy` |
| Push de tag `v*` + secrets Docker configurados | También publica imágenes en Docker Hub |

### Jobs

```yaml
test:        # Se ejecuta en PRs y push a develop
  - Backend: mvn test (40 tests: 25 unit + 15 integración)
  - Frontend: npm ci + npm run build

release:     # Solo en tags v*
  - Build JAR con Maven
  - Build frontend con npm
  - Empaqueta .tar.gz del frontend
  - Crea GitHub Release con artifacts adjuntos
  - Avanza rama deploy al commit del tag

docker:      # Solo en tags v* si existen secrets de Docker Hub
  - Build y push imágenes backend + frontend a Docker Hub
```

---

## 📦 Proceso de release

Cada release sigue este flujo:

### 1. Preparación

```bash
git checkout develop
# Asegurar que develop está listo (features mergeadas, tests verdes)
```

### 2. Taggear (annotated tag)

```bash
git checkout main
git merge develop              # Merge final a main
git tag -a v1.2.0 -m "Release v1.2.0 - Breve descripción de cambios"
git push origin v1.2.0
```

> Los annotated tags (`-a`) incluyen autor, fecha y mensaje. Son requeridos para releases oficiales.

### 3. Automatizado por CI

Al pushear el tag, el CI automáticamente:

1. Corre todos los tests
2. Buildca el JAR del backend
3. Buildca y empaqueta el frontend
4. Crea un **GitHub Release** con:
   - `vaultflow-core-*.jar` (backend ejecutable)
   - `vaultflow-frontend.tar.gz` (frontend compilado)
   - Notas de release generadas automáticamente
5. Avanza la rama **`deploy`** al commit del tag
6. Si hay credenciales Docker configuradas, también publica imágenes en Docker Hub

### Descargar un release

Ir a [Releases](https://github.com/Taikaros/VaultFlow-Core/releases) y descargar los artifacts:

- **JAR:** `java -jar vaultflow-core-*.jar` (requiere Java 21)
- **Frontend:** extraer `vaultflow-frontend.tar.gz` y ejecutar `npm start` (requiere Node.js 20)

---

## 📡 API

### Autenticación

Todas las rutas excepto `/api/v1/auth/**` requieren header:

```
Authorization: Bearer <token>
```

### Endpoints

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/v1/auth/register` | Registrar empresa + wallet |
| POST | `/api/v1/auth/login` | Iniciar sesión |
| GET | `/api/v1/dashboard/metrics` | Métricas agregadas |
| GET | `/api/v1/wallets` | Listar wallets |
| POST | `/api/v1/wallets` | Crear wallet |
| GET | `/api/v1/wallets/{id}` | Detalle wallet |
| GET | `/api/v1/wallets/{id}/transactions` | Transacciones por wallet (paginado) |
| GET | `/api/v1/cards` | Listar tarjetas (`?walletId=`) |
| POST | `/api/v1/wallets/{id}/cards` | Emitir tarjeta |
| PATCH | `/api/v1/cards/{id}` | Actualizar tarjeta |
| DELETE | `/api/v1/cards/{id}` | Cancelar tarjeta |
| POST | `/api/v1/transactions` | Ejecutar pago |
| GET | `/api/v1/transactions` | Listar transacciones (paginado, `?status=&from=&to=`) |
| GET | `/api/v1/users` | Listar usuarios (paginado) |
| POST | `/api/v1/users` | Crear usuario |
| PATCH | `/api/v1/users/{id}` | Actualizar usuario |
| DELETE | `/api/v1/users/{id}` | Eliminar usuario |

### 🔌 WebSocket

```
Endpoint:    /ws
Protocolo:   STOMP sobre SockJS
Broker:      /topic
Notificaciones: /topic/company/{companyId}
```

### 📄 Paginación

Parámetros `?page=0&size=20`. La respuesta incluye:

```json
{
  "content": [ ... ],
  "totalElements": 50,
  "totalPages": 3,
  "number": 0
}
```

---

## 📋 Changelog

### v1.1.0 (2026-06-29)

- Roles de usuario (ADMIN/EMPLOYEE) con CRUD completo desde el frontend
- WebSockets para notificaciones en tiempo real (STOMP/SockJS)
- Dashboard con métricas agregadas y gráfico de torta (Recharts)
- Paginación en transacciones y listado de usuarios
- Estrategia de ramas: main, develop, deploy, feature/*, fix/*
- CI/CD pipeline con release automático al pushear tags
- README completo con documentación, branching y changelog

### v1.0.1 (2026-06-28)

- 40 tests: 25 unitarios (Mockito) + 15 de integración (H2)
- Seed data automática con usuario demo
- CI pipeline con GitHub Actions
- DataInitializer con detección de SQLite vs H2

### v1.0.0 (2026-06-27)

- MVP inicial del backend: Auth, Wallet, Card, Transaction
- Frontend con login, registro y dashboard básico
- JWT + seguridad con Spring Security
- Documentación OpenAPI con Swagger UI
- Docker multi-stage para backend y frontend
- Docker Compose para desarrollo local

---

## 🤝 Contribuir

Ver [CONTRIBUTING.md](CONTRIBUTING.md) para:
- Convención de commits (Conventional Commits)
- Flujo de versionado (SemVer)
- Proceso de release
- Políticas de ramas y PRs

---

## 🧪 Pruebas

```bash
# Backend — 40 tests (25 unit + 15 integración)
cd backend
./mvnw test

# Frontend — lint + build
cd frontend
npm run lint
npm run build
```

---

## 📄 Licencia

Uso interno.
