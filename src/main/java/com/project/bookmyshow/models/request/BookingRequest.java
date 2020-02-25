package com.project.bookmyshow.models.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BookingRequest {

    private int customerId;
    private int scheduledLiveShowId;
    private List<Integer> seatIdList;
}
