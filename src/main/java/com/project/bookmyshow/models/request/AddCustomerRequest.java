package com.project.bookmyshow.models.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddCustomerRequest {
    private String name;
    private String email;
    private String password;
}
