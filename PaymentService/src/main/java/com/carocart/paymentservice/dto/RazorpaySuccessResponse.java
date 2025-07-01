package com.carocart.paymentservice.dto;


public class RazorpaySuccessResponse {
    private String razorpayOrderId;
    private Integer amount;   // Amount in paise (int)
    private String currency;  // "INR"
    private String key;       // Razorpay public key for frontend

    public RazorpaySuccessResponse() {}

    public RazorpaySuccessResponse(String razorpayOrderId, Integer amount, String currency, String key) {
        this.razorpayOrderId = razorpayOrderId;
        this.amount = amount;
        this.currency = currency;
        this.key = key;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public void setRazorpayOrderId(String razorpayOrderId) {
        this.razorpayOrderId = razorpayOrderId;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return "RazorpaySuccessResponse{" +
                "razorpayOrderId='" + razorpayOrderId + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", key='" + key + '\'' +
                '}';
    }
}
