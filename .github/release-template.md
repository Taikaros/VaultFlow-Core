## VaultFlow-Core ${{ github.ref_name }}

### Cambios desde la versión anterior

- Roles de usuario (ADMIN/EMPLOYEE) con CRUD completo
- WebSockets para notificaciones en tiempo real (STOMP/SockJS)
- Dashboard con métricas agregadas y gráfico de transacciones
- Paginación en transacciones y listado de usuarios
- README completo con documentación de setup y API

### Descarga

- **Backend (JAR):** `vaultflow-core-*.jar` — requiere Java 21
- **Frontend:** `vaultflow-frontend.tar.gz` — requiere Node.js 20 (extraer y correr `npm start`)

### Docker

```bash
docker pull <username>/vaultflow-backend:${{ github.ref_name }}
docker pull <username>/vaultflow-frontend:${{ github.ref_name }}
```

### Seed Data

Usuario demo: `admin@techcorp.com` / `admin123`
