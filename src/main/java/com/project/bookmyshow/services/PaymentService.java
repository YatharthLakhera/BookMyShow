package com.project.bookmyshow.services;

public class PaymentService {

    public enum Status {
        SUCCESS, FAILED, INPROGRESS
    }

    /**
     * Mocking payment API
     * @return
     */
    public static Status processPayment() {
        return Status.SUCCESS;
    }
}
