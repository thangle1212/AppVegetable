package com.example.appvegetables.home;
import java.io.Serializable;
public class Product implements Serializable {
    private final String name;
    private final String price;
    private final int imageResId;

    // Constructor
    public Product(String name, String price, int imageResId) {
        this.name = name;
        this.price = price;
        this.imageResId = imageResId;
    }
    // Getter cho name
    public String getName() {
        return name;
    }

    // Getter cho price
    public String getPrice() {
        return price;
    }

    // Getter cho imageResId
    public int getImageResId() {
        return imageResId;
    }
}
