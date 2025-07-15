-- Remove duplicate payment_status column and fix column mapping
-- This script aligns database schema with Java PaymentStatus enum

-- Step 1: Update existing payment_status column with data from status column
UPDATE payments SET payment_status = 
    CASE 
        WHEN status = 'COMPLETED' THEN 'SUCCESSFUL'
        WHEN status = 'PENDING' THEN 'PENDING'
        WHEN status = 'FAILED' THEN 'FAILED'
        ELSE 'PENDING'
    END;

-- Step 2: Drop the old status column
ALTER TABLE payments DROP COLUMN status;

-- Step 3: Drop the old payment_status enum type since we're using VARCHAR now
DROP TYPE IF EXISTS payment_status CASCADE;

-- Step 4: Add constraint to ensure payment_status uses valid Java enum values (drop existing first)
ALTER TABLE payments DROP CONSTRAINT IF EXISTS chk_payment_status;
ALTER TABLE payments ADD CONSTRAINT chk_payment_status 
CHECK (payment_status IN ('PENDING', 'PROCESSING', 'SUCCESSFUL', 'FAILED', 'DECLINED', 'CANCELED'));

-- Step 5: Update the index to use the new column name
DROP INDEX IF EXISTS idx_payments_status;
CREATE INDEX idx_payments_status ON payments(payment_status);