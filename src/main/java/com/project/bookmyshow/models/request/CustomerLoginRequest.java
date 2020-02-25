package com.project.bookmyshow.models.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerLoginRequest {
    private String email;
    private String password;
}
