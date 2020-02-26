package com.project.bookmyshow.controller;

import com.project.bookmyshow.constants.ErrorMessages;
import com.project.bookmyshow.exceptions.CustomerException;
import com.project.bookmyshow.models.APIResponse;
import com.project.bookmyshow.models.response.CustomerDetail;
import com.project.bookmyshow.models.request.AddCustomerRequest;
import com.project.bookmyshow.models.request.CustomerLoginRequest;
import com.project.bookmyshow.models.response.ErrorResponse;
import com.project.bookmyshow.services.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    /**
     * This API is used for user to login into the system for
     * booking tickets
     * @param request
     * @return
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<APIResponse> getLoginCustomerDetails(@RequestBody CustomerLoginRequest request) {
        log.info("Customer Login - Email : {}, Password : {}", request.getEmail(), request.getPassword());
        CustomerDetail customerDetail = customerService.getCustomerDetails(request.getEmail(), request.getPassword());
        ResponseEntity<APIResponse> responseEntity = new ResponseEntity<>(
                new ErrorResponse(ErrorMessages.USER_NOT_REGISTERED),
                HttpStatus.OK
        );
        if (customerDetail != null) {
            responseEntity = new ResponseEntity<>(customerDetail, HttpStatus.OK);
        }
        return responseEntity;
    }

    /**
     * This API is used for user to register into the system for
     * booking tickets
     * @param addCustomerRequest
     * @return
     */
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<APIResponse> registerCustomer(@RequestBody AddCustomerRequest addCustomerRequest) {
        log.info("Add Customer : {}", ToStringBuilder.reflectionToString(addCustomerRequest));
        ResponseEntity<APIResponse> response;
        try {
            response = new ResponseEntity<>(customerService.addCustomerToDB(addCustomerRequest), HttpStatus.CREATED);
        } catch (CustomerException e) {
            response = new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.OK);
        }
        return response;
    }

    /**
     * This API is used for the logged in user to access their
     * booking history
     * @param customerId
     * @return
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/{customerId}/history", method = RequestMethod.GET)
    public ResponseEntity getBookingListForCustomer(@PathVariable(name = "customerId") int customerId) {
        ResponseEntity responseEntity;
        if (customerService.isCustomerRegistered(customerId)) {
            responseEntity = new ResponseEntity(customerService.getBookingDetailsList(customerId), HttpStatus.OK);
        } else {
            responseEntity = new ResponseEntity<>(
                    new ErrorResponse(ErrorMessages.USER_NOT_REGISTERED),
                    HttpStatus.OK
            );
        }
        return responseEntity;
    }
}
