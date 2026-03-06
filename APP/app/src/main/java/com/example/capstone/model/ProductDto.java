// ProductDto.java
package com.example.capstone.model;

public class ProductDto {
    private Long id;
    private String productNumber;
    private String name;
    private String registeredAt;

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProductNumber() { return productNumber; }
    public void setProductNumber(String productNumber) { this.productNumber = productNumber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(String registeredAt) { this.registeredAt = registeredAt; }
}
