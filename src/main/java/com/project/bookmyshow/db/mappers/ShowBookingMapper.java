package com.project.bookmyshow.db.mappers;

import static com.project.bookmyshow.db.mappers.ShowBookingDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import com.project.bookmyshow.db.mappers.ShowBooking;
import com.project.bookmyshow.db.typehandlers.AmountTypeHandler;
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
public interface ShowBookingMapper {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.777+05:30", comments="Source Table: TBL_ShowBooking")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    long count(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.777+05:30", comments="Source Table: TBL_ShowBooking")
    @DeleteProvider(type=SqlProviderAdapter.class, method="delete")
    int delete(DeleteStatementProvider deleteStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.777+05:30", comments="Source Table: TBL_ShowBooking")
    @InsertProvider(type=SqlProviderAdapter.class, method="insert")
    @SelectKey(statement="SELECT LAST_INSERT_ID()", keyProperty="record.bookingId", before=false, resultType=Integer.class)
    int insert(InsertStatementProvider<ShowBooking> insertStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.777+05:30", comments="Source Table: TBL_ShowBooking")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("ShowBookingResult")
    ShowBooking selectOne(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.778+05:30", comments="Source Table: TBL_ShowBooking")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Results(id="ShowBookingResult", value = {
        @Result(column="booking_id", property="bookingId", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="customer_id", property="customerId", jdbcType=JdbcType.INTEGER),
        @Result(column="scheduled_live_show_id", property="scheduledLiveShowId", jdbcType=JdbcType.INTEGER),
        @Result(column="total_seats_booked", property="totalSeatsBooked", jdbcType=JdbcType.INTEGER),
        @Result(column="convenience_fee", property="convenienceFee", jdbcType=JdbcType.INTEGER),
        @Result(column="total_booking_amount", property="totalBookingAmount", typeHandler=AmountTypeHandler.class, jdbcType=JdbcType.INTEGER),
        @Result(column="booking_ref_no", property="bookingRefNo", jdbcType=JdbcType.VARCHAR),
        @Result(column="status_id", property="statusId", jdbcType=JdbcType.INTEGER),
        @Result(column="created_at", property="createdAt", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="modified_at", property="modifiedAt", jdbcType=JdbcType.TIMESTAMP)
    })
    List<ShowBooking> selectMany(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.778+05:30", comments="Source Table: TBL_ShowBooking")
    @UpdateProvider(type=SqlProviderAdapter.class, method="update")
    int update(UpdateStatementProvider updateStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.778+05:30", comments="Source Table: TBL_ShowBooking")
    default QueryExpressionDSL<MyBatis3SelectModelAdapter<Long>> countByExample() {
        return SelectDSL.selectWithMapper(this::count, SqlBuilder.count())
                .from(showBooking);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.778+05:30", comments="Source Table: TBL_ShowBooking")
    default DeleteDSL<MyBatis3DeleteModelAdapter<Integer>> deleteByExample() {
        return DeleteDSL.deleteFromWithMapper(this::delete, showBooking);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.778+05:30", comments="Source Table: TBL_ShowBooking")
    default int deleteByPrimaryKey(Integer bookingId_) {
        return DeleteDSL.deleteFromWithMapper(this::delete, showBooking)
                .where(bookingId, isEqualTo(bookingId_))
                .build()
                .execute();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.778+05:30", comments="Source Table: TBL_ShowBooking")
    default int insert(ShowBooking record) {
        return insert(SqlBuilder.insert(record)
                .into(showBooking)
                .map(customerId).toProperty("customerId")
                .map(scheduledLiveShowId).toProperty("scheduledLiveShowId")
                .map(totalSeatsBooked).toProperty("totalSeatsBooked")
                .map(convenienceFee).toProperty("convenienceFee")
                .map(totalBookingAmount).toProperty("totalBookingAmount")
                .map(bookingRefNo).toProperty("bookingRefNo")
                .map(statusId).toProperty("statusId")
                .map(createdAt).toProperty("createdAt")
                .map(modifiedAt).toProperty("modifiedAt")
                .build()
                .render(RenderingStrategy.MYBATIS3));
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.778+05:30", comments="Source Table: TBL_ShowBooking")
    default int insertSelective(ShowBooking record) {
        return insert(SqlBuilder.insert(record)
                .into(showBooking)
                .map(customerId).toPropertyWhenPresent("customerId", record::getCustomerId)
                .map(scheduledLiveShowId).toPropertyWhenPresent("scheduledLiveShowId", record::getScheduledLiveShowId)
                .map(totalSeatsBooked).toPropertyWhenPresent("totalSeatsBooked", record::getTotalSeatsBooked)
                .map(convenienceFee).toPropertyWhenPresent("convenienceFee", record::getConvenienceFee)
                .map(totalBookingAmount).toPropertyWhenPresent("totalBookingAmount", record::getTotalBookingAmount)
                .map(bookingRefNo).toPropertyWhenPresent("bookingRefNo", record::getBookingRefNo)
                .map(statusId).toPropertyWhenPresent("statusId", record::getStatusId)
                .map(createdAt).toPropertyWhenPresent("createdAt", record::getCreatedAt)
                .map(modifiedAt).toPropertyWhenPresent("modifiedAt", record::getModifiedAt)
                .build()
                .render(RenderingStrategy.MYBATIS3));
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.778+05:30", comments="Source Table: TBL_ShowBooking")
    default QueryExpressionDSL<MyBatis3SelectModelAdapter<List<ShowBooking>>> selectByExample() {
        return SelectDSL.selectWithMapper(this::selectMany, bookingId, customerId, scheduledLiveShowId, totalSeatsBooked, convenienceFee, totalBookingAmount, bookingRefNo, statusId, createdAt, modifiedAt)
                .from(showBooking);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.778+05:30", comments="Source Table: TBL_ShowBooking")
    default QueryExpressionDSL<MyBatis3SelectModelAdapter<List<ShowBooking>>> selectDistinctByExample() {
        return SelectDSL.selectDistinctWithMapper(this::selectMany, bookingId, customerId, scheduledLiveShowId, totalSeatsBooked, convenienceFee, totalBookingAmount, bookingRefNo, statusId, createdAt, modifiedAt)
                .from(showBooking);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.779+05:30", comments="Source Table: TBL_ShowBooking")
    default ShowBooking selectByPrimaryKey(Integer bookingId_) {
        return SelectDSL.selectWithMapper(this::selectOne, bookingId, customerId, scheduledLiveShowId, totalSeatsBooked, convenienceFee, totalBookingAmount, bookingRefNo, statusId, createdAt, modifiedAt)
                .from(showBooking)
                .where(bookingId, isEqualTo(bookingId_))
                .build()
                .execute();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.779+05:30", comments="Source Table: TBL_ShowBooking")
    default UpdateDSL<MyBatis3UpdateModelAdapter<Integer>> updateByExample(ShowBooking record) {
        return UpdateDSL.updateWithMapper(this::update, showBooking)
                .set(customerId).equalTo(record::getCustomerId)
                .set(scheduledLiveShowId).equalTo(record::getScheduledLiveShowId)
                .set(totalSeatsBooked).equalTo(record::getTotalSeatsBooked)
                .set(convenienceFee).equalTo(record::getConvenienceFee)
                .set(totalBookingAmount).equalTo(record::getTotalBookingAmount)
                .set(bookingRefNo).equalTo(record::getBookingRefNo)
                .set(statusId).equalTo(record::getStatusId)
                .set(createdAt).equalTo(record::getCreatedAt)
                .set(modifiedAt).equalTo(record::getModifiedAt);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.779+05:30", comments="Source Table: TBL_ShowBooking")
    default UpdateDSL<MyBatis3UpdateModelAdapter<Integer>> updateByExampleSelective(ShowBooking record) {
        return UpdateDSL.updateWithMapper(this::update, showBooking)
                .set(customerId).equalToWhenPresent(record::getCustomerId)
                .set(scheduledLiveShowId).equalToWhenPresent(record::getScheduledLiveShowId)
                .set(totalSeatsBooked).equalToWhenPresent(record::getTotalSeatsBooked)
                .set(convenienceFee).equalToWhenPresent(record::getConvenienceFee)
                .set(totalBookingAmount).equalToWhenPresent(record::getTotalBookingAmount)
                .set(bookingRefNo).equalToWhenPresent(record::getBookingRefNo)
                .set(statusId).equalToWhenPresent(record::getStatusId)
                .set(createdAt).equalToWhenPresent(record::getCreatedAt)
                .set(modifiedAt).equalToWhenPresent(record::getModifiedAt);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.779+05:30", comments="Source Table: TBL_ShowBooking")
    default int updateByPrimaryKey(ShowBooking record) {
        return UpdateDSL.updateWithMapper(this::update, showBooking)
                .set(customerId).equalTo(record::getCustomerId)
                .set(scheduledLiveShowId).equalTo(record::getScheduledLiveShowId)
                .set(totalSeatsBooked).equalTo(record::getTotalSeatsBooked)
                .set(convenienceFee).equalTo(record::getConvenienceFee)
                .set(totalBookingAmount).equalTo(record::getTotalBookingAmount)
                .set(bookingRefNo).equalTo(record::getBookingRefNo)
                .set(statusId).equalTo(record::getStatusId)
                .set(createdAt).equalTo(record::getCreatedAt)
                .set(modifiedAt).equalTo(record::getModifiedAt)
                .where(bookingId, isEqualTo(record::getBookingId))
                .build()
                .execute();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.779+05:30", comments="Source Table: TBL_ShowBooking")
    default int updateByPrimaryKeySelective(ShowBooking record) {
        return UpdateDSL.updateWithMapper(this::update, showBooking)
                .set(customerId).equalToWhenPresent(record::getCustomerId)
                .set(scheduledLiveShowId).equalToWhenPresent(record::getScheduledLiveShowId)
                .set(totalSeatsBooked).equalToWhenPresent(record::getTotalSeatsBooked)
                .set(convenienceFee).equalToWhenPresent(record::getConvenienceFee)
                .set(totalBookingAmount).equalToWhenPresent(record::getTotalBookingAmount)
                .set(bookingRefNo).equalToWhenPresent(record::getBookingRefNo)
                .set(statusId).equalToWhenPresent(record::getStatusId)
                .set(createdAt).equalToWhenPresent(record::getCreatedAt)
                .set(modifiedAt).equalToWhenPresent(record::getModifiedAt)
                .where(bookingId, isEqualTo(record::getBookingId))
                .build()
                .execute();
    }
}