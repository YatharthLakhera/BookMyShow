package com.project.bookmyshow.db.mappers;

import static com.project.bookmyshow.db.mappers.CustomerDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import com.project.bookmyshow.db.mappers.Customer;
import java.util.List;
import javax.annotation.Generated;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.SelectKey;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.dynamic.sql.SqlBuilder;
import org.mybatis.dynamic.sql.delete.DeleteDSL;
import org.mybatis.dynamic.sql.delete.MyBatis3DeleteModelAdapter;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.insert.render.InsertStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.MyBatis3SelectModelAdapter;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL;
import org.mybatis.dynamic.sql.select.SelectDSL;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.MyBatis3UpdateModelAdapter;
import org.mybatis.dynamic.sql.update.UpdateDSL;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;

@Mapper
public interface CustomerMapper {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.709+05:30", comments="Source Table: TBL_Customer")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    long count(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.711+05:30", comments="Source Table: TBL_Customer")
    @DeleteProvider(type=SqlProviderAdapter.class, method="delete")
    int delete(DeleteStatementProvider deleteStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.712+05:30", comments="Source Table: TBL_Customer")
    @InsertProvider(type=SqlProviderAdapter.class, method="insert")
    @SelectKey(statement="SELECT LAST_INSERT_ID()", keyProperty="record.customerId", before=false, resultType=Integer.class)
    int insert(InsertStatementProvider<Customer> insertStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.713+05:30", comments="Source Table: TBL_Customer")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("CustomerResult")
    Customer selectOne(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.715+05:30", comments="Source Table: TBL_Customer")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Results(id="CustomerResult", value = {
        @Result(column="customer_id", property="customerId", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="name", property="name", jdbcType=JdbcType.VARCHAR),
        @Result(column="email", property="email", jdbcType=JdbcType.VARCHAR),
        @Result(column="password", property="password", jdbcType=JdbcType.VARCHAR),
        @Result(column="role", property="role", jdbcType=JdbcType.VARCHAR),
        @Result(column="created_at", property="createdAt", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="modified_at", property="modifiedAt", jdbcType=JdbcType.TIMESTAMP)
    })
    List<Customer> selectMany(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.716+05:30", comments="Source Table: TBL_Customer")
    @UpdateProvider(type=SqlProviderAdapter.class, method="update")
    int update(UpdateStatementProvider updateStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.717+05:30", comments="Source Table: TBL_Customer")
    default QueryExpressionDSL<MyBatis3SelectModelAdapter<Long>> countByExample() {
        return SelectDSL.selectWithMapper(this::count, SqlBuilder.count())
                .from(customer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.718+05:30", comments="Source Table: TBL_Customer")
    default DeleteDSL<MyBatis3DeleteModelAdapter<Integer>> deleteByExample() {
        return DeleteDSL.deleteFromWithMapper(this::delete, customer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.718+05:30", comments="Source Table: TBL_Customer")
    default int deleteByPrimaryKey(Integer customerId_) {
        return DeleteDSL.deleteFromWithMapper(this::delete, customer)
                .where(customerId, isEqualTo(customerId_))
                .build()
                .execute();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.719+05:30", comments="Source Table: TBL_Customer")
    default int insert(Customer record) {
        return insert(SqlBuilder.insert(record)
                .into(customer)
                .map(name).toProperty("name")
                .map(email).toProperty("email")
                .map(password).toProperty("password")
                .map(role).toProperty("role")
                .map(createdAt).toProperty("createdAt")
                .map(modifiedAt).toProperty("modifiedAt")
                .build()
                .render(RenderingStrategy.MYBATIS3));
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.721+05:30", comments="Source Table: TBL_Customer")
    default int insertSelective(Customer record) {
        return insert(SqlBuilder.insert(record)
                .into(customer)
                .map(name).toPropertyWhenPresent("name", record::getName)
                .map(email).toPropertyWhenPresent("email", record::getEmail)
                .map(password).toPropertyWhenPresent("password", record::getPassword)
                .map(role).toPropertyWhenPresent("role", record::getRole)
                .map(createdAt).toPropertyWhenPresent("createdAt", record::getCreatedAt)
                .map(modifiedAt).toPropertyWhenPresent("modifiedAt", record::getModifiedAt)
                .build()
                .render(RenderingStrategy.MYBATIS3));
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.722+05:30", comments="Source Table: TBL_Customer")
    default QueryExpressionDSL<MyBatis3SelectModelAdapter<List<Customer>>> selectByExample() {
        return SelectDSL.selectWithMapper(this::selectMany, customerId, name, email, password, role, createdAt, modifiedAt)
                .from(customer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.723+05:30", comments="Source Table: TBL_Customer")
    default QueryExpressionDSL<MyBatis3SelectModelAdapter<List<Customer>>> selectDistinctByExample() {
        return SelectDSL.selectDistinctWithMapper(this::selectMany, customerId, name, email, password, role, createdAt, modifiedAt)
                .from(customer);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.724+05:30", comments="Source Table: TBL_Customer")
    default Customer selectByPrimaryKey(Integer customerId_) {
        return SelectDSL.selectWithMapper(this::selectOne, customerId, name, email, password, role, createdAt, modifiedAt)
                .from(customer)
                .where(customerId, isEqualTo(customerId_))
                .build()
                .execute();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.725+05:30", comments="Source Table: TBL_Customer")
    default UpdateDSL<MyBatis3UpdateModelAdapter<Integer>> updateByExample(Customer record) {
        return UpdateDSL.updateWithMapper(this::update, customer)
                .set(name).equalTo(record::getName)
                .set(email).equalTo(record::getEmail)
                .set(password).equalTo(record::getPassword)
                .set(role).equalTo(record::getRole)
                .set(createdAt).equalTo(record::getCreatedAt)
                .set(modifiedAt).equalTo(record::getModifiedAt);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.726+05:30", comments="Source Table: TBL_Customer")
    default UpdateDSL<MyBatis3UpdateModelAdapter<Integer>> updateByExampleSelective(Customer record) {
        return UpdateDSL.updateWithMapper(this::update, customer)
                .set(name).equalToWhenPresent(record::getName)
                .set(email).equalToWhenPresent(record::getEmail)
                .set(password).equalToWhenPresent(record::getPassword)
                .set(role).equalToWhenPresent(record::getRole)
                .set(createdAt).equalToWhenPresent(record::getCreatedAt)
                .set(modifiedAt).equalToWhenPresent(record::getModifiedAt);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.727+05:30", comments="Source Table: TBL_Customer")
    default int updateByPrimaryKey(Customer record) {
        return UpdateDSL.updateWithMapper(this::update, customer)
                .set(name).equalTo(record::getName)
                .set(email).equalTo(record::getEmail)
                .set(password).equalTo(record::getPassword)
                .set(role).equalTo(record::getRole)
                .set(createdAt).equalTo(record::getCreatedAt)
                .set(modifiedAt).equalTo(record::getModifiedAt)
                .where(customerId, isEqualTo(record::getCustomerId))
                .build()
                .execute();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.728+05:30", comments="Source Table: TBL_Customer")
    default int updateByPrimaryKeySelective(Customer record) {
        return UpdateDSL.updateWithMapper(this::update, customer)
                .set(name).equalToWhenPresent(record::getName)
                .set(email).equalToWhenPresent(record::getEmail)
                .set(password).equalToWhenPresent(record::getPassword)
                .set(role).equalToWhenPresent(record::getRole)
                .set(createdAt).equalToWhenPresent(record::getCreatedAt)
                .set(modifiedAt).equalToWhenPresent(record::getModifiedAt)
                .where(customerId, isEqualTo(record::getCustomerId))
                .build()
                .execute();
    }
}