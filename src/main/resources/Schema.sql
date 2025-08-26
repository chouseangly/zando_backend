-- This script is corrected to use the built-in gen_random_uuid() function.
-- There is no need to create any extension.

-- ## Users Table ##
-- Stores user account information.
CREATE TABLE "user" (
                        user_id SERIAL PRIMARY KEY,
                        uuid UUID DEFAULT gen_random_uuid() NOT NULL,
                        user_name VARCHAR(255) UNIQUE NOT NULL,
                        first_name VARCHAR(255),
                        last_name VARCHAR(255),
                        email VARCHAR(255) UNIQUE NOT NULL,
                        password VARCHAR(255) NOT NULL,
                        role VARCHAR(50) DEFAULT 'user' NOT NULL,
                        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
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
                         created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
drop table product cascade ;

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
CREATE TABLE "order" (
                         order_id SERIAL PRIMARY KEY,
                         uuid UUID DEFAULT gen_random_uuid() NOT NULL,
                         user_id INT NOT NULL,
                         total_amount NUMERIC(10, 2) NOT NULL,
                         status VARCHAR(50) DEFAULT 'pending' NOT NULL,
                         created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                         CONSTRAINT fk_user
                             FOREIGN KEY(user_id)
                                 REFERENCES "user"(user_id)
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
                                    REFERENCES "order"(order_id)
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
                          created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT fk_user
                              FOREIGN KEY(user_id)
                                  REFERENCES "user"(user_id)
                                  ON DELETE CASCADE,
                          CONSTRAINT fk_product
                              FOREIGN KEY(product_id)
                                  REFERENCES product(product_id)
                                  ON DELETE CASCADE,
                          UNIQUE (user_id, product_id)
);

-- ## Notifications Table ##
-- Stores notifications for users.
CREATE TABLE notification (
                              notification_id SERIAL PRIMARY KEY,
                              uuid UUID DEFAULT gen_random_uuid() NOT NULL,
                              user_id INT NOT NULL,
                              message TEXT NOT NULL,
                              is_read BOOLEAN DEFAULT FALSE,
                              created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                              CONSTRAINT fk_user
                                  FOREIGN KEY(user_id)
                                      REFERENCES "user"(user_id)
                                      ON DELETE CASCADE
);
-- First, create the category table
CREATE TABLE category (
                          category_id SERIAL PRIMARY KEY,
                          uuid UUID DEFAULT gen_random_uuid() NOT NULL,
                          name VARCHAR(255) NOT NULL,
                          parent_id INT, -- This will link to another category_id
                          created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT fk_parent_category
                              FOREIGN KEY(parent_id)
                                  REFERENCES category(category_id)
                                  ON DELETE CASCADE -- If a parent is deleted, its children are also deleted
);


-- This script assumes the category table is empty and the IDs will be generated sequentially starting from 1.

-- 1. Insert Main Categories
INSERT INTO category (name, parent_id) VALUES
                                           ('Women', NULL),      -- Assumes ID will be 1
                                           ('Men', NULL),        -- Assumes ID will be 2
                                           ('Boys', NULL),       -- Assumes ID will be 3
                                           ('Girls', NULL);      -- Assumes ID will be 4

-- 2. Insert Level 2 Categories (Sub-categories of 'Women', whose ID is 1)
INSERT INTO category (name, parent_id) VALUES
                                           ('New In', 1),        -- Assumes ID will be 5
                                           ('Clothing', 1),      -- Assumes ID will be 6
                                           ('Shoes', 1),         -- Assumes ID will be 7
                                           ('Accessories', 1),   -- Assumes ID will be 8
                                           ('Shop by collection', 1), -- Assumes ID will be 9
                                           ('SALE', 1);          -- Assumes ID will be 10

-- 3. Insert Level 3 Categories (Sub-sub-categories)

-- Sub-categories of 'New In' (Parent ID: 5)
INSERT INTO category (name, parent_id) VALUES
                                           ('All', 5),
                                           ('Lifestyle', 5),
                                           ('Casual', 5),
                                           ('Sportlife', 5),
                                           ('New In Top', 5),
                                           ('New In Dress', 5),
                                           ('New In Bottom', 5);

-- Sub-categories of 'Clothing' (Parent ID: 6)
INSERT INTO category (name, parent_id) VALUES
                                           ('All', 6),
                                           ('Tops', 6),
                                           ('Blazers', 6),
                                           ('Vest', 6),
                                           ('Bras', 6),
                                           ('Shirts', 6),
                                           ('T-Shirts', 6),
                                           ('Jackets', 6),
                                           ('Polo Shirts', 6),
                                           ('Hoodies & Sweatshirts', 6),
                                           ('Jumpsuits', 6),
                                           ('Dresses', 6),
                                           ('Cardigans', 6),
                                           ('Blouses', 6),
                                           ('Sportswear', 6),
                                           ('Trousers', 6),
                                           ('Jeans', 6),
                                           ('Skirts', 6),
                                           ('Shorts', 6);

-- Sub-categories of 'Shoes' (Parent ID: 7)
INSERT INTO category (name, parent_id) VALUES
                                           ('All', 7),
                                           ('Loafers', 7),
                                           ('Sandals', 7),
                                           ('Sneakers', 7);

-- Sub-categories of 'Accessories' (Parent ID: 8)
INSERT INTO category (name, parent_id) VALUES
                                           ('All', 8),
                                           ('Bows', 8),
                                           ('Bags', 8),
                                           ('Socks', 8),
                                           ('Caps & Hats', 8),
                                           ('Backpacks', 8),
                                           ('Belts', 8),
                                           ('UnderWear', 8),
                                           ('Gloves', 8),
                                           ('Sport Equipment', 8);

-- Sub-categories of 'Shop by collection' (Parent ID: 9)
INSERT INTO category (name, parent_id) VALUES
                                           ('All', 9),
                                           ('Coffee Lover Series 5', 9),
                                           ('Women Denim', 9);

-- Sub-categories of 'SALE' (Parent ID: 10)
INSERT INTO category (name, parent_id) VALUES
                                           ('Clothing', 10),
                                           ('Shoes', 10),
                                           ('Accessories', 10),
                                           ('Shop by collection', 10);
truncate table size , product,product_variant,product_image restart identity cascade ;
DROP TABLE IF EXISTS product_category, category, favorite, order_item, "order", variant_size, product_image, product_variant, size, product, "user" CASCADE;