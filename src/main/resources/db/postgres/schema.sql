-- ========== TABLES ==========

CREATE TABLE products (
                          id SERIAL PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          price NUMERIC(10,2) NOT NULL,
                          image_url VARCHAR(500)
);

CREATE TABLE options (
                         id SERIAL PRIMARY KEY,
                         product_id INT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
                         name VARCHAR(255) NOT NULL,
                         quantity INT NOT NULL
);