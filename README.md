# VaultFlow-Core

Plataforma B2B de gestión de tarjetas digitales corporativas y billeteras virtuales.

## Stack

| Capa | Tecnología |
|------|-----------|
| Backend | Java 21, Spring Boot 3.3.5, Maven |
| Frontend | Next.js 14 (App Router), React 18, TypeScript |
| BD | SQLite (dev), H2 (tests) |
| ORM | Spring Data JPA + Hibernate |
| Seguridad | JWT (jjwt 0.12), BCrypt |
| Tiempo real | WebSocket (STOMP sobre SockJS) |
| UI | shadcn/ui (Radix + Tailwind), Recharts |
| Documentación API | SpringDoc OpenAPI (Swagger UI) |
| CI | GitHub Actions (JDK 21 + Node 20) |
| Contenedores | Docker multi-stage |

## Estructura

```
VaultFlow-Core/
├── backend/                   # API REST (Spring Boot)
│   ├── src/
│   │   ├── main/java/com/vaultflow/
│   │   │   ├── config/        # SecurityConfig, WebSocketConfig, DataInitializer
│   │   │   ├── controller/    # Auth, Wallet, Card, Transaction, User, Dashboard
│   │   │   ├── dto/           # Request/Response DTOs
│   │   │   ├── model/         # Company, Wallet, Card, Transaction, User
│   │   │   ├── repository/    # JPA repositories
│   │   │   ├── security/      # JwtTokenProvider, JwtAuthenticationFilter
│   │   │   └── service/       # Auth, Wallet, Card, Transaction, User, Notification
│   │   └── test/              # 25 tests unitarios + 15 de integración
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                  # Dashboard web (Next.js)
│   ├── src/
│   │   ├── app/
│   │   │   ├── login/         # Inicio de sesión
│   │   │   ├── register/      # Registro de empresa
│   │   │   └── dashboard/     # Inicio, Tarjetas, Transacciones, Usuarios
│   │   ├── components/ui/     # shadcn/ui components
│   │   ├── context/           # AuthContext, WebSocketProvider
│   │   └── lib/api.ts         # API client tipado
│   ├── Dockerfile
│   └── package.json
├── specs/
│   ├── api/openapi.yaml       # Contrato OpenAPI 3.1
│   ├── database/schema.sql    # DDL SQLite
│   └── flows/                 # Diagramas Mermaid
├── docker-compose.yml
└── .github/workflows/ci.yml
```

## Inicio rápido

### Requisitos

- Java 21
- Node.js 20
- Docker (opcional)

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

La API arranca en `http://localhost:8080`. Swagger UI en `/swagger-ui.html`.

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

- **Email:** `admin@techcorp.com`
- **Contraseña:** `admin123`
- 2 wallets (USD 50.000 + USD 10.000)
- 2 tarjetas activas
- 1 transacción completada

## API

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

### WebSocket

```
Endpoint: /ws
Protocolo: STOMP sobre SockJS
Broker: /topic
Notificaciones: /topic/company/{companyId}
```

### Paginación

Parámetros `?page=0&size=20`. Respuesta incluye `content`, `totalElements`, `totalPages`, `number`.

## Pruebas

```bash
cd backend
./mvnw test           # 40 tests (25 unit + 15 integración)
```

```bash
cd frontend
npm run lint          # ESLint
npm run build         # TypeScript + compilación
```

## Modelo de datos

```
companies 1---* wallets
companies 1---* users
wallets   1---* cards
wallets   1---* transactions (to_wallet_id)
cards     1---* transactions (from_card_id)
```

### Roles de usuario

- **ADMIN** — acceso completo, gestión de usuarios
- **EMPLOYEE** — operaciones básicas (tarjetas, pagos)

## Licencia

Uso interno.
