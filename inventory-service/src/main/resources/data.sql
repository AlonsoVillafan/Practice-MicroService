-- aqui inserto datos
-- utilizo IGNORE para no volver a insertar
-- y haya problemas por duplicidad

INSERT IGNORE INTO products (product_id, name, stock)
VALUES ('P001', 'Laptop', 10);

INSERT IGNORE INTO products (product_id, name, stock)
VALUES ('P002', 'Mouse', 25);

INSERT IGNORE INTO products (product_id, name, stock)
VALUES ('P003', 'Monitor', 0);