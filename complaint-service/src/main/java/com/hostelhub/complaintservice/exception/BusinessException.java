package com.hostelhub.complaintservice.exception;

public class BusinessException
        extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}