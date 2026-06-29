# Flujo de Emisión de Tarjeta

```mermaid
sequenceDiagram
    actor Admin
    participant Dashboard
    participant API
    participant DB

    Admin->>Dashboard: Solicita emitir tarjeta
    Dashboard->>API: POST /wallets/{walletId}/cards
    API->>DB: Verificar wallet existe
    API->>API: Generar número de tarjeta
    API->>DB: INSERT card (ACTIVE)
    API-->>Dashboard: Card emitida
    Dashboard-->>Admin: Tarjeta lista
```

## Pasos
1. Admin selecciona una wallet activa
2. Ingresa nombre del titular y límite (opcional)
3. API genera número de tarjeta virtual (formato: `4xxx-xxxx-xxxx-xxxx`)
4. Tarjeta se crea en estado `ACTIVE`
5. La tarjeta ya puede usarse para pagos
