# Flujo de Transacción (Pago)

```mermaid
sequenceDiagram
    actor Employee
    participant Dashboard
    participant API
    participant DB

    Employee->>Dashboard: Inicia pago
    Dashboard->>API: POST /transactions
    API->>DB: Validar tarjeta (ACTIVE)
    API->>DB: Validar límite (spent + amount <= limit)
    API->>DB: Validar saldo wallet origen
    API->>DB: INSERT transaction (PENDING)
    API->>DB: UPDATE wallet balance (origen - amount)
    API->>DB: UPDATE wallet balance (destino + amount)
    API->>DB: UPDATE card spent_amount
    API->>DB: UPDATE transaction status (COMPLETED)
    API-->>Dashboard: Transacción completada
    Dashboard-->>Employee: Pago exitoso
```

## Reglas de Negocio
- Una tarjeta no puede gastar más de su `limit_amount`
- Una wallet no puede tener saldo negativo
- El estado inicial de una transacción es `PENDING`
- Si todo valida, pasa a `COMPLETED`; si no, `FAILED`
