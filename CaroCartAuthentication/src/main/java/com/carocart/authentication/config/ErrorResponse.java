package com.carocart.authentication.config;

import java.util.Map;

public class ErrorResponse {
    private String message;               // optional general message
    private Map<String, String> errors;   // field -> message

    public ErrorResponse() {}

    public ErrorResponse(String message) {
        this.message = message;
    }

    public ErrorResponse(Map<String, String> errors) {
        this.errors = errors;
    }

    public ErrorResponse(String message, Map<String, String> errors) {
        this.message = message;
        this.errors = errors;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Map<String, String> getErrors() { return errors; }
    public void setErrors(Map<String, String> errors) { this.errors = errors; }
}
