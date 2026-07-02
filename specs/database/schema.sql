-- VaultFlow-Core Database Schema
-- SQLite DDL v1

CREATE TABLE IF NOT EXISTS companies (
    id          TEXT PRIMARY KEY DEFAULT (lower(hex(randomblob(16)))),
    name        TEXT NOT NULL,
    tax_id      TEXT NOT NULL UNIQUE,
    email       TEXT NOT NULL UNIQUE,
    password    TEXT NOT NULL,
    created_at  TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at  TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS wallets (
    id          TEXT PRIMARY KEY DEFAULT (lower(hex(randomblob(16)))),
    company_id  TEXT NOT NULL,
    balance     REAL NOT NULL DEFAULT 0.00,
    currency    TEXT NOT NULL DEFAULT 'USD',
    created_at  TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at  TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS users (
    id          TEXT PRIMARY KEY DEFAULT (lower(hex(randomblob(16)))),
    company_id  TEXT NOT NULL,
    wallet_id   TEXT,
    email       TEXT NOT NULL UNIQUE,
    name        TEXT NOT NULL,
    role        TEXT NOT NULL DEFAULT 'EMPLOYEE' CHECK (role IN ('ADMIN', 'EMPLOYEE')),
    password    TEXT NOT NULL,
    created_at  TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    FOREIGN KEY (wallet_id) REFERENCES wallets(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS cards (
    id            TEXT PRIMARY KEY DEFAULT (lower(hex(randomblob(16)))),
    wallet_id     TEXT NOT NULL,
    card_number   TEXT NOT NULL UNIQUE,
    holder_name   TEXT NOT NULL,
    status        TEXT NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'SUSPENDED', 'CANCELLED')),
    limit_amount  REAL NOT NULL DEFAULT 1000.00,
    spent_amount  REAL NOT NULL DEFAULT 0.00,
    created_at    TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (wallet_id) REFERENCES wallets(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS transactions (
    id                TEXT PRIMARY KEY DEFAULT (lower(hex(randomblob(16)))),
    from_card_id      TEXT,
    to_wallet_id      TEXT NOT NULL,
    amount            REAL NOT NULL CHECK (amount > 0),
    description       TEXT,
    type              TEXT NOT NULL DEFAULT 'PAYMENT' CHECK (type IN ('PAYMENT', 'TOPUP', 'REFUND')),
    status            TEXT NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED')),
    original_amount   REAL,
    original_currency TEXT,
    conversion_rate   REAL,
    created_at        TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (from_card_id) REFERENCES cards(id) ON DELETE SET NULL,
    FOREIGN KEY (to_wallet_id) REFERENCES wallets(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_wallets_company ON wallets(company_id);
CREATE INDEX idx_users_company ON users(company_id);
CREATE INDEX idx_users_wallet ON users(wallet_id);
CREATE INDEX idx_cards_wallet ON cards(wallet_id);
CREATE INDEX idx_cards_status ON cards(status);
CREATE INDEX idx_transactions_card ON transactions(from_card_id);
CREATE INDEX idx_transactions_wallet ON transactions(to_wallet_id);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_created ON transactions(created_at);
