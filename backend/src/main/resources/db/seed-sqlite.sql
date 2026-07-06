-- VaultFlow-Core Seed Data for SQLite (dev profile)

INSERT OR IGNORE INTO companies (id, name, tax_id, email, password, created_at, updated_at)
VALUES (hex(randomblob(16)), 'TechCorp Demo', 'TECH-2026-001', 'admin@techcorp.com',
        '$2b$10$MLWItGcXOwPfszldHtGd9uYK1Vv0OnqE8Ew/BjlPxHq03o7xpU4Qm', datetime('now'), datetime('now'));

INSERT OR IGNORE INTO wallets (id, company_id, balance, currency, created_at, updated_at)
SELECT hex(randomblob(16)), id, 50000.00, 'USD', datetime('now'), datetime('now')
FROM companies WHERE email = 'admin@techcorp.com';

INSERT OR IGNORE INTO wallets (id, company_id, balance, currency, created_at, updated_at)
SELECT hex(randomblob(16)), id, 10000.00, 'USD', datetime('now'), datetime('now')
FROM companies WHERE email = 'admin@techcorp.com';

INSERT OR IGNORE INTO cards (id, wallet_id, card_number, holder_name, status, limit_amount, spent_amount, created_at)
SELECT hex(randomblob(16)), id, '4111111111111111', 'Admin Principal', 'ACTIVE', 5000.00, 0.00, datetime('now')
FROM wallets
WHERE NOT EXISTS (SELECT 1 FROM cards WHERE card_number = '4111111111111111');

INSERT OR IGNORE INTO cards (id, wallet_id, card_number, holder_name, status, limit_amount, spent_amount, created_at)
SELECT hex(randomblob(16)), id, '4222222222222222', 'Marketing Team', 'ACTIVE', 2000.00, 500.00, datetime('now')
FROM wallets
WHERE NOT EXISTS (SELECT 1 FROM cards WHERE card_number = '4222222222222222');

INSERT OR IGNORE INTO transactions (id, from_card_id, to_wallet_id, amount, description, type, status, created_at)
SELECT hex(randomblob(16)), c.id, w.id, 500.00, 'Campaña redes sociales', 'PAYMENT', 'COMPLETED', datetime('now')
FROM cards c, wallets w
WHERE c.card_number = '4222222222222222'
  AND w.balance = 10000.00;
