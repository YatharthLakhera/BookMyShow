package com.project.bookmyshow.models.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.project.bookmyshow.models.APIResponse;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HallDetail implements APIResponse {
    private int hallId;
    private String hallCode;
    private Integer hallRowCount;
    private Integer hallColCount;
    @Singular private List<ScheduledTimingDetails> scheduledTimings;
}
