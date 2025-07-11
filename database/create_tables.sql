-- PostgreSQL DDL for Crowdfunding Database

-- Drop tables if they exist (in reverse order due to foreign key constraints)
DROP TABLE IF EXISTS payments CASCADE;
DROP TABLE IF EXISTS likes CASCADE;
DROP TABLE IF EXISTS comments CASCADE;
DROP TABLE IF EXISTS contributions CASCADE;
DROP TABLE IF EXISTS campaign_images CASCADE;
DROP TABLE IF EXISTS campaigns CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Create ENUM types first
CREATE TYPE campaign_status AS ENUM ('DRAFT', 'PENDING', 'ACTIVE', 'SUCCESSFUL', 'FAILED', 'CANCELED');
CREATE TYPE payment_status AS ENUM ('PENDING', 'COMPLETED', 'FAILED');

-- 1. Users Table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    avatar_url TEXT,
    bio TEXT,
    stripe_account_id VARCHAR(255),
    stripe_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Categories Table
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    icon_url TEXT,
    active BOOLEAN DEFAULT TRUE
);

-- 3. Campaigns Table
CREATE TABLE campaigns (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    short_description TEXT,
    full_description TEXT,
    funding_goal DECIMAL(38,2) NOT NULL,
    current_amount DECIMAL(38,2) DEFAULT 0,
    deadline TIMESTAMP NOT NULL,
    status campaign_status DEFAULT 'DRAFT',
    category_id BIGINT NOT NULL,
    creator_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_campaigns_category FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT fk_campaigns_creator FOREIGN KEY (creator_id) REFERENCES users(id)
);

-- 4. Campaign Images Table
CREATE TABLE campaign_images (
    id BIGSERIAL PRIMARY KEY,
    campaign_id BIGINT NOT NULL,
    image_url TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_campaign_images_campaign FOREIGN KEY (campaign_id) REFERENCES campaigns(id) ON DELETE CASCADE
);

-- 5. Contributions Table
CREATE TABLE contributions (
    id BIGSERIAL PRIMARY KEY,
    campaign_id BIGINT NOT NULL,
    user_id BIGINT NULL, -- NULL for anonymous contributions
    amount DECIMAL(38,2) NOT NULL,
    is_anonymous BOOLEAN DEFAULT FALSE,
    display_name VARCHAR(255), -- For anonymous users who want to show a name
    message TEXT, -- Optional message from contributor
    contributed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_contributions_campaign FOREIGN KEY (campaign_id) REFERENCES campaigns(id),
    CONSTRAINT fk_contributions_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT chk_contributions_amount CHECK (amount > 0)
);

-- 6. Comments Table
CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    campaign_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_comments_campaign FOREIGN KEY (campaign_id) REFERENCES campaigns(id),
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 7. Likes Table
CREATE TYPE like_type AS ENUM ('like', 'dislike', 'love', 'laugh', 'angry', 'sad');

CREATE TABLE likes (
    id BIGSERIAL PRIMARY KEY,
    campaign_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    like_type like_type NOT NULL DEFAULT 'like',
    liked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_likes_campaign FOREIGN KEY (campaign_id) REFERENCES campaigns(id),
    CONSTRAINT fk_likes_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT unique_user_campaign_like UNIQUE (campaign_id, user_id)
);

-- 8. Payments Table
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    contribution_id BIGINT NOT NULL,
    stripe_payment_id VARCHAR(255) UNIQUE NOT NULL,
    status payment_status DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_payments_contribution FOREIGN KEY (contribution_id) REFERENCES contributions(id)
);

-- Create indexes for better performance
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_campaigns_creator ON campaigns(creator_id);
CREATE INDEX idx_campaigns_category ON campaigns(category_id);
CREATE INDEX idx_campaigns_status ON campaigns(status);
CREATE INDEX idx_campaigns_deadline ON campaigns(deadline);
CREATE INDEX idx_campaign_images_campaign ON campaign_images(campaign_id);
CREATE INDEX idx_contributions_campaign ON contributions(campaign_id);
CREATE INDEX idx_contributions_user ON contributions(user_id);
CREATE INDEX idx_contributions_amount ON contributions(amount DESC);
CREATE INDEX idx_contributions_date ON contributions(contributed_at DESC);
CREATE INDEX idx_contributions_campaign_amount ON contributions(campaign_id, amount DESC);
CREATE INDEX idx_comments_campaign ON comments(campaign_id);
CREATE INDEX idx_comments_user ON comments(user_id);
CREATE INDEX idx_comments_date ON comments(created_at DESC);
CREATE INDEX idx_likes_campaign ON likes(campaign_id);
CREATE INDEX idx_likes_user ON likes(user_id);
CREATE INDEX idx_payments_contribution ON payments(contribution_id);
CREATE INDEX idx_payments_stripe ON payments(stripe_payment_id);

-- Create a function to automatically update the updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create triggers for auto-updating updated_at columns
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_campaigns_updated_at
    BEFORE UPDATE ON campaigns
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_payments_updated_at
    BEFORE UPDATE ON payments
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Insert some sample categories
INSERT INTO categories (name, description, icon_url) VALUES
('Technology', 'Tech startups, apps, and gadgets', 'https://example.com/icons/tech.svg'),
('Arts & Crafts', 'Creative projects, art, and handmade items', 'https://example.com/icons/arts.svg'),
('Health & Fitness', 'Health-related products and fitness equipment', 'https://example.com/icons/health.svg'),
('Education', 'Educational projects and learning tools', 'https://example.com/icons/education.svg'),
('Environment', 'Environmental and sustainability projects', 'https://example.com/icons/environment.svg'),
('Social Causes', 'Charitable and social impact projects', 'https://example.com/icons/social.svg'),
('Gaming', 'Video games and gaming accessories', 'https://example.com/icons/gaming.svg'),
('Food & Beverage', 'Food products and restaurants', 'https://example.com/icons/food.svg');

-- Create view for campaign statistics
CREATE VIEW campaign_stats AS
SELECT 
    c.id,
    c.title,
    c.funding_goal,
    c.current_amount,
    c.deadline,
    c.status,
    COALESCE(contrib_stats.total_contributions, 0) as total_contributions,
    COALESCE(contrib_stats.contributor_count, 0) as contributor_count,
    COALESCE(comment_stats.comment_count, 0) as comment_count,
    COALESCE(like_stats.like_count, 0) as like_count,
    CASE 
        WHEN c.funding_goal > 0 THEN 
            ROUND((c.current_amount / c.funding_goal * 100), 2)
        ELSE 0 
    END as progress_percentage
FROM campaigns c
LEFT JOIN (
    SELECT 
        campaign_id,
        COUNT(*) as total_contributions,
        COUNT(DISTINCT user_id) as contributor_count
    FROM contributions
    GROUP BY campaign_id
) contrib_stats ON c.id = contrib_stats.campaign_id
LEFT JOIN (
    SELECT 
        campaign_id,
        COUNT(*) as comment_count
    FROM comments
    GROUP BY campaign_id
) comment_stats ON c.id = comment_stats.campaign_id
LEFT JOIN (
    SELECT 
        campaign_id,
        COUNT(*) as like_count
    FROM likes
    GROUP BY campaign_id
) like_stats ON c.id = like_stats.campaign_id;


ALTER TABLE campaigns
  ALTER COLUMN title DROP NOT NULL,
  ALTER COLUMN funding_goal DROP NOT NULL,
  ALTER COLUMN deadline DROP NOT NULL,
  ALTER COLUMN category_id DROP NOT NULL;
