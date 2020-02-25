package com.project.bookmyshow.controller;

import com.project.bookmyshow.constants.ErrorMessages;
import com.project.bookmyshow.db.mappers.ShowBooking;
import com.project.bookmyshow.exceptions.BookingException;
import com.project.bookmyshow.models.APIResponse;
import com.project.bookmyshow.models.request.BookingPaymentRequest;
import com.project.bookmyshow.models.request.BookingRequest;
import com.project.bookmyshow.models.response.ErrorResponse;
import com.project.bookmyshow.services.BookingService;
import com.project.bookmyshow.services.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/booking")
public class BookingController {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private CustomerService customerService;

    /**
     * This API takes input as {@link BookingRequest} and initiate
     * booking of the seats for the customer if available otherwise
     * returns an error response(i.e. {@link ErrorResponse}) with
     * the appropriate error message.
     * @param bookingRequest
     * @return
     */
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<APIResponse> initiateBooking(@RequestBody BookingRequest bookingRequest) {
        ResponseEntity<APIResponse> response;
        if (customerService.isCustomerRegistered(bookingRequest.getCustomerId())) {
            try {
                response = new ResponseEntity<>(bookingService.initateBooking(bookingRequest), HttpStatus.CREATED);
            } catch (BookingException e) {
                response = new ResponseEntity<>(new ErrorResponse(e.getErrorMessage()), HttpStatus.OK);
            }
        } else {
            response = new ResponseEntity<>(new ErrorResponse(ErrorMessages.SIGNED_IN_FOR_BOOKING), HttpStatus.OK);
        }
        return response;
    }

    /**
     * This API takes input as {@link BookingPaymentRequest} and
     * initiate the payment for the booking of the seats for the
     * customer and returns with the show details and booking status
     * (i.e. {@link com.project.bookmyshow.models.response.BookingDetails})
     * returns an error response(i.e. {@link ErrorResponse}) with
     * the appropriate error message in case session expires or seat are not
     * available or any unexpected scenario.
     * @param bookingPaymentRequest
     * @return
     */
    @PostMapping(value = "/pay", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<APIResponse> finalizeBooking(@RequestBody BookingPaymentRequest bookingPaymentRequest) {
        ResponseEntity<APIResponse> response;
        if (customerService.isCustomerRegistered(bookingPaymentRequest.getCustomerId())) {
            try {
                response = new ResponseEntity<>(bookingService.finalizeBooking(bookingPaymentRequest), HttpStatus.CREATED);
            } catch (BookingException e) {
                response = new ResponseEntity<>(new ErrorResponse(e.getErrorMessage()), HttpStatus.OK);
            }
        } else {
            response = new ResponseEntity<>(new ErrorResponse(ErrorMessages.SIGNED_IN_FOR_BOOKING), HttpStatus.OK);
        }
        return response;
    }

    /**
     * This API returns the show details and booking status
     * (i.e. {@link com.project.bookmyshow.models.response.BookingDetails})
     * based on the {@link ShowBooking#getBookingRefNo()}. This function is
     * independent of customerId as this API can be used for QrCode generated
     * on booking ticket to show at cinema hall thus giving smooth user experience.
     * @param bookingRefNo
     * @return
     */
    @RequestMapping(value = "/{bookingRefNo}", method = RequestMethod.GET)
    public ResponseEntity<APIResponse> getBookingForCustomer(@PathVariable(name = "bookingRefNo") String bookingRefNo) {
        return new ResponseEntity<>(bookingService.getBookingDetails(bookingRefNo), HttpStatus.OK);
    }
}
