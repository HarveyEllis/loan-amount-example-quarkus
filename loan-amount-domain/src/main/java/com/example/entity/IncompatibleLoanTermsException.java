/* (C)2021 */
package com.example.entity;

public class IncompatibleLoanTermsException extends Exception {
    IncompatibleLoanTermsException(String message) {
        super(message);
    }
}
