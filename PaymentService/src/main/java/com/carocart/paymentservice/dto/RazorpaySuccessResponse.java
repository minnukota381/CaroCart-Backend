package com.carocart.paymentservice.dto;

public class RazorpaySuccessResponse {
    private String razorpayOrderId;
    private int amount;
    private String currency;
    private String razorpayKey;
    // Constructors, Getters, Setters



    public RazorpaySuccessResponse(String razorpayOrderId, int amount, String currency, String razorpayKey) {
        this.razorpayOrderId = razorpayOrderId;
        this.amount = amount;
        this.currency = currency;
        this.razorpayKey = razorpayKey;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public void setRazorpayOrderId(String razorpayOrderId) {
        this.razorpayOrderId = razorpayOrderId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getRazorpayKey() {
        return razorpayKey;
    }

    public void setRazorpayKey(String razorpayKey) {
        this.razorpayKey = razorpayKey;
    }
}
