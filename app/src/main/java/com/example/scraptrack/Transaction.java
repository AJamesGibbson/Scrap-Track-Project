package com.example.scraptrack;

import java.util.List;

public class Transaction {
    private String customerName;
    private String customerPhoneNumber;
    private String transactionDate;
    private List<Material> materials;
    private double totalPrice;
    private String sessionId; // Field to store session ID

    // Default constructor for Firebase
    public Transaction() {
    }

    // Constructor
    public Transaction(String customerName, String customerPhoneNumber, String transactionDate,
                       List<Material> materials, double totalPrice, String sessionId) {
        this.customerName = customerName;
        this.customerPhoneNumber = customerPhoneNumber;
        this.transactionDate = transactionDate;
        this.materials = materials;
        this.totalPrice = totalPrice;
        this.sessionId = sessionId;
    }

    // Getters and Setters
    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhoneNumber() {
        return customerPhoneNumber;
    }

    public void setCustomerPhoneNumber(String customerPhoneNumber) {
        this.customerPhoneNumber = customerPhoneNumber;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public List<Material> getMaterials() {
        return materials;
    }

    public void setMaterials(List<Material> materials) {
        this.materials = materials;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
