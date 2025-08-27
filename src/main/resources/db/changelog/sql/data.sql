-- ========== PRODUCTS ==========
INSERT INTO products (name, price, image_url) VALUES
                                                  ('Coca-Cola',       1.49, 'https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/spaeti-demo-products/product-images/cocacola-original.png'),
                                                  ('Fanta',           1.49, 'https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/spaeti-demo-products/product-images/fanta-orange.png'),
                                                  ('Apfelschorle',    1.59, 'https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/spaeti-demo-products/product-images/apfelschorle-bio.png'),
                                                  ('Fritz-Kola',      1.99, 'https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/spaeti-demo-products/product-images/fritz-kola-kola.png'),
                                                  ('Club-Mate',       1.89, 'https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/spaeti-demo-products/product-images/+club-mate-original.png'),
                                                  ('Water',           0.99, 'https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/spaeti-demo-products/product-images/+water-gerolsteiner-naturell.png'),
                                                  ('Mr. Tom',         0.99, 'https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/spaeti-demo-products/product-images/mr-tom-erdnussriegel.png'),
                                                  ('Lorenz Chips',    1.79, 'https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/spaeti-demo-products/product-images/lorenz-crunchips-salted.png'),
                                                  ('Haribo',          1.49, 'https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/spaeti-demo-products/product-images/haribo-tropifrutti.png'),
                                                  ('Takis',           2.49, 'https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/spaeti-demo-products/product-images/takis-fuego.png'),
                                                  ('Studentenfutter', 1.99, 'https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/spaeti-demo-products/product-images/studentenfutter-seeberger.png');

-- ========== OPTIONS / VARIANTS ==========
-- Coca-Cola
INSERT INTO options (product_id, name, quantity, version) VALUES
                                                              ((SELECT id FROM products WHERE name='Coca-Cola'), 'original', 100, 0),
                                                              ((SELECT id FROM products WHERE name='Coca-Cola'), 'zero',     100, 0);

-- Fanta
INSERT INTO options (product_id, name, quantity, version) VALUES
                                                              ((SELECT id FROM products WHERE name='Fanta'), 'orange', 100, 0),
                                                              ((SELECT id FROM products WHERE name='Fanta'), 'lemon',  100, 0);

-- Apfelschorle
INSERT INTO options (product_id, name, quantity, version) VALUES
                                                              ((SELECT id FROM products WHERE name='Apfelschorle'), 'bio',      100, 0),
                                                              ((SELECT id FROM products WHERE name='Apfelschorle'), 'non-bio',  100, 0);

-- Fritz-Kola
INSERT INTO options (product_id, name, quantity, version) VALUES
    ((SELECT id FROM products WHERE name='Fritz-Kola'), 'kola', 100, 0);

-- Club-Mate
INSERT INTO options (product_id, name, quantity, version) VALUES
                                                              ((SELECT id FROM products WHERE name='Club-Mate'), 'original', 100, 0),
                                                              ((SELECT id FROM products WHERE name='Club-Mate'), 'zero',     100, 0);

-- Water
INSERT INTO options (product_id, name, quantity, version) VALUES
                                                              ((SELECT id FROM products WHERE name='Water'), 'gerolsteiner-naturell', 100, 0),
                                                              ((SELECT id FROM products WHERE name='Water'), 'kondrauer-sparkling',   100, 0);

-- Mr. Tom
INSERT INTO options (product_id, name, quantity, version) VALUES
                                                              ((SELECT id FROM products WHERE name='Mr. Tom'), 'erdnussriegel',   100, 0),
                                                              ((SELECT id FROM products WHERE name='Mr. Tom'), 'salted-caramel',  100, 0);

-- Lorenz Chips
INSERT INTO options (product_id, name, quantity, version) VALUES
                                                              ((SELECT id FROM products WHERE name='Lorenz Chips'), 'crunchips-salted',   100, 0),
                                                              ((SELECT id FROM products WHERE name='Lorenz Chips'), 'naturals-rosmarin',  100, 0),
                                                              ((SELECT id FROM products WHERE name='Lorenz Chips'), 'naturals-balsamico', 100, 0);

-- Haribo
INSERT INTO options (product_id, name, quantity, version) VALUES
                                                              ((SELECT id FROM products WHERE name='Haribo'), 'tropifrutti', 100, 0),
                                                              ((SELECT id FROM products WHERE name='Haribo'), 'goldbaeren',  100, 0);

-- Takis
INSERT INTO options (product_id, name, quantity, version) VALUES
                                                              ((SELECT id FROM products WHERE name='Takis'), 'fuego',            100, 0),
                                                              ((SELECT id FROM products WHERE name='Takis'), 'nachos-xplosion',  100, 0);

-- Studentenfutter
INSERT INTO options (product_id, name, quantity, version) VALUES
                                                              ((SELECT id FROM products WHERE name='Studentenfutter'), 'seeberger', 100, 0),
                                                              ((SELECT id FROM products WHERE name='Studentenfutter'), 'clasen-bio', 100, 0);