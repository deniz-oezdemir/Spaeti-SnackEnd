-- products
INSERT INTO PRODUCT (name, price, image_url)
VALUES ('Car', 1000.0, 'https://images.unsplash.com/photo-1494905998402-395d579af36f?w=400&h=400&fit=crop'),
       ('Bike', 200.0, 'https://images.unsplash.com/photo-1571068316344-75bc76f77890?w=400&h=400&fit=crop'),
       ('Truck', 30000.0, 'https://images.unsplash.com/photo-1586190848861-99aa4a171e90?w=400&h=400&fit=crop'),
       ('Laptop', 1500.0, 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=400&h=400&fit=crop'),
       ('Phone', 800.0, 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400&h=400&fit=crop'),
       ('Product 6', 12.34, 'https://placeholder.vn/placeholder/400x400?bg=ff7f50&color=ffffff&text=Product6'),
       ('Product 7', 23.45, 'https://placeholder.vn/placeholder/400x400?bg=6a5acd&color=ffffff&text=Product7'),
       ('Product 8', 34.56, 'https://placeholder.vn/placeholder/400x400?bg=20b2aa&color=ffffff&text=Product8'),
       ('Product 9', 45.67, 'https://placeholder.vn/placeholder/400x400?bg=ff4500&color=ffffff&text=Product9'),
       ('Product 10', 56.78, 'https://placeholder.vn/placeholder/400x400?bg=2e8b57&color=ffffff&text=Product10'),
       ('Product 11', 67.89, 'https://placeholder.vn/placeholder/400x400?bg=800080&color=ffffff&text=Product11'),
       ('Product 12', 78.90, 'https://placeholder.vn/placeholder/400x400?bg=ff1493&color=ffffff&text=Product12'),
       ('Product 13', 89.01, 'https://placeholder.vn/placeholder/400x400?bg=4682b4&color=ffffff&text=Product13'),
       ('Product 14', 90.12, 'https://placeholder.vn/placeholder/400x400?bg=daa520&color=ffffff&text=Product14'),
       ('Product 15', 10.23, 'https://placeholder.vn/placeholder/400x400?bg=008b8b&color=ffffff&text=Product15'),
       ('Product 16', 21.34, 'https://placeholder.vn/placeholder/400x400?bg=ff6347&color=ffffff&text=Product16'),
       ('Product 17', 32.45, 'https://placeholder.vn/placeholder/400x400?bg=7f00ff&color=ffffff&text=Product17'),
       ('Product 18', 43.56, 'https://placeholder.vn/placeholder/400x400?bg=00ced1&color=ffffff&text=Product18'),
       ('Product 19', 54.67, 'https://placeholder.vn/placeholder/400x400?bg=dc143c&color=ffffff&text=Product19'),
       ('Product 20', 65.78, 'https://placeholder.vn/placeholder/400x400?bg=1e90ff&color=ffffff&text=Product20'),
       ('Product 21', 76.89, 'https://placeholder.vn/placeholder/400x400?bg=ff00ff&color=ffffff&text=Product21'),
       ('Product 22', 87.90, 'https://placeholder.vn/placeholder/400x400?bg=32cd32&color=ffffff&text=Product22'),
       ('Product 23', 98.01, 'https://placeholder.vn/placeholder/400x400?bg=ff8c00&color=ffffff&text=Product23'),
       ('Product 24', 109.12, 'https://placeholder.vn/placeholder/400x400?bg=4169e1&color=ffffff&text=Product24'),
       ('Product 25', 120.34, 'https://placeholder.vn/placeholder/400x400?bg=8b4513&color=ffffff&text=Product25');

-- 10 admin
INSERT INTO MEMBER (email, password, role)
VALUES ('sebas@sebas.com', '123456', 'ADMIN');

-- 10 customers
INSERT INTO MEMBER (email, password, role)
VALUES ('user1@example.com', 'pass', 'CUSTOMER'),
       ('user2@example.com', 'pass', 'CUSTOMER'),
       ('user3@example.com', 'pass', 'CUSTOMER'),
       ('user4@example.com', 'pass', 'CUSTOMER'),
       ('user5@example.com', 'pass', 'CUSTOMER'),
       ('user6@example.com', 'pass', 'CUSTOMER'),
       ('user7@example.com', 'pass', 'CUSTOMER'),
       ('user8@example.com', 'pass', 'CUSTOMER'),
       ('user9@example.com', 'pass', 'CUSTOMER'),
       ('user10@example.com', 'pass', 'CUSTOMER');

-- 20 random cart_item entries
INSERT INTO CART_ITEM (member_id, product_id, quantity, added_at)
VALUES (2, 6, 1, CURRENT_TIMESTAMP()),
       (2, 7, 2, CURRENT_TIMESTAMP()),
       (3, 8, 1, CURRENT_TIMESTAMP()),
       (3, 9, 3, CURRENT_TIMESTAMP()),
       (4, 10, 1, CURRENT_TIMESTAMP()),
       (4, 11, 2, CURRENT_TIMESTAMP()),
       (5, 12, 1, CURRENT_TIMESTAMP()),
       (5, 13, 4, CURRENT_TIMESTAMP()),
       (6, 14, 2, CURRENT_TIMESTAMP()),
       (6, 15, 1, CURRENT_TIMESTAMP()),
       (7, 16, 3, CURRENT_TIMESTAMP()),
       (7, 17, 1, CURRENT_TIMESTAMP()),
       (8, 18, 2, CURRENT_TIMESTAMP()),
       (8, 19, 1, CURRENT_TIMESTAMP()),
       (9, 20, 1, CURRENT_TIMESTAMP()),
       (9, 21, 2, CURRENT_TIMESTAMP()),
       (10, 22, 1, CURRENT_TIMESTAMP()),
       (10, 23, 3, CURRENT_TIMESTAMP()),
       (2, 24, 1, CURRENT_TIMESTAMP()),
       (3, 25, 2, CURRENT_TIMESTAMP());