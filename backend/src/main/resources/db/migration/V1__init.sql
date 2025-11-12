CREATE TYPE user_type AS ENUM ('BUSINESS','RETAILER');

CREATE TABLE app_user (
  id            BIGSERIAL PRIMARY KEY,
  username      VARCHAR(50)  NOT NULL UNIQUE,
  email         VARCHAR(120) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  full_name     VARCHAR(120),
  phone         VARCHAR(30),
  address_line1 VARCHAR(200),
  address_line2 VARCHAR(200),
  city          VARCHAR(80),
  state         VARCHAR(80),
  postal_code   VARCHAR(20),
  country       VARCHAR(80),
  user_type     user_type     NOT NULL,
  created_at    TIMESTAMP     NOT NULL DEFAULT NOW(),
  updated_at    TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_app_user_email ON app_user(email);
