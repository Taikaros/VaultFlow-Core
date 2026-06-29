-- VaultFlow-Core Seed Data
-- Datos demo para desarrollo local
-- Se ejecuta solo si la tabla companies está vacía

INSERT INTO companies (id, name, tax_id, email, password)
VALUES ('comp-0001-0000-0000-000000000001', 'TechCorp Demo', 'TECH-2026-001', 'admin@techcorp.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy');

INSERT INTO wallets (id, company_id, balance, currency)
VALUES ('wallet-0001-0000-0000-000000000001', 'comp-0001-0000-0000-000000000001', 50000.00, 'USD');

INSERT INTO wallets (id, company_id, balance, currency)
VALUES ('wallet-0001-0000-0000-000000000002', 'comp-0001-0000-0000-000000000001', 10000.00, 'USD');

INSERT INTO cards (id, wallet_id, card_number, holder_name, status, limit_amount, spent_amount)
VALUES ('card-0001-0000-0000-000000000001', 'wallet-0001-0000-0000-000000000001',
        '4111111111111111', 'Admin Principal', 'ACTIVE', 5000.00, 0.00);

INSERT INTO cards (id, wallet_id, card_number, holder_name, status, limit_amount, spent_amount)
VALUES ('card-0001-0000-0000-000000000002', 'wallet-0001-0000-0000-000000000001',
        '4222222222222222', 'Marketing Team', 'ACTIVE', 2000.00, 500.00);

INSERT INTO transactions (id, from_card_id, to_wallet_id, amount, description, type, status)
VALUES ('tx-0001-0000-0000-000000000001', 'card-0001-0000-0000-000000000002',
        'wallet-0001-0000-0000-000000000002', 500.00, 'Campaña redes sociales', 'PAYMENT', 'COMPLETED');
