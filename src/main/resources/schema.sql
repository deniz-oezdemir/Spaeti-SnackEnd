DROP TABLE IF EXISTS CART_ITEM;
DROP TABLE IF EXISTS PRODUCT;
DROP TABLE IF EXISTS MEMBER;

CREATE TABLE PRODUCT
(
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    name      VARCHAR(255)   NOT NULL,
    price     DECIMAL(10, 2) NOT NULL,
    image_url VARCHAR(255)   NOT NULL
) engine=InnoDB;

CREATE TABLE MEMBER
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    email    VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(255) NOT NULL
) engine=InnoDB;

CREATE TABLE CART_ITEM
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id  BIGINT    NOT NULL,
    product_id BIGINT    NOT NULL,
    quantity   INT       NOT NULL DEFAULT 1,
    added_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (member_id, product_id),
    CONSTRAINT fk_member FOREIGN KEY (member_id) REFERENCES MEMBER (id) ON DELETE CASCADE,
    CONSTRAINT fk_product FOREIGN KEY (product_id) REFERENCES PRODUCT (id) ON DELETE CASCADE
);

CREATE INDEX idx_cart_item_cover ON cart_item(added_at, product_id);
CREATE INDEX idx_product_cover ON product(id, name);
