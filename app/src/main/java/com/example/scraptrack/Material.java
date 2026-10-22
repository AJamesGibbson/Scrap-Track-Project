package com.example.scraptrack;

public class Material {
    private String name;
    private double quantity;
    private String unit;
    private double price;

    // No-argument constructor (required for Firebase)
    public Material() {
    }

    // Parameterized constructor
    public Material(String name, double quantity, String unit, double price) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.price = price;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
