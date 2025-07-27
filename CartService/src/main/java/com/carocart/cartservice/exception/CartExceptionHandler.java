package com.carocart.cartservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class CartExceptionHandler {

    @ExceptionHandler(OneVendorCartException.class)
    public ResponseEntity<?> handleOneVendorCartException(OneVendorCartException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleOtherExceptions(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage()));
    }

    public static class ErrorResponse {
        public String error;
        public ErrorResponse(String msg) { this.error = msg; }
        public String getError() { return error; }
    }
}
