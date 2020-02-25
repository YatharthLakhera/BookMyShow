package com.project.bookmyshow.db.dao;

import com.project.bookmyshow.db.ConnectionFactory;
import com.project.bookmyshow.db.mappers.Customer;
import com.project.bookmyshow.db.mappers.CustomerDynamicSqlSupport;
import com.project.bookmyshow.db.mappers.CustomerMapper;
import com.project.bookmyshow.enums.CustomerRole;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.dynamic.sql.SqlBuilder;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Repository
public class CustomerDAO {

    public Customer getCustomerById(int customerId) {
        @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
        CustomerMapper customerMapper = sqlSession.getMapper(CustomerMapper.class);
        return customerMapper.selectByPrimaryKey(customerId);
    }

    public Customer getCustomerBy(String email, String password) {
        @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
        return getCustomerBy(email, password, sqlSession);
    }

    public Customer getCustomerBy(String email, String password, SqlSession sqlSession) {
        CustomerMapper customerMapper = sqlSession.getMapper(CustomerMapper.class);
        return getCustomerBy(email, password, customerMapper);
    }

    private Customer getCustomerBy(String email, String password, CustomerMapper customerMapper) {
        CustomerDynamicSqlSupport.Customer customerSqlSupport = new CustomerDynamicSqlSupport.Customer();
        List<Customer> customerList = customerMapper.selectByExample()
                .where(customerSqlSupport.email, SqlBuilder.isEqualTo(email))
                .and(customerSqlSupport.password, SqlBuilder.isEqualTo(password))
                .limit(1).build().execute();
        Customer customer = null;
        if (!CollectionUtils.isEmpty(customerList)) {
            customer = customerList.get(0);
        }
        return customer;
    }

    public Customer insert(String name, String email, String password) {
        @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
        CustomerMapper customerMapper = sqlSession.getMapper(CustomerMapper.class);
        Customer customer = getCustomerBy(email, password, customerMapper);
        if (customer == null) {
            customer = new Customer();
            customer.setEmail(email);
            customer.setPassword(password);
            customer.setName(name);
            customer.setRole(CustomerRole.USER);
            int entries = customerMapper.insertSelective(customer);
            log.info("Customer add to DB : {}", entries);
            sqlSession.commit();
        }
        return customer;
    }
}
