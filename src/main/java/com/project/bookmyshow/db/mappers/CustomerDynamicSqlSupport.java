package com.project.bookmyshow.db.mappers;

import com.project.bookmyshow.enums.CustomerRole;
import java.sql.JDBCType;
import java.util.Date;
import javax.annotation.Generated;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public final class CustomerDynamicSqlSupport {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.705+05:30", comments="Source Table: TBL_Customer")
    public static final Customer customer = new Customer();

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.706+05:30", comments="Source field: TBL_Customer.customer_id")
    public static final SqlColumn<Integer> customerId = customer.customerId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.706+05:30", comments="Source field: TBL_Customer.name")
    public static final SqlColumn<String> name = customer.name;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.707+05:30", comments="Source field: TBL_Customer.email")
    public static final SqlColumn<String> email = customer.email;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.707+05:30", comments="Source field: TBL_Customer.password")
    public static final SqlColumn<String> password = customer.password;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.707+05:30", comments="Source field: TBL_Customer.role")
    public static final SqlColumn<CustomerRole> role = customer.role;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.707+05:30", comments="Source field: TBL_Customer.created_at")
    public static final SqlColumn<Date> createdAt = customer.createdAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.707+05:30", comments="Source field: TBL_Customer.modified_at")
    public static final SqlColumn<Date> modifiedAt = customer.modifiedAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.706+05:30", comments="Source Table: TBL_Customer")
    public static final class Customer extends SqlTable {
        public final SqlColumn<Integer> customerId = column("customer_id", JDBCType.INTEGER);

        public final SqlColumn<String> name = column("name", JDBCType.VARCHAR);

        public final SqlColumn<String> email = column("email", JDBCType.VARCHAR);

        public final SqlColumn<String> password = column("password", JDBCType.VARCHAR);

        public final SqlColumn<CustomerRole> role = column("role", JDBCType.VARCHAR);

        public final SqlColumn<Date> createdAt = column("created_at", JDBCType.TIMESTAMP);

        public final SqlColumn<Date> modifiedAt = column("modified_at", JDBCType.TIMESTAMP);

        public Customer() {
            super("TBL_Customer");
        }
    }
}