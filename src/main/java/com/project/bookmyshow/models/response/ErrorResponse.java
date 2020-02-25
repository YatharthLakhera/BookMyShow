package com.project.bookmyshow.models.response;

import com.project.bookmyshow.models.APIResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse implements APIResponse {
    private String errorMessage;

    public ErrorResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
