package com.dropzone.paymentservice.model;

public enum PaymentMode {
    SUCCESS,
    FAILED,
    SLOW,
    TIMEOUT,
    DUPLICATE_CALLBACK,
    SERVICE_UNAVAILABLE
}
