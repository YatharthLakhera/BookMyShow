package com.project.bookmyshow.models.response;

import com.project.bookmyshow.models.APIResponse;
import com.project.bookmyshow.models.SeatDetails;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class ScheduledTimingDetails implements APIResponse {
    private int scheduledTimingId;
    private Date startTime;
    private Date endTime;
    private double ticketAmount;
    List<SeatDetails> seatDetailsList;
}
