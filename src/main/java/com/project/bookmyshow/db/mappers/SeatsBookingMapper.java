package com.project.bookmyshow.db.mappers;

import static com.project.bookmyshow.db.mappers.SeatsBookingDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import com.project.bookmyshow.db.mappers.SeatsBooking;
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
public interface SeatsBookingMapper {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.765+05:30", comments="Source Table: TBL_SeatsBooking")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    long count(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.765+05:30", comments="Source Table: TBL_SeatsBooking")
    @DeleteProvider(type=SqlProviderAdapter.class, method="delete")
    int delete(DeleteStatementProvider deleteStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.765+05:30", comments="Source Table: TBL_SeatsBooking")
    @InsertProvider(type=SqlProviderAdapter.class, method="insert")
    @SelectKey(statement="SELECT LAST_INSERT_ID()", keyProperty="record.seatBookingId", before=false, resultType=Integer.class)
    int insert(InsertStatementProvider<SeatsBooking> insertStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.766+05:30", comments="Source Table: TBL_SeatsBooking")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("SeatsBookingResult")
    SeatsBooking selectOne(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.766+05:30", comments="Source Table: TBL_SeatsBooking")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Results(id="SeatsBookingResult", value = {
        @Result(column="seat_booking_id", property="seatBookingId", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="seat_id", property="seatId", jdbcType=JdbcType.INTEGER),
        @Result(column="booking_id", property="bookingId", jdbcType=JdbcType.INTEGER),
        @Result(column="scheduled_live_show_id", property="scheduledLiveShowId", jdbcType=JdbcType.INTEGER),
        @Result(column="seat_booking_status", property="seatBookingStatus", jdbcType=JdbcType.INTEGER),
        @Result(column="created_at", property="createdAt", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="modified_at", property="modifiedAt", jdbcType=JdbcType.TIMESTAMP)
    })
    List<SeatsBooking> selectMany(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.766+05:30", comments="Source Table: TBL_SeatsBooking")
    @UpdateProvider(type=SqlProviderAdapter.class, method="update")
    int update(UpdateStatementProvider updateStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.766+05:30", comments="Source Table: TBL_SeatsBooking")
    default QueryExpressionDSL<MyBatis3SelectModelAdapter<Long>> countByExample() {
        return SelectDSL.selectWithMapper(this::count, SqlBuilder.count())
                .from(seatsBooking);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.766+05:30", comments="Source Table: TBL_SeatsBooking")
    default DeleteDSL<MyBatis3DeleteModelAdapter<Integer>> deleteByExample() {
        return DeleteDSL.deleteFromWithMapper(this::delete, seatsBooking);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.766+05:30", comments="Source Table: TBL_SeatsBooking")
    default int deleteByPrimaryKey(Integer seatBookingId_) {
        return DeleteDSL.deleteFromWithMapper(this::delete, seatsBooking)
                .where(seatBookingId, isEqualTo(seatBookingId_))
                .build()
                .execute();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.766+05:30", comments="Source Table: TBL_SeatsBooking")
    default int insert(SeatsBooking record) {
        return insert(SqlBuilder.insert(record)
                .into(seatsBooking)
                .map(seatId).toProperty("seatId")
                .map(bookingId).toProperty("bookingId")
                .map(scheduledLiveShowId).toProperty("scheduledLiveShowId")
                .map(seatBookingStatus).toProperty("seatBookingStatus")
                .map(createdAt).toProperty("createdAt")
                .map(modifiedAt).toProperty("modifiedAt")
                .build()
                .render(RenderingStrategy.MYBATIS3));
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.766+05:30", comments="Source Table: TBL_SeatsBooking")
    default int insertSelective(SeatsBooking record) {
        return insert(SqlBuilder.insert(record)
                .into(seatsBooking)
                .map(seatId).toPropertyWhenPresent("seatId", record::getSeatId)
                .map(bookingId).toPropertyWhenPresent("bookingId", record::getBookingId)
                .map(scheduledLiveShowId).toPropertyWhenPresent("scheduledLiveShowId", record::getScheduledLiveShowId)
                .map(seatBookingStatus).toPropertyWhenPresent("seatBookingStatus", record::getSeatBookingStatus)
                .map(createdAt).toPropertyWhenPresent("createdAt", record::getCreatedAt)
                .map(modifiedAt).toPropertyWhenPresent("modifiedAt", record::getModifiedAt)
                .build()
                .render(RenderingStrategy.MYBATIS3));
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.766+05:30", comments="Source Table: TBL_SeatsBooking")
    default QueryExpressionDSL<MyBatis3SelectModelAdapter<List<SeatsBooking>>> selectByExample() {
        return SelectDSL.selectWithMapper(this::selectMany, seatBookingId, seatId, bookingId, scheduledLiveShowId, seatBookingStatus, createdAt, modifiedAt)
                .from(seatsBooking);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.766+05:30", comments="Source Table: TBL_SeatsBooking")
    default QueryExpressionDSL<MyBatis3SelectModelAdapter<List<SeatsBooking>>> selectDistinctByExample() {
        return SelectDSL.selectDistinctWithMapper(this::selectMany, seatBookingId, seatId, bookingId, scheduledLiveShowId, seatBookingStatus, createdAt, modifiedAt)
                .from(seatsBooking);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.767+05:30", comments="Source Table: TBL_SeatsBooking")
    default SeatsBooking selectByPrimaryKey(Integer seatBookingId_) {
        return SelectDSL.selectWithMapper(this::selectOne, seatBookingId, seatId, bookingId, scheduledLiveShowId, seatBookingStatus, createdAt, modifiedAt)
                .from(seatsBooking)
                .where(seatBookingId, isEqualTo(seatBookingId_))
                .build()
                .execute();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.767+05:30", comments="Source Table: TBL_SeatsBooking")
    default UpdateDSL<MyBatis3UpdateModelAdapter<Integer>> updateByExample(SeatsBooking record) {
        return UpdateDSL.updateWithMapper(this::update, seatsBooking)
                .set(seatId).equalTo(record::getSeatId)
                .set(bookingId).equalTo(record::getBookingId)
                .set(scheduledLiveShowId).equalTo(record::getScheduledLiveShowId)
                .set(seatBookingStatus).equalTo(record::getSeatBookingStatus)
                .set(createdAt).equalTo(record::getCreatedAt)
                .set(modifiedAt).equalTo(record::getModifiedAt);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.767+05:30", comments="Source Table: TBL_SeatsBooking")
    default UpdateDSL<MyBatis3UpdateModelAdapter<Integer>> updateByExampleSelective(SeatsBooking record) {
        return UpdateDSL.updateWithMapper(this::update, seatsBooking)
                .set(seatId).equalToWhenPresent(record::getSeatId)
                .set(bookingId).equalToWhenPresent(record::getBookingId)
                .set(scheduledLiveShowId).equalToWhenPresent(record::getScheduledLiveShowId)
                .set(seatBookingStatus).equalToWhenPresent(record::getSeatBookingStatus)
                .set(createdAt).equalToWhenPresent(record::getCreatedAt)
                .set(modifiedAt).equalToWhenPresent(record::getModifiedAt);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.767+05:30", comments="Source Table: TBL_SeatsBooking")
    default int updateByPrimaryKey(SeatsBooking record) {
        return UpdateDSL.updateWithMapper(this::update, seatsBooking)
                .set(seatId).equalTo(record::getSeatId)
                .set(bookingId).equalTo(record::getBookingId)
                .set(scheduledLiveShowId).equalTo(record::getScheduledLiveShowId)
                .set(seatBookingStatus).equalTo(record::getSeatBookingStatus)
                .set(createdAt).equalTo(record::getCreatedAt)
                .set(modifiedAt).equalTo(record::getModifiedAt)
                .where(seatBookingId, isEqualTo(record::getSeatBookingId))
                .build()
                .execute();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.767+05:30", comments="Source Table: TBL_SeatsBooking")
    default int updateByPrimaryKeySelective(SeatsBooking record) {
        return UpdateDSL.updateWithMapper(this::update, seatsBooking)
                .set(seatId).equalToWhenPresent(record::getSeatId)
                .set(bookingId).equalToWhenPresent(record::getBookingId)
                .set(scheduledLiveShowId).equalToWhenPresent(record::getScheduledLiveShowId)
                .set(seatBookingStatus).equalToWhenPresent(record::getSeatBookingStatus)
                .set(createdAt).equalToWhenPresent(record::getCreatedAt)
                .set(modifiedAt).equalToWhenPresent(record::getModifiedAt)
                .where(seatBookingId, isEqualTo(record::getSeatBookingId))
                .build()
                .execute();
    }
}