--TABLA PRINCIPAL DE PEDIDOS
CREATE TABLE IF NOT EXISTS orders (

    --MySQL genera automáticamente 1, 2, 3, 4...
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    --Estado actual del pedido: PENDING, CONFIRMED o CANCELLED.
    status VARCHAR(30) NOT NULL,

    --Fecha en la que se creó el pedido.
    created_at DATETIME NOT NULL,

    --Fecha de la última modificación.
    updated_at DATETIME NOT NULL
    );



--PRODUCTOS QUE PERTENECEN A CADA PEDIDO
CREATE TABLE IF NOT EXISTS order_items (

    --MySQL genera automáticamente el ID del item.
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    --Identifica a qué pedido pertenece este producto.
    order_id BIGINT NOT NULL,

    --ID del producto manejado por Inventory Service.
    product_id VARCHAR(50) NOT NULL,

    --Cantidad solicitada de ese producto.
    quantity INT NOT NULL,

    --Relación interna dentro de la base de datos de Order Service.
    CONSTRAINT fk_order_items_order
    FOREIGN KEY (order_id)
    REFERENCES orders(id)
    );



--HISTORIAL DE CAMBIOS DE ESTADO
CREATE TABLE IF NOT EXISTS order_status_history (
    --Identificador único del registro histórico.
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    --Pedido al que pertenece este cambio.
    order_id BIGINT NOT NULL,

    --Puede ser NULL cuando es el primer estado del pedido.
    previous_status VARCHAR(30),

    --Nuevo estado que tomó el pedido.
    new_status VARCHAR(30) NOT NULL,

    --Momento exacto del cambio.
    created_at DATETIME NOT NULL,

    --Cada historial pertenece a un pedido existente.
    CONSTRAINT fk_order_history_order
    FOREIGN KEY (order_id)
    REFERENCES orders(id)
    );