package com.project.bookmyshow.db.mappers;

import com.project.bookmyshow.enums.CustomerRole;
import java.util.Date;
import javax.annotation.Generated;

public class Customer {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.693+05:30", comments="Source field: TBL_Customer.customer_id")
    private Integer customerId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.698+05:30", comments="Source field: TBL_Customer.name")
    private String name;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.699+05:30", comments="Source field: TBL_Customer.email")
    private String email;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.699+05:30", comments="Source field: TBL_Customer.password")
    private String password;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.699+05:30", comments="Source field: TBL_Customer.role")
    private CustomerRole role;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.699+05:30", comments="Source field: TBL_Customer.created_at")
    private Date createdAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.7+05:30", comments="Source field: TBL_Customer.modified_at")
    private Date modifiedAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.697+05:30", comments="Source field: TBL_Customer.customer_id")
    public Integer getCustomerId() {
        return customerId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.698+05:30", comments="Source field: TBL_Customer.customer_id")
    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.698+05:30", comments="Source field: TBL_Customer.name")
    public String getName() {
        return name;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.698+05:30", comments="Source field: TBL_Customer.name")
    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.699+05:30", comments="Source field: TBL_Customer.email")
    public String getEmail() {
        return email;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.699+05:30", comments="Source field: TBL_Customer.email")
    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.699+05:30", comments="Source field: TBL_Customer.password")
    public String getPassword() {
        return password;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.699+05:30", comments="Source field: TBL_Customer.password")
    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.699+05:30", comments="Source field: TBL_Customer.role")
    public CustomerRole getRole() {
        return role;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.699+05:30", comments="Source field: TBL_Customer.role")
    public void setRole(CustomerRole role) {
        this.role = role;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.699+05:30", comments="Source field: TBL_Customer.created_at")
    public Date getCreatedAt() {
        return createdAt;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.7+05:30", comments="Source field: TBL_Customer.created_at")
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.7+05:30", comments="Source field: TBL_Customer.modified_at")
    public Date getModifiedAt() {
        return modifiedAt;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.7+05:30", comments="Source field: TBL_Customer.modified_at")
    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }
}