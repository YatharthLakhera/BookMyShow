package com.project.bookmyshow.exceptions;

public class BookingException extends Exception {

    private String errorMessage;

    public BookingException(String errorMessage) {
        super(errorMessage);
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
