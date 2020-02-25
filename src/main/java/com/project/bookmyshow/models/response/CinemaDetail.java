package com.project.bookmyshow.models.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.project.bookmyshow.models.APIResponse;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CinemaDetail implements APIResponse {

    private int cinemaId;
    private String cinemaName;
    @Singular private List<HallDetail> hallDetails;

    public void addHallDetail(HallDetail hallDetail) {
        if (hallDetails == null) {
            hallDetails = new ArrayList<>();
        }
        hallDetails.add(hallDetail);
    }
}
