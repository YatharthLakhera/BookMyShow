package com.project.bookmyshow.models.response;

import com.project.bookmyshow.enums.CustomerRole;
import com.project.bookmyshow.models.APIResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerDetail implements APIResponse {
    private int customerId;
    private String customerName;
    private String customerEmail;
    private CustomerRole customerRole;
}
