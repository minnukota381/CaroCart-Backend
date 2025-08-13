package com.carocart.orderservice.dto;

public class VendorEarningsDTO {
    private Long vendorId;
    private String vendorName;
    private String upiId;
    private double totalSales;
    private double commissionAmount;
    private double vendorPayable;

    public VendorEarningsDTO() {}

    public VendorEarningsDTO(Long vendorId, String vendorName, String upiId,
                              double totalSales, double commissionAmount) {
        this.vendorId = vendorId;
        this.vendorName = vendorName;
        this.upiId = upiId;
        this.totalSales = totalSales;
        this.commissionAmount = commissionAmount;
        this.vendorPayable = totalSales - commissionAmount;
    }

	public Long getVendorId() {
		return vendorId;
	}

	public void setVendorId(Long vendorId) {
		this.vendorId = vendorId;
	}

	public String getVendorName() {
		return vendorName;
	}

	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}

	public String getUpiId() {
		return upiId;
	}

	public void setUpiId(String upiId) {
		this.upiId = upiId;
	}

	public double getTotalSales() {
		return totalSales;
	}

	public void setTotalSales(double totalSales) {
		this.totalSales = totalSales;
	}

	public double getCommissionAmount() {
		return commissionAmount;
	}

	public void setCommissionAmount(double commissionAmount) {
		this.commissionAmount = commissionAmount;
	}

	public double getVendorPayable() {
		return vendorPayable;
	}

	public void setVendorPayable(double vendorPayable) {
		this.vendorPayable = vendorPayable;
	}

    // getters & setters...
    
    
}