-- ALTER TABLE queries for Simple Stripe Payment Integration
-- Based on existing create_tables.sql

-- 1. First, let's add more payment statuses to the existing enum
ALTER TYPE payment_status ADD VALUE 'PROCESSING';
ALTER TYPE payment_status ADD VALUE 'REQUIRES_ACTION';
ALTER TYPE payment_status ADD VALUE 'CANCELED';
ALTER TYPE payment_status ADD VALUE 'REFUNDED';

-- 2. Modify the contributions table to track payment status
ALTER TABLE contributions 
ADD COLUMN payment_status payment_status DEFAULT 'PENDING',
ADD COLUMN payment_intent_id VARCHAR(255), -- Stripe Payment Intent ID
ADD COLUMN currency VARCHAR(3) DEFAULT 'USD',
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- 3. Modify the payments table for better Stripe integration
ALTER TABLE payments 
ADD COLUMN payment_intent_id VARCHAR(255), -- Stripe Payment Intent ID (same as contribution)
ADD COLUMN currency VARCHAR(3) DEFAULT 'USD',
ADD COLUMN amount DECIMAL(38,2), -- Store amount in payments table too
ADD COLUMN failure_reason TEXT, -- Store failure reason
ADD COLUMN payment_method_id VARCHAR(255), -- Stripe Payment Method ID
ADD COLUMN client_secret VARCHAR(255); -- Payment Intent client secret

-- 4. Add indexes for better performance
CREATE INDEX idx_contributions_payment_intent ON contributions(payment_intent_id);
CREATE INDEX idx_contributions_payment_status ON contributions(payment_status);
CREATE INDEX idx_payments_payment_intent ON payments(payment_intent_id);

-- 5. Add triggers for updated_at on contributions
CREATE TRIGGER update_contributions_updated_at
    BEFORE UPDATE ON contributions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 6. Add constraints to ensure data integrity
ALTER TABLE contributions 
ADD CONSTRAINT chk_contributions_currency CHECK (currency IN ('USD', 'EUR', 'GBP', 'CAD', 'AUD'));

ALTER TABLE payments 
ADD CONSTRAINT chk_payments_currency CHECK (currency IN ('USD', 'EUR', 'GBP', 'CAD', 'AUD')),
ADD CONSTRAINT chk_payments_amount CHECK (amount > 0);

-- 7. Make payment_intent_id unique per table (important for Stripe)
ALTER TABLE contributions 
ADD CONSTRAINT uq_contributions_payment_intent UNIQUE (payment_intent_id);

ALTER TABLE payments 
ADD CONSTRAINT uq_payments_payment_intent UNIQUE (payment_intent_id);

-- 8. Optional: Add a simple audit table for payment events (for debugging)
CREATE TABLE payment_audit (
    id BIGSERIAL PRIMARY KEY,
    contribution_id BIGINT REFERENCES contributions(id),
    payment_intent_id VARCHAR(255),
    old_status payment_status,
    new_status payment_status,
    event_data TEXT, -- JSON string from Stripe webhook
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payment_audit_contribution ON payment_audit(contribution_id);
CREATE INDEX idx_payment_audit_payment_intent ON payment_audit(payment_intent_id);
