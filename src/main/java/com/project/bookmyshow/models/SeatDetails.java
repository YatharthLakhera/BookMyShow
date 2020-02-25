package com.project.bookmyshow.models;

import com.project.bookmyshow.enums.SeatStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SeatDetails {
    private int seatId;
    private SeatStatus seatStatus;
    private String seatCode;
    private int seatRowLocation;
    private int seatColLocation;
}
