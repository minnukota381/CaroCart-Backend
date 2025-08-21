package com.carocart.cartservice.dto;

public class CartSummaryDTO {
    private double subtotal;
    private double deliveryFee;
    private boolean freeDelivery;
    private double totalPayable;
    // getters/setters
	public double getSubtotal() {
		return subtotal;
	}
	public void setSubtotal(double subtotal) {
		this.subtotal = subtotal;
	}
	public double getDeliveryFee() {
		return deliveryFee;
	}
	public void setDeliveryFee(double deliveryFee) {
		this.deliveryFee = deliveryFee;
	}
	public boolean isFreeDelivery() {
		return freeDelivery;
	}
	public void setFreeDelivery(boolean freeDelivery) {
		this.freeDelivery = freeDelivery;
	}
	public double getTotalPayable() {
		return totalPayable;
	}
	public void setTotalPayable(double totalPayable) {
		this.totalPayable = totalPayable;
	}
    
}
