package com.project.bookmyshow.models.response;

import com.project.bookmyshow.models.APIResponse;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class BookingDetails implements APIResponse {
    private Double totalAmount;
    private String bookingRefNo;
    private String bookingStatus;
    private ShowDetail showDetail;
}
