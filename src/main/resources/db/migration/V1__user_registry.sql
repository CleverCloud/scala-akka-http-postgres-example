CREATE TABLE IF NOT EXISTS user_registry (
    id serial PRIMARY KEY,
    name VARCHAR ( 255 ) UNIQUE NOT NULL,
    age INT NOT NULL,
    country VARCHAR ( 255 ) NOT NULL
)