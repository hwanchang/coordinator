CREATE TABLE "brand" (
    "id" BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    "name" VARCHAR(32) NOT NULL,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NULL,
    CONSTRAINT "uk_brand_name" UNIQUE ("name")
);

CREATE TABLE "product" (
    "id" BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    "brand_id" BIGINT NOT NULL,
    "name" VARCHAR(32) NOT NULL,
    "category" VARCHAR(32) NOT NULL,
    "price" DECIMAL(12,0) NOT NULL,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NULL
);

CREATE INDEX "idx_product_brand_id" ON "product" ("brand_id");
CREATE INDEX "idx_product_category_price" ON "product" ("category", "price");
