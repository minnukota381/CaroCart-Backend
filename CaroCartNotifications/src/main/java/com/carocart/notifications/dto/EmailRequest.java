// ================= Notifications service =================
// src/main/java/com/carocart/notifications/dto/EmailRequest.java
package com.carocart.notifications.dto;

public class EmailRequest {
    // Preferred field
    private String to;
    // Legacy compatibility (your /send endpoint uses this)
    private String toEmail;

    private String subject;
    private String body;
    private Boolean html; // nullable; treat null as false

    public EmailRequest() {}

    // Legacy 3-arg constructor (plain text)
    public EmailRequest(String to, String subject, String body) {
        this.to = to;
        this.subject = subject;
        this.body = body;
        this.html = false;
    }

    // New 4-arg constructor (HTML toggle)
    public EmailRequest(String to, String subject, String body, boolean html) {
        this.to = to;
        this.subject = subject;
        this.body = body;
        this.html = html;
    }

    // Getters/setters
    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public String getToEmail() { return toEmail; }
    public void setToEmail(String toEmail) { this.toEmail = toEmail; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    // Keep boolean-style getter for controller's req.isHtml()
    public boolean isHtml() { return html != null && html; }
    public Boolean getHtml() { return html; } // optional, if you need it
    public void setHtml(Boolean html) { this.html = html; }
}
