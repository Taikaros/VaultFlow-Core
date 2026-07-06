# Plan de Deploy: VaultFlow-Core

**Render** (Backend API) + **Netlify** (Frontend UI)

## Decisiones tomadas

| Decisión | Opción elegida |
|----------|---------------|
| Base de datos producción | PostgreSQL |
| Frontend Netlify | Static Export |
| WebSocket | STOMP + SockJS con `@stomp/stompjs` + `sockjs-client` |

---

## Fase 1 — Backend: PostgreSQL + Prod Profile

### Archivos a modificar/crear

| Archivo | Acción |
|---------|--------|
| `backend/pom.xml` | Agregar driver PostgreSQL (`postgresql:42.7.x`, runtime scope) + `spring-boot-starter-actuator` |
| `backend/src/main/resources/application.yml` | Cambiar a multi-perfil: default `dev` |
| `backend/src/main/resources/application-dev.yml` | **NUEVO** — SQLite config actual |
| `backend/src/main/resources/application-prod.yml` | **NUEVO** — PostgreSQL config |
| `backend/src/main/java/com/vaultflow/config/DataInitializer.java` | Refactor: soporte PostgreSQL (no solo SQLite) |
| `backend/src/main/resources/db/seed.sql` | Adaptar a PostgreSQL (`NOW()` en vez de `datetime('now')`, `gen_random_uuid()` en vez de `hex(randomblob(16))`) |
| `backend/src/main/java/com/vaultflow/config/CorsConfig.java` | **NUEVO** — CORS desde `CORS_ALLOWED_ORIGINS` env var |

### application-dev.yml (SQLite)
```yaml
spring:
  datasource:
    url: jdbc:sqlite:vaultflow.db
    driver-class-name: org.sqlite.JDBC
  jpa:
    database-platform: org.hibernate.community.dialect.SQLiteDialect
    hibernate:
      ddl-auto: update
```

### application-prod.yml (PostgreSQL)
```yaml
spring:
  datasource:
    url: ${JDBC_DATABASE_URL}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 5
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: validate
    show-sql: false
  jackson:
    serialization:
      write-dates-as-timestamps: false

vaultflow:
  jwt:
    secret: ${VAULTFLOW_JWT_SECRET}
    expiration: 86400000

cors:
  allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000}
```

### application.properties (o mantener yml)
```properties
spring.profiles.active=dev
```

### Seed para PostgreSQL
`backend/src/main/resources/db/seed.sql` — cambiar:
- `datetime('now')` → `NOW()`
- `hex(randomblob(16))` → `gen_random_uuid()::text`
- Usar `INSERT ... ON CONFLICT DO NOTHING` en vez de `INSERT OR IGNORE`

### DataInitializer.java
- Agregar chequeo de perfil activo o tipo de DB
- Ejecutar seed solo si tabla vacía (ya funciona)
- Adaptar sentencias SQL para PostgreSQL

### Tests
- `mvn test -B` debe seguir pasando (usan H2)

### Commit
```
feat(db): add PostgreSQL support and production profile
Closes #13
```

---

## Fase 2 — Frontend: Static Export + Netlify

### Archivos a modificar/crear

| Archivo | Acción |
|---------|--------|
| `frontend/next.config.mjs` | Agregar `output: 'export'`, `images: { unoptimized: true }`, `trailingSlash: true` |
| `frontend/netlify.toml` | **NUEVO** — build command, publish dir, redirects, node version |
| `frontend/public/_redirects` | **NUEVO** — `/* /index.html 200` |
| `frontend/.env.production` | **NO COMMITEAR** — variables de producción |

### next.config.mjs
```js
const nextConfig = {
  output: 'export',
  images: { unoptimized: true },
  trailingSlash: true,
};
export default nextConfig;
```

### netlify.toml
```toml
[build]
  command = "npm ci && npm run build"
  publish = "out/"

[build.environment]
  NODE_VERSION = "20"

[[redirects]]
  from = "/*"
  to = "/index.html"
  status = 200
```

### .env.production (local, no commitear — setear en Netlify Dashboard)
```
NEXT_PUBLIC_API_URL=https://vaultflow-api.onrender.com/api/v1
NEXT_PUBLIC_WS_URL=wss://vaultflow-api.onrender.com/ws
```

### Verificaciones
- `npm run build` genera carpeta `out/` sin errores
- No hay `getServerSideProps`, `API routes`, ni `middleware`
- Imágenes con `next/image` migradas a `<img>` o config `unoptimized: true`

### Commit
```
feat(frontend): configure static export and Netlify deployment
Closes #14
```

---

## Fase 3 — WebSocket para Producción

### Problema detectado
- Backend expone STOMP sobre SockJS
- Frontend intenta conectar WebSocket nativo sin librería cliente
- Static export no soporta WebSocket server-side

### Archivos a modificar

| Archivo | Acción |
|---------|--------|
| `frontend/package.json` | Agregar `@stomp/stompjs` + `sockjs-client` |
| `frontend/src/context/websocket.tsx` | Reescribir usando STOMP Client con SockJS |

### websocket.tsx (nueva implementación)
```typescript
'use client';

import { Client } from '@stomp/stompjs';
import { createContext, useContext, useEffect, useState, type ReactNode } from 'react';

const WS_URL = process.env.NEXT_PUBLIC_WS_URL || 'http://localhost:8080/ws';

interface WebSocketContextType {
  connected: boolean;
  subscribe: (destination: string, callback: (msg: any) => void) => () => void;
  send: (destination: string, body: any) => void;
}

const WebSocketContext = createContext<WebSocketContextType | null>(null);

export function WebSocketProvider({ children }: { children: ReactNode }) {
  const [client] = useState(() => new Client({
    brokerURL: WS_URL.replace(/^http/, 'ws') + '/ws',
    connectHeaders: {},
    debug: process.env.NODE_ENV === 'development' ? console.log : undefined,
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
  }));
  const [connected, setConnected] = useState(false);

  useEffect(() => {
    client.onConnect = () => setConnected(true);
    client.onDisconnect = () => setConnected(false);
    client.activate();
    return () => { client.deactivate(); };
  }, [client]);

  const subscribe = (destination: string, callback: (msg: any) => void) => {
    const sub = client.subscribe(destination, message => {
      callback(JSON.parse(message.body));
    });
    return () => sub.unsubscribe();
  };

  const send = (destination: string, body: any) => {
    client.publish({ destination, body: JSON.stringify(body) });
  };

  return (
    <WebSocketContext.Provider value={{ connected, subscribe, send }}>
      {children}
    </WebSocketContext.Provider>
  );
}

export const useWebSocket = () => {
  const ctx = useContext(WebSocketContext);
  if (!ctx) throw new Error('useWebSocket must be used within WebSocketProvider');
  return ctx;
};
```

### Commit
```
feat(ws): add STOMP/SockJS client for production WebSocket
Closes #16
```

---

## Fase 4 — Deploy Automático

### 4A — Render (Backend API)

#### Opción 1: render.yaml (Infraestructura como código)
```yaml
services:
  - type: web
    name: vaultflow-api
    env: java
    region: ohio
    plan: free
    buildCommand: mvn package -DskipTests -B
    startCommand: java -jar target/*.jar
    healthCheckPath: /actuator/health
    envVars:
      - key: SPRING_PROFILES_ACTIVE
        value: prod
      - key: VAULTFLOW_JWT_SECRET
        sync: false  # se setea manualmente
      - key: CORS_ALLOWED_ORIGINS
        value: https://vaultflow.netlify.app
      - key: JDBC_DATABASE_URL
        fromDatabase:
          name: vaultflow-db
          property: connectionString

databases:
  - name: vaultflow-db
    plan: free
    region: ohio
```

#### Opción 2: Configuración manual en Render Dashboard
1. New Web Service → Connect GitHub repo (branch `main`)
2. Build Command: `mvn package -DskipTests -B`
3. Start Command: `java -jar target/*.jar`
4. Health Check Path: `/actuator/health`
5. Environment Variables:
   - `SPRING_PROFILES_ACTIVE=prod`
   - `VAULTFLOW_JWT_SECRET=<valor-seguro>`
   - `CORS_ALLOWED_ORIGINS=https://vaultflow.netlify.app`
   - `JDBC_DATABASE_URL` (se obtiene al crear PostgreSQL DB)
6. PostgreSQL DB: New Database → PostgreSQL → Free → copiar Internal Database URL

### 4B — Netlify (Frontend UI)

1. Netlify Dashboard → Import from GitHub
2. Select repo `Taikaros/VaultFlow-Core`
3. Config:
   - Base directory: `frontend/`
   - Build command: `npm ci && npm run build`
   - Publish directory: `frontend/out/`
4. Environment Variables:
   - `NEXT_PUBLIC_API_URL=https://vaultflow-api.onrender.com/api/v1`
   - `NEXT_PUBLIC_WS_URL=wss://vaultflow-api.onrender.com/ws`
5. Deploy automático en cada push a `main`

### 4C — CI/CD Workflow (GitHub Actions)

**Estado actual**: Issue conocido — workflow no se ejecuta. Causa probable: validación de esquema en versiones recientes de acciones de GitHub.

Pasos:
1. Investigar logs de error en Actions de GitHub
2. Versiones de acciones a revisar: `docker/build-push-action@v6`, `softprops/action-gh-release@v2`
3. Alternativa: no depende de GHA — Render y Netlify deployan directo desde el repo

### Commits
```
feat(deploy): add Render configuration for backend API
Closes #15
feat(deploy): configure Netlify deployment for frontend
Closes #14
fix(ci): restore CI workflow functionality
```

---

## Fase 5 — Release v2.0.0

| Paso | Acción |
|------|--------|
| 5.1 | `scripts/bump-version.sh 2.0.0` |
| 5.2 | `npx standard-version --release-as minor` (o crear CHANGELOG manual) |
| 5.3 | Commit + push branch `feat/deploy-prep` |
| 5.4 | PR → `develop` (squash merge) |
| 5.5 | PR → `main` (merge) |
| 5.6 | `git tag -a v2.0.0 -m "v2.0.0: PostgreSQL, static export, deploy automático"` |
| 5.7 | `git push origin v2.0.0` |
| 5.8 | GitHub Release v2.0.0 (manual si CI no funciona) |
| 5.9 | Avanzar rama `deploy` a `main` |

---

## Verificación post-deploy

```bash
# Health check API
curl https://vaultflow-api.onrender.com/actuator/health
# → {"status":"UP"}

# Login API
curl -X POST https://vaultflow-api.onrender.com/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@techcorp.com","password":"admin123"}'
# → {"token":"eyJ..."}

# Frontend
open https://vaultflow.netlify.app
# → Login page carga, login funciona, dashboard muestra datos

# WebSocket (browser console)
# → STOMP connected
```

---

## Resumen de archivos

### NUEVOS
```
backend/src/main/resources/application-dev.yml
backend/src/main/resources/application-prod.yml
backend/src/main/java/com/vaultflow/config/CorsConfig.java
frontend/netlify.toml
frontend/public/_redirects
```

### MODIFICADOS
```
backend/pom.xml                          (+ PostgreSQL driver, Actuator)
backend/src/main/resources/application.yml (multi-perfil)
backend/src/main/resources/db/seed.sql   (sintaxis PostgreSQL)
backend/src/main/java/.../DataInitializer.java (soporte PostgreSQL)
frontend/next.config.mjs                 (output: export)
frontend/package.json                    (+ @stomp/stompjs, sockjs-client)
frontend/src/context/websocket.tsx       (STOMP + SockJS)
```

### OPCIONALES
```
render.yaml
```
