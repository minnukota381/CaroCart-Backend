// ================= Order service =================
// src/main/java/com/carocart/orderservice/dto/EmailRequest.java
package com.carocart.orderservice.dto;

public class EmailRequest {
    private String to;
    private String toEmail; // not required by orders, but keeps JSON shape identical

    private String subject;
    private String body;
    private Boolean html;

    public EmailRequest() {}

    public EmailRequest(String to, String subject, String body) {
        this.to = to;
        this.subject = subject;
        this.body = body;
        this.html = false;
    }

    public EmailRequest(String to, String subject, String body, boolean html) {
        this.to = to;
        this.subject = subject;
        this.body = body;
        this.html = html;
    }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public String getToEmail() { return toEmail; }
    public void setToEmail(String toEmail) { this.toEmail = toEmail; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public boolean isHtml() { return html != null && html; }
    public Boolean getHtml() { return html; }
    public void setHtml(Boolean html) { this.html = html; }
}
