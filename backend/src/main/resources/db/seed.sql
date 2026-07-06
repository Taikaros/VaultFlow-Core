-- VaultFlow-Core Seed Data
-- Datos demo para desarrollo y PostgreSQL
-- Se ejecuta solo si la tabla companies está vacía

INSERT INTO companies (id, name, tax_id, email, password, created_at, updated_at)
VALUES (gen_random_uuid()::text, 'TechCorp Demo', 'TECH-2026-001', 'admin@techcorp.com',
        '$2b$10$MLWItGcXOwPfszldHtGd9uYK1Vv0OnqE8Ew/BjlPxHq03o7xpU4Qm', NOW(), NOW())
ON CONFLICT DO NOTHING;

INSERT INTO wallets (id, company_id, balance, currency, created_at, updated_at)
SELECT gen_random_uuid()::text, id, 50000.00, 'USD', NOW(), NOW()
FROM companies WHERE email = 'admin@techcorp.com'
AND NOT EXISTS (SELECT 1 FROM wallets WHERE balance = 50000.00);

INSERT INTO wallets (id, company_id, balance, currency, created_at, updated_at)
SELECT gen_random_uuid()::text, id, 10000.00, 'USD', NOW(), NOW()
FROM companies WHERE email = 'admin@techcorp.com'
AND NOT EXISTS (SELECT 1 FROM wallets WHERE balance = 10000.00);

INSERT INTO cards (id, wallet_id, card_number, holder_name, status, limit_amount, spent_amount, created_at)
SELECT gen_random_uuid()::text, id, '4111111111111111', 'Admin Principal', 'ACTIVE', 5000.00, 0.00, NOW()
FROM wallets
WHERE NOT EXISTS (SELECT 1 FROM cards WHERE card_number = '4111111111111111');

INSERT INTO cards (id, wallet_id, card_number, holder_name, status, limit_amount, spent_amount, created_at)
SELECT gen_random_uuid()::text, id, '4222222222222222', 'Marketing Team', 'ACTIVE', 2000.00, 500.00, NOW()
FROM wallets
WHERE NOT EXISTS (SELECT 1 FROM cards WHERE card_number = '4222222222222222');

INSERT INTO transactions (id, from_card_id, to_wallet_id, amount, description, type, status, created_at)
SELECT gen_random_uuid()::text, c.id, w.id, 500.00, 'Campaña redes sociales', 'PAYMENT', 'COMPLETED', NOW()
FROM cards c, wallets w
WHERE c.card_number = '4222222222222222'
  AND w.balance = 10000.00
  AND NOT EXISTS (SELECT 1 FROM transactions WHERE amount = 500.00 AND description = 'Campaña redes sociales');
