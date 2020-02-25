package com.project.bookmyshow.services;

import com.project.bookmyshow.constants.ErrorMessages;
import com.project.bookmyshow.db.dao.CustomerDAO;
import com.project.bookmyshow.db.dao.ShowBookingDAO;
import com.project.bookmyshow.db.mappers.Customer;
import com.project.bookmyshow.db.mappers.ShowBooking;
import com.project.bookmyshow.exceptions.CustomerException;
import com.project.bookmyshow.models.response.BookingDetails;
import com.project.bookmyshow.models.response.CustomerDetail;
import com.project.bookmyshow.models.request.AddCustomerRequest;
import com.project.bookmyshow.models.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CustomerService {

    @Autowired
    private BookingService bookingService;
    @Autowired
    CustomerDAO customerDAO;
    /**
     * This function adds/registers the user and returns
     * the new customer details
     * @param addCustomerRequest
     * @return
     * @throws CustomerException
     */
    public ErrorResponse addCustomerToDB(AddCustomerRequest addCustomerRequest) throws CustomerException {
        if (!isCustomerRegistered(addCustomerRequest.getEmail(), addCustomerRequest.getPassword())) {
            Customer customer = customerDAO.insert(
                    addCustomerRequest.getName(),
                    addCustomerRequest.getEmail(),
                    addCustomerRequest.getPassword()
            );
            log.debug("Customer add : {}", ToStringBuilder.reflectionToString(customer));
        } else {
            throw new CustomerException(ErrorMessages.USER_ALREADY_REGISTERED);
        }

        return new ErrorResponse(ErrorMessages.USER_REGISTER_SUCCESSFUL);
    }

    /**
     * This function returns boolean for checking
     * if the customer is registered or not
     * @param customerId
     * @return
     */
    public boolean isCustomerRegistered(int customerId) {
        Customer customer = customerDAO.getCustomerById(customerId);
        return (customer != null);
    }

    /**
     * This function returns boolean for checking
     * if the customer is registered or not
     * @param email
     * @param password
     * @return
     */
    public boolean isCustomerRegistered(String email, String password) {
        Customer customer = customerDAO.getCustomerBy(email, password);
        return (customer != null);
    }

    /**
     * This function returns the customer details for login
     * purpose based on the entered email and password
     * @param email
     * @param password
     * @return
     */
    public CustomerDetail getCustomerDetails(String email, String password) {
        Customer customer = customerDAO.getCustomerBy(email, password);
        CustomerDetail customerDetail = null;
        if (customer != null) {
            customerDetail = CustomerDetail.builder()
                    .customerName(customer.getName())
                    .customerEmail(customer.getEmail())
                    .customerId(customer.getCustomerId())
                    .customerRole(customer.getRole())
                    .build();
        }
        return customerDetail;
    }

    /**
     * This function returns the booking history of the customer
     * @param customerId
     * @return
     */
    public List<BookingDetails> getBookingDetailsList(int customerId) {
        ShowBookingDAO showBookingDAO = new ShowBookingDAO();
        List<ShowBooking> showBookings = showBookingDAO.getBookedShowByCustomerId(customerId);
        List<BookingDetails> bookingDetails = new ArrayList<>();
        if (!CollectionUtils.isEmpty(showBookings)) {
            for (ShowBooking showBooking : showBookings) {
                bookingDetails.add(bookingService.getBookingDetails(showBooking));
            }
        }
        return bookingDetails;
    }
}
