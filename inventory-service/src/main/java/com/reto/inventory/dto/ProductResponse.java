package com.reto.inventory.dto;

//Usando record evito escribir codigo como getters, equals, hashcode y toString
//Usando record creo clases ligeras e inmutables
//Se dedica exclusivamente a transportar solo los datos que necesito mostrar
public record ProductResponse (String productId, String name, Integer stock) {
}


/*si no usara record el codigo seria asi:
public class ProductResponse {

    private final String productId;
    private final String name;
    private final Integer stock;

    // Constructor
    public ProductResponse(String productId, String name, Integer stock) {
        this.productId = productId;
        this.name = name;
        this.stock = stock;
    }

    // Getters
    public String getProductId() { return productId; }
    public String getName()      { return name; }
    public Integer getStock()    { return stock; }

    // equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductResponse that)) return false;
        return Objects.equals(productId, that.productId)
            && Objects.equals(name, that.name)
            && Objects.equals(stock, that.stock);
    }

    // hashCode
    @Override
    public int hashCode() {
        return Objects.hash(productId, name, stock);
    }

    // toString
    @Override
    public String toString() {
        return "ProductResponse[productId=" + productId
             + ", name=" + name
             + ", stock=" + stock + "]";
    }
}
 */