-- This script is corrected to use the built-in gen_random_uuid() function.
-- There is no need to create any extension.

-- ## Users Table ##
-- Stores user account information.
CREATE TABLE users (
                       user_id SERIAL PRIMARY KEY,
                       uuid UUID DEFAULT gen_random_uuid() NOT NULL,
                       user_name VARCHAR(255) UNIQUE NOT NULL,
                       first_name VARCHAR(255),
                       last_name VARCHAR(255),
                       email VARCHAR(255) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(50) DEFAULT 'user' NOT NULL,
                       enabled BOOLEAN DEFAULT FALSE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- ✅ FIX: Changed from TIMESTAMP WITH TIME ZONE
);
drop table users cascade ;
truncate table user_profile restart identity cascade ;
CREATE TABLE user_profile
(
    profile_id    SERIAL PRIMARY KEY,
    user_id       BIGINT REFERENCES users (user_id) ON DELETE CASCADE UNIQUE,
    user_name     VARCHAR(50),
    first_name    VARCHAR(50),
    last_name     VARCHAR(50),
    birthday      DATE,
    gender        VARCHAR(10),
    phone_number  VARCHAR(20),
    profile_image TEXT,
    address       TEXT

);


CREATE TABLE otp_number (
                            id SERIAL PRIMARY KEY,
                            email VARCHAR(255) NOT NULL,
                            otp VARCHAR(6) NOT NULL,
                            created_at TIMESTAMP NOT NULL,
                            verified BOOLEAN DEFAULT FALSE
);

-- ## Products Table ##
-- Stores base product information.
CREATE TABLE product (
                         product_id SERIAL PRIMARY KEY,
                         uuid UUID DEFAULT gen_random_uuid() NOT NULL,
                         name VARCHAR(255) NOT NULL,
                         description TEXT,
                         base_price NUMERIC(10, 2) NOT NULL,
                         discount_percent INT DEFAULT 0,
                         final_price NUMERIC(10, 2) GENERATED ALWAYS AS (base_price * (1 - discount_percent / 100.0)) STORED,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

alter table product add column is_available BOOLEAN DEFAULT TRUE;
drop table product cascade ;
delete from users where user_id = 2;

-- ## Size Table ##
-- Stores available product sizes.
CREATE TABLE size (
                      size_id SERIAL PRIMARY KEY,
                      uuid UUID DEFAULT gen_random_uuid() NOT NULL,
                      name VARCHAR(50) UNIQUE NOT NULL
);

-- ## Product Variant Table ##
-- Stores different product variations, like by color.
CREATE TABLE product_variant (
                                 variant_id SERIAL PRIMARY KEY,
                                 uuid UUID DEFAULT gen_random_uuid() NOT NULL,
                                 product_id INT NOT NULL,
                                 color VARCHAR(50),
                                 CONSTRAINT fk_product
                                     FOREIGN KEY(product_id)
                                         REFERENCES product(product_id)
                                         ON DELETE CASCADE
);

-- ## Product Image Table ##
-- Stores images for each product variant.
CREATE TABLE product_image (
                               image_id SERIAL PRIMARY KEY,
                               uuid UUID DEFAULT gen_random_uuid() NOT NULL,
                               variant_id INT NOT NULL,
                               image_url VARCHAR(255) NOT NULL,
                               CONSTRAINT fk_variant
                                   FOREIGN KEY(variant_id)
                                       REFERENCES product_variant(variant_id)
                                       ON DELETE CASCADE
);

-- ## Variant Size Table (Inventory) ##
-- Maps sizes to variants and tracks availability.
CREATE TABLE variant_size (
                              id SERIAL PRIMARY KEY,
                              uuid UUID DEFAULT gen_random_uuid() NOT NULL,
                              variant_id INT NOT NULL,
                              size_id INT NOT NULL,
                              is_available BOOLEAN DEFAULT TRUE,
                              CONSTRAINT fk_variant
                                  FOREIGN KEY(variant_id)
                                      REFERENCES product_variant(variant_id)
                                      ON DELETE CASCADE,
                              CONSTRAINT fk_size
                                  FOREIGN KEY(size_id)
                                      REFERENCES size(size_id)
                                      ON DELETE CASCADE,
                              UNIQUE (variant_id, size_id)
);

-- ## Orders Table ##
-- Stores customer order information.
CREATE TABLE orders (
                         order_id SERIAL PRIMARY KEY,
                         uuid UUID DEFAULT gen_random_uuid() NOT NULL,
                         user_id INT NOT NULL,
                         total_amount NUMERIC(10, 2) NOT NULL,
                         status VARCHAR(50) DEFAULT 'pending' NOT NULL,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         CONSTRAINT fk_user
                             FOREIGN KEY(user_id)
                                 REFERENCES users(user_id)
                                 ON DELETE CASCADE
);

-- ## Order Item Table ##
-- Stores individual items within an order.
CREATE TABLE order_item (
                            order_item_id SERIAL PRIMARY KEY,
                            uuid UUID DEFAULT gen_random_uuid() NOT NULL,
                            order_id INT NOT NULL,
                            product_id INT,
                            variant_id INT,
                            size_id INT,
                            quantity INT NOT NULL,
                            price NUMERIC(10, 2) NOT NULL,
                            CONSTRAINT fk_order
                                FOREIGN KEY(order_id)
                                    REFERENCES orders(order_id)
                                    ON DELETE CASCADE,
                            CONSTRAINT fk_product
                                FOREIGN KEY(product_id)
                                    REFERENCES product(product_id)
                                    ON DELETE SET NULL,
                            CONSTRAINT fk_variant
                                FOREIGN KEY(variant_id)
                                    REFERENCES product_variant(variant_id)
                                    ON DELETE SET NULL,
                            CONSTRAINT fk_size
                                FOREIGN KEY(size_id)
                                    REFERENCES size(size_id)
                                    ON DELETE SET NULL
);

-- ## Favorites Table ##
-- Stores user's favorite products.
CREATE TABLE favorite (
                          favorite_id SERIAL PRIMARY KEY,
                          uuid UUID DEFAULT gen_random_uuid() NOT NULL,
                          user_id INT NOT NULL,
                          product_id INT NOT NULL,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT fk_user
                              FOREIGN KEY(user_id)
                                  REFERENCES users(user_id)
                                  ON DELETE CASCADE,
                          CONSTRAINT fk_product
                              FOREIGN KEY(product_id)
                                  REFERENCES product(product_id)
                                  ON DELETE CASCADE,
                          UNIQUE (user_id, product_id)
);

-- ## Notifications Table ##
-- Stores notifications for users.
CREATE TABLE IF NOT EXISTS notifications (
                                             id SERIAL PRIMARY KEY,
                                             user_id BIGINT REFERENCES users(user_id) ON DELETE CASCADE,
                                             product_id BIGINT,
                                             title VARCHAR(255),
                                             content TEXT,
                                             icon_url VARCHAR(255),
                                             is_read BOOLEAN DEFAULT FALSE,
                                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                             updated_at TIMESTAMP
);
-- First, create the category table
CREATE TABLE category (
                          category_id SERIAL PRIMARY KEY,
                          uuid UUID DEFAULT gen_random_uuid() NOT NULL,
                          name VARCHAR(255) NOT NULL,
                          parent_id INT, -- This will link to another category_id
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT fk_parent_category
                              FOREIGN KEY(parent_id)
                                  REFERENCES category(category_id)
                                  ON DELETE CASCADE -- If a parent is deleted, its children are also deleted
);

-- ## Product Category Table (Bridge Table) ##
-- Links products to categories (many-to-many).
CREATE TABLE product_category (
                                  product_id INT NOT NULL,
                                  category_id INT NOT NULL,
                                  PRIMARY KEY (product_id, category_id), -- Ensures a product is in a category only once
                                  CONSTRAINT fk_product
                                      FOREIGN KEY(product_id)
                                          REFERENCES product(product_id)
                                          ON DELETE CASCADE,
                                  CONSTRAINT fk_category
                                      FOREIGN KEY(category_id)
                                          REFERENCES category(category_id)
                                          ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS transactions (
                                            id SERIAL8 PRIMARY KEY,
                                            user_id BIGINT NOT NULL,
                                            total_amount DECIMAL(10, 2) NOT NULL,
                                            status VARCHAR(50) NOT NULL, -- e.g., 'Pending', 'Processing', 'Shipped', 'Delivered', 'Cancelled'
                                            shipping_address TEXT,
                                            payment_method VARCHAR(100),
                                            order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                            FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Table to store individual items within a transaction
CREATE TABLE IF NOT EXISTS transaction_items (
                                                 id SERIAL8 PRIMARY KEY,
                                                 transaction_id BIGINT NOT NULL,
                                                 product_id BIGINT NOT NULL,
                                                 quantity INT NOT NULL,
                                                 price_at_purchase DECIMAL(10, 2) NOT NULL,
                                                 FOREIGN KEY (transaction_id) REFERENCES transactions(id),
                                                 FOREIGN KEY (product_id) REFERENCES product(product_id)
);
drop table notifications cascade  ;

-- This script assumes the category table is empty and the IDs will be generated sequentially starting from 1.
-- This script clears and populates the category table based on the menu structure in the provided images.

-- 1. Clear all existing data from the category table to start fresh.
TRUNCATE TABLE category RESTART IDENTITY CASCADE;

-- 2. Insert the four main, top-level categories. Their parent_id is NULL.
INSERT INTO category (name, parent_id) VALUES
                                           ('Women', NULL),      -- ID will be 1
                                           ('Men', NULL),        -- ID will be 2
                                           ('Boys', NULL),       -- ID will be 3
                                           ('Girls', NULL);      -- ID will be 4

--------------------------------------------------------------------------------
-- 3. INSERT CATEGORIES FOR 'WOMEN' (Parent ID: 1)
--------------------------------------------------------------------------------
-- Level 2 sub-categories for 'Women'
INSERT INTO category (name, parent_id) VALUES
                                           ('New In', 1),            -- ID: 5
                                           ('Clothing', 1),          -- ID: 6
                                           ('Shoes', 1),             -- ID: 7
                                           ('Accessories', 1),       -- ID: 8
                                           ('Shop by collection', 1),-- ID: 9
                                           ('SALE', 1);              -- ID: 10

-- Level 3 sub-categories for 'Women' -> 'New In' (Parent ID: 5)
INSERT INTO category (name, parent_id) VALUES
                                           ('All', 5), ('Casual', 5), ('Sportlife', 5), ('New In Top', 5), ('New In Dress', 5), ('New In Bottom', 5);

-- Level 3 sub-categories for 'Women' -> 'Clothing' (Parent ID: 6)
INSERT INTO category (name, parent_id) VALUES
                                           ('All', 6), ('Tops', 6), ('Blazers', 6), ('Vest', 6), ('Bras', 6), ('Shirts', 6), ('T-Shirts', 6), ('Jackets', 6), ('Polo Shirts', 6), ('Hoodies & Sweatshirts', 6), ('Jumpsuits', 6), ('Dresses', 6), ('Cardigans', 6), ('Blouses', 6), ('Sportswear', 6), ('Trousers', 6), ('Jeans', 6), ('Skirts', 6), ('Shorts', 6);

-- Level 3 sub-categories for 'Women' -> 'Shoes' (Parent ID: 7)
INSERT INTO category (name, parent_id) VALUES
                                           ('All', 7), ('Sneakers', 7), ('Sandals', 7), ('Loafers', 7);

-- Level 3 sub-categories for 'Women' -> 'Accessories' (Parent ID: 8)
INSERT INTO category (name, parent_id) VALUES
                                           ('All', 8), ('Backpacks', 8), ('Bags', 8), ('Belts', 8), ('Caps & Hats', 8), ('Socks', 8), ('Gloves', 8), ('Bows', 8), ('UnderWear', 8), ('Sport Equipment', 8);

-- Level 3 sub-categories for 'Women' -> 'Shop by collection' (Parent ID: 9)
INSERT INTO category (name, parent_id) VALUES
                                           ('All', 9), ('Coffee Lover Series 5', 9), ('Women Denim', 9);

-- Level 3 sub-categories for 'Women' -> 'SALE' (Parent ID: 10)
INSERT INTO category (name, parent_id) VALUES
                                           ('Clothing', 10), ('Shoes', 10), ('Accessories', 10), ('Shop by collection', 10);

--------------------------------------------------------------------------------
-- 4. INSERT CATEGORIES FOR 'MEN' (Parent ID: 2)
--------------------------------------------------------------------------------
-- Level 2 sub-categories for 'Men'
INSERT INTO category (name, parent_id) VALUES
                                           ('New In', 2), ('Clothing', 2), ('Shoes', 2), ('Accessories', 2), ('Shop by collection', 2), ('SALE', 2);

-- Level 3 sub-categories for 'Men' -> 'New In'
INSERT INTO category (name, parent_id) VALUES
                                           ('All', (SELECT category_id FROM category WHERE name = 'New In' AND parent_id = 2)),
                                           ('Lifestyle', (SELECT category_id FROM category WHERE name = 'New In' AND parent_id = 2)),
                                           ('Casual', (SELECT category_id FROM category WHERE name = 'New In' AND parent_id = 2)),
                                           ('New In Top', (SELECT category_id FROM category WHERE name = 'New In' AND parent_id = 2)),
                                           ('New In Bottom', (SELECT category_id FROM category WHERE name = 'New In' AND parent_id = 2));

-- Level 3 sub-categories for 'Men' -> 'Clothing'
INSERT INTO category (name, parent_id) VALUES
                                           ('All', (SELECT category_id FROM category WHERE name = 'Clothing' AND parent_id = 2)),
                                           ('Shirts', (SELECT category_id FROM category WHERE name = 'Clothing' AND parent_id = 2)),
                                           ('Blazers', (SELECT category_id FROM category WHERE name = 'Clothing' AND parent_id = 2)),
                                           ('Vest', (SELECT category_id FROM category WHERE name = 'Clothing' AND parent_id = 2)),
                                           ('Polo Shirts', (SELECT category_id FROM category WHERE name = 'Clothing' AND parent_id = 2)),
                                           ('T-Shirts', (SELECT category_id FROM category WHERE name = 'Clothing' AND parent_id = 2)),
                                           ('Jackets', (SELECT category_id FROM category WHERE name = 'Clothing' AND parent_id = 2)),
                                           ('Hoodies & Sweatshirts', (SELECT category_id FROM category WHERE name = 'Clothing' AND parent_id = 2)),
                                           ('Cardigans', (SELECT category_id FROM category WHERE name = 'Clothing' AND parent_id = 2)),
                                           ('Sportswear', (SELECT category_id FROM category WHERE name = 'Clothing' AND parent_id = 2)),
                                           ('Trousers', (SELECT category_id FROM category WHERE name = 'Clothing' AND parent_id = 2)),
                                           ('Jeans', (SELECT category_id FROM category WHERE name = 'Clothing' AND parent_id = 2)),
                                           ('Shorts', (SELECT category_id FROM category WHERE name = 'Clothing' AND parent_id = 2)),
                                           ('Boxers', (SELECT category_id FROM category WHERE name = 'Clothing' AND parent_id = 2));

-- Level 3 sub-categories for 'Men' -> 'Shoes'
INSERT INTO category (name, parent_id) VALUES
                                           ('All', (SELECT category_id FROM category WHERE name = 'Shoes' AND parent_id = 2)),
                                           ('Sandals', (SELECT category_id FROM category WHERE name = 'Shoes' AND parent_id = 2)),
                                           ('Sneakers', (SELECT category_id FROM category WHERE name = 'Shoes' AND parent_id = 2));

-- Level 3 sub-categories for 'Men' -> 'Accessories'
INSERT INTO category (name, parent_id) VALUES
                                           ('All', (SELECT category_id FROM category WHERE name = 'Accessories' AND parent_id = 2)),
                                           ('Caps & Hats', (SELECT category_id FROM category WHERE name = 'Accessories' AND parent_id = 2)),
                                           ('Bags', (SELECT category_id FROM category WHERE name = 'Accessories' AND parent_id = 2)),
                                           ('Socks', (SELECT category_id FROM category WHERE name = 'Accessories' AND parent_id = 2)),
                                           ('Belts', (SELECT category_id FROM category WHERE name = 'Accessories' AND parent_id = 2)),
                                           ('Gloves', (SELECT category_id FROM category WHERE name = 'Accessories' AND parent_id = 2)),
                                           ('Wallet', (SELECT category_id FROM category WHERE name = 'Accessories' AND parent_id = 2)),
                                           ('Backpacks', (SELECT category_id FROM category WHERE name = 'Accessories' AND parent_id = 2));

-- Level 3 sub-categories for 'Men' -> 'Shop by collection'
INSERT INTO category (name, parent_id) VALUES
                                           ('All', (SELECT category_id FROM category WHERE name = 'Shop by collection' AND parent_id = 2)),
                                           ('Coffee Lover Series 5', (SELECT category_id FROM category WHERE name = 'Shop by collection' AND parent_id = 2)),
                                           ('Men Denim', (SELECT category_id FROM category WHERE name = 'Shop by collection' AND parent_id = 2));

-- Level 3 sub-categories for 'Men' -> 'SALE'
INSERT INTO category (name, parent_id) VALUES
                                           ('Clothing', (SELECT category_id FROM category WHERE name = 'SALE' AND parent_id = 2)),
                                           ('Shoes', (SELECT category_id FROM category WHERE name = 'SALE' AND parent_id = 2)),
                                           ('Accessories', (SELECT category_id FROM category WHERE name = 'SALE' AND parent_id = 2)),
                                           ('Shop by collection', (SELECT category_id FROM category WHERE name = 'SALE' AND parent_id = 2));

--------------------------------------------------------------------------------
-- 5. INSERT CATEGORIES FOR 'BOYS' (Parent ID: 3)
--------------------------------------------------------------------------------
-- Level 2 sub-categories for 'Boys'
INSERT INTO category (name, parent_id) VALUES
                                           ('New In', 3), ('Clothing', 3), ('Shoes', 3), ('SALE', 3);

-- Level 3 sub-categories for 'Boys' -> 'Clothing'
INSERT INTO category (name, parent_id) VALUES
                                           ('All', (SELECT category_id FROM category WHERE name = 'Clothing' AND parent_id = 3)),
                                           ('Shirts', (SELECT category_id FROM category WHERE name = 'Clothing' AND parent_id = 3)),
                                           ('T-Shirts', (SELECT category_id FROM category WHERE name = 'Clothing' AND parent_id = 3)),
                                           ('Jackets', (SELECT category_id FROM category WHERE name = 'Clothing' AND parent_id = 3)),
                                           ('Clothing Sets', (SELECT category_id FROM category WHERE name = 'Clothing' AND parent_id = 3)),
                                           ('Trousers', (SELECT category_id FROM category WHERE name = 'Clothing' AND parent_id = 3)),
                                           ('Shorts', (SELECT category_id FROM category WHERE name = 'Clothing' AND parent_id = 3));

-- Level 3 sub-categories for 'Boys' -> 'Shoes'
INSERT INTO category (name, parent_id) VALUES
                                           ('All', (SELECT category_id FROM category WHERE name = 'Shoes' AND parent_id = 3)),
                                           ('Sneakers', (SELECT category_id FROM category WHERE name = 'Shoes' AND parent_id = 3)),
                                           ('Sandals', (SELECT category_id FROM category WHERE name = 'Shoes' AND parent_id = 3));

-- Level 3 sub-categories for 'Boys' -> 'SALE'
INSERT INTO category (name, parent_id) VALUES
                                           ('New In', (SELECT category_id FROM category WHERE name = 'SALE' AND parent_id = 3)),
                                           ('Clothing', (SELECT category_id FROM category WHERE name = 'SALE' AND parent_id = 3)),
                                           ('Shoes', (SELECT category_id FROM category WHERE name = 'SALE' AND parent_id = 3));

--------------------------------------------------------------------------------
-- 6. INSERT CATEGORIES FOR 'GIRLS' (Parent ID: 4)
--------------------------------------------------------------------------------
-- Level 2 sub-categories for 'Girls'
INSERT INTO category (name, parent_id) VALUES
                                           ('New In', 4), ('Clothing', 4), ('Accessories', 4), ('Shoes', 4), ('SALE', 4);

-- Level 3 sub-categories for 'Girls' -> 'Clothing'
INSERT INTO category (name, parent_id) VALUES
                                           ('All', (SELECT category_id FROM category WHERE name = 'Clothing' AND parent_id = 4)),
                                           ('Jeans', (SELECT category_id FROM category WHERE name = 'Clothing' AND parent_id = 4)),
                                           ('Cardigan', (SELECT category_id FROM category WHERE name = 'Clothing' AND parent_id = 4)),
                                           ('Shirts', (SELECT category_id FROM category WHERE name = 'Clothing' AND parent_id = 4)),
                                           ('T-Shirts', (SELECT category_id FROM category WHERE name = 'Clothing' AND parent_id = 4)),
                                           ('Dresses', (SELECT category_id FROM category WHERE name = 'Clothing' AND parent_id = 4)),
                                           ('Trousers', (SELECT category_id FROM category WHERE name = 'Clothing' AND parent_id = 4));

-- Level 3 sub-categories for 'Girls' -> 'Shoes'
INSERT INTO category (name, parent_id) VALUES
                                           ('All', (SELECT category_id FROM category WHERE name = 'Shoes' AND parent_id = 4)),
                                           ('Sandals', (SELECT category_id FROM category WHERE name = 'Shoes' AND parent_id = 4)),
                                           ('Sneakers', (SELECT category_id FROM category WHERE name = 'Shoes' AND parent_id = 4));

-- Level 3 sub-categories for 'Girls' -> 'SALE'
INSERT INTO category (name, parent_id) VALUES
                                           ('New In', (SELECT category_id FROM category WHERE name = 'SALE' AND parent_id = 4)),
                                           ('Clothing', (SELECT category_id FROM category WHERE name = 'SALE' AND parent_id = 4)),
                                           ('Accessories', (SELECT category_id FROM category WHERE name = 'SALE' AND parent_id = 4)),
                                           ('Shoes', (SELECT category_id FROM category WHERE name = 'SALE' AND parent_id = 4));





delete from category where category_id = 18;
truncate table  restart identity cascade ;
DROP TABLE IF EXISTS order_item CASCADE;