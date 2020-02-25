package com.project.bookmyshow.models.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookingPaymentRequest {
    private int customerId;
    private String bookingRefNo;
    private double ticketAmount;
}
