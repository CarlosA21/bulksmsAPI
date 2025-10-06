-- Add validation image columns to User table if they don't exist

ALTER TABLE User
ADD COLUMN IF NOT EXISTS validation_image_path VARCHAR(500),
ADD COLUMN IF NOT EXISTS validation_image_name VARCHAR(255),
ADD COLUMN IF NOT EXISTS account_validated BOOLEAN DEFAULT FALSE;

-- Update existing users to have account_validated as false if null
UPDATE User SET account_validated = FALSE WHERE account_validated IS NULL;

