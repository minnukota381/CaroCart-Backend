package com.carocart.delivery.dto;

public class EarningsSummaryDTO {
    private long deliveredCount;
    private double totalFee;
    private double totalTip;
    private double totalPenalty;
    private double net;

    public EarningsSummaryDTO(long deliveredCount, double totalFee, double totalTip, double totalPenalty, double net) {
        this.deliveredCount = deliveredCount;
        this.totalFee = totalFee;
        this.totalTip = totalTip;
        this.totalPenalty = totalPenalty;
        this.net = net;
    }
    // getters
    public long getDeliveredCount() { return deliveredCount; }
    public double getTotalFee() { return totalFee; }
    public double getTotalTip() { return totalTip; }
    public double getTotalPenalty() { return totalPenalty; }
    public double getNet() { return net; }
}
