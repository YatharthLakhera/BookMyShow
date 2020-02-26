package com.test.bookmyshow;

import com.project.bookmyshow.constants.ErrorMessages;
import com.project.bookmyshow.db.dao.CustomerDAO;
import com.project.bookmyshow.db.mappers.Customer;
import com.project.bookmyshow.enums.CustomerRole;
import com.project.bookmyshow.exceptions.CustomerException;
import com.project.bookmyshow.models.request.AddCustomerRequest;
import com.project.bookmyshow.models.response.CustomerDetail;
import com.project.bookmyshow.models.response.ErrorResponse;
import com.project.bookmyshow.services.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Date;

import static org.mockito.Mockito.when;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class BookMyShowTestApplication {

    @Mock
    private CustomerDAO customerDAO;
    @InjectMocks
    private CustomerService customerService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getCustomerByIdTest() {
        int customerId = 1;
        String email = "testuser1@gmail.com", pass = "TestPass1";
        when(customerDAO.getCustomerBy(email, pass)).thenReturn(getCustomerByID(customerId, email, pass));
        CustomerDetail customerDetail = customerService.getCustomerDetails(email, pass);
        Assert.assertEquals(email, customerDetail.getCustomerEmail());
        Assert.assertEquals(customerId, customerDetail.getCustomerId());
    }

    @Test
    public void addCustomerTest() {
        int customerId = 1;
        String email = "testuser1@gmail.com", pass = "TestPass1", name = "Test User " + customerId;
        when(customerDAO.insert(name, email, pass)).thenReturn(getCustomerByID(customerId, email, pass));
        try {
            ErrorResponse errorResponse = customerService.addCustomerToDB(
                    AddCustomerRequest.builder()
                            .email(email)
                            .name(name)
                            .password(pass)
                            .build()
            );
            Assert.assertEquals(ErrorMessages.USER_REGISTER_SUCCESSFUL, errorResponse.getErrorMessage());
        } catch (CustomerException e) {
            Assert.assertEquals(ErrorMessages.USER_ALREADY_REGISTERED, e.getMessage());
        }
    }



    private Customer getCustomerByID(int customerId, String email, String pass) {
        Date curdate = new Date();
        Customer customer = new Customer();
        customer.setCustomerId(customerId);
        customer.setName("Test User " + customerId);
        customer.setEmail(email);
        customer.setPassword(pass);
        customer.setRole(CustomerRole.USER);
        customer.setCreatedAt(curdate);
        customer.setModifiedAt(curdate);
        return customer;
    }
}
