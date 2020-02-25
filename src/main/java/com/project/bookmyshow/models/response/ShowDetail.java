package com.project.bookmyshow.models.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.project.bookmyshow.enums.ShowType;
import com.project.bookmyshow.models.APIResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShowDetail implements APIResponse {
    private int showId;
    private ShowType showType;
    private String showName;
    private List<CinemaDetail> cinemaDetails;
}