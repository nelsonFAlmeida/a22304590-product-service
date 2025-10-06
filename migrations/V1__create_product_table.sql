CREATE TABLE IF NOT EXISTS product (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(120) NOT NULL,
    description TEXT,
    sku VARCHAR(255) NOT NULL UNIQUE,
    price NUMERIC(15,2) NOT NULL CHECK (price >= 0),
    stock INT NOT NULL CHECK (stock >= 0),
    currency CHAR(3) NOT NULL DEFAULT 'EUR',
    supplier_id UUID,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
    );

CREATE INDEX IF NOT EXISTS idx_product_sku ON product(sku);
CREATE INDEX IF NOT EXISTS idx_product_supplier_id ON product(supplier_id);
CREATE INDEX IF NOT EXISTS idx_product_name ON product(name);