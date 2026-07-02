# Changelog

## v1.1.1 (2026-07-02)

### 🏗️ Infraestructura
- Sistema de versionado con single source of truth (`VERSION` file + `scripts/bump-version.sh`)
- Conventional Commits validados automáticamente (commitlint + husky)
- standard-version para bumps automáticos de versión
- Pre-release support (alpha, beta, rc) en CI/CD
- Annotated tags para releases oficiales

### 📝 Documentación
- `CONTRIBUTING.md` con guía completa de contribución y versionado
- README sincronizado con estado actual del proyecto
- OpenAPI spec actualizada a v1.1.0
- CHANGELOG.md restaurado en develop

### 🐛 Correcciones
- Version drift corregido: pom.xml y package.json sincronizados a 1.1.0
- Artifact naming del JAR ahora refleja la versión real
- Pre-releases ya no sobreescriben tag Docker `:latest`

---
## v1.1.0 (2026-06-29)

### ✨ Features
- Roles de usuario (ADMIN/EMPLOYEE) con CRUD completo desde el frontend
- WebSockets para notificaciones en tiempo real (STOMP/SockJS)
- Dashboard con métricas agregadas y gráfico de torta (Recharts)
- Paginación en transacciones y listado de usuarios

### 🏗️ Infraestructura
- Estrategia de ramas: main, develop, deploy, feature/*, fix/*
- CI/CD pipeline con release automático al pushear tags
- Docker job condicional (requiere credenciales Docker Hub)

### 📝 Documentación
- README completo con badges, estrategia de ramas, workflow CI, proceso de release
- OpenAPI spec actualizada a v1.1.0 (todos los endpoints documentados)
- CHANGELOG.md separado del README

### 🐛 Correcciones
- Test TransactionService actualizado para incluir NotificationService
- Test de integración de transacciones corregido para paginación
- CI: packaging de frontend sin directorio public/

---

## v1.0.1 (2026-06-28)

### 🧪 Testing
- 40 tests: 25 unitarios (Mockito) + 15 de integración (H2)
- Cobertura de AuthService, CardService, TransactionService, WalletService
- Tests de integración para Auth, Card, Transaction, Wallet controllers

### ⚙️ Configuración
- Seed data automática con usuario demo
- DataInitializer con detección de SQLite vs H2
- CI pipeline con GitHub Actions (JDK 21 + Node 20)
- `.gitignore` completo para Java + Node + SQLite

---

## v1.0.0 (2026-06-27)

### 🚀 MVP Inicial
- Registro y autenticación de empresas (JWT + BCrypt)
- CRUD de wallets con saldo en USD
- Emisión y gestión de tarjetas virtuales (ACTIVE/SUSPENDED/CANCELLED)
- Pagos entre tarjetas y wallets con validación de fondos y límites
- Frontend con login, registro y dashboard básico

### 🛠️ Stack
- Backend: Java 21, Spring Boot 3.3.5, Spring Data JPA, SQLite
- Frontend: Next.js 14 (App Router), React 18, shadcn/ui, Tailwind CSS
- Seguridad: JWT, Spring Security, BCrypt
- Documentación: SpringDoc OpenAPI (Swagger UI)

### 📦 DevOps
- Docker multi-stage para backend y frontend
- Docker Compose para desarrollo local
- Especificaciones: OpenAPI 3.1, DDL SQLite, diagramas Mermaid
