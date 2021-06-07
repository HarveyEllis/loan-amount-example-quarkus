package com.example.entity;

public class IncompatibleLoanTermsException extends Exception {
    IncompatibleLoanTermsException(String message) {
        super(message);
    }
}
