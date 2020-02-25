package com.project.bookmyshow.db.mappers;

import static com.project.bookmyshow.db.mappers.ShowBookingStatusDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import com.project.bookmyshow.db.mappers.ShowBookingStatus;
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
public interface ShowBookingStatusMapper {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.782+05:30", comments="Source Table: TBL_ShowBookingStatus")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    long count(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.782+05:30", comments="Source Table: TBL_ShowBookingStatus")
    @DeleteProvider(type=SqlProviderAdapter.class, method="delete")
    int delete(DeleteStatementProvider deleteStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.782+05:30", comments="Source Table: TBL_ShowBookingStatus")
    @InsertProvider(type=SqlProviderAdapter.class, method="insert")
    @SelectKey(statement="SELECT LAST_INSERT_ID()", keyProperty="record.showBookingStatusId", before=false, resultType=Integer.class)
    int insert(InsertStatementProvider<ShowBookingStatus> insertStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.782+05:30", comments="Source Table: TBL_ShowBookingStatus")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("ShowBookingStatusResult")
    ShowBookingStatus selectOne(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.782+05:30", comments="Source Table: TBL_ShowBookingStatus")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Results(id="ShowBookingStatusResult", value = {
        @Result(column="show_booking_status_id", property="showBookingStatusId", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="booking_id", property="bookingId", jdbcType=JdbcType.INTEGER),
        @Result(column="status_id", property="statusId", jdbcType=JdbcType.INTEGER),
        @Result(column="created_at", property="createdAt", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="modified_at", property="modifiedAt", jdbcType=JdbcType.TIMESTAMP)
    })
    List<ShowBookingStatus> selectMany(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.782+05:30", comments="Source Table: TBL_ShowBookingStatus")
    @UpdateProvider(type=SqlProviderAdapter.class, method="update")
    int update(UpdateStatementProvider updateStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.782+05:30", comments="Source Table: TBL_ShowBookingStatus")
    default QueryExpressionDSL<MyBatis3SelectModelAdapter<Long>> countByExample() {
        return SelectDSL.selectWithMapper(this::count, SqlBuilder.count())
                .from(showBookingStatus);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.782+05:30", comments="Source Table: TBL_ShowBookingStatus")
    default DeleteDSL<MyBatis3DeleteModelAdapter<Integer>> deleteByExample() {
        return DeleteDSL.deleteFromWithMapper(this::delete, showBookingStatus);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.783+05:30", comments="Source Table: TBL_ShowBookingStatus")
    default int deleteByPrimaryKey(Integer showBookingStatusId_) {
        return DeleteDSL.deleteFromWithMapper(this::delete, showBookingStatus)
                .where(showBookingStatusId, isEqualTo(showBookingStatusId_))
                .build()
                .execute();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.783+05:30", comments="Source Table: TBL_ShowBookingStatus")
    default int insert(ShowBookingStatus record) {
        return insert(SqlBuilder.insert(record)
                .into(showBookingStatus)
                .map(bookingId).toProperty("bookingId")
                .map(statusId).toProperty("statusId")
                .map(createdAt).toProperty("createdAt")
                .map(modifiedAt).toProperty("modifiedAt")
                .build()
                .render(RenderingStrategy.MYBATIS3));
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.783+05:30", comments="Source Table: TBL_ShowBookingStatus")
    default int insertSelective(ShowBookingStatus record) {
        return insert(SqlBuilder.insert(record)
                .into(showBookingStatus)
                .map(bookingId).toPropertyWhenPresent("bookingId", record::getBookingId)
                .map(statusId).toPropertyWhenPresent("statusId", record::getStatusId)
                .map(createdAt).toPropertyWhenPresent("createdAt", record::getCreatedAt)
                .map(modifiedAt).toPropertyWhenPresent("modifiedAt", record::getModifiedAt)
                .build()
                .render(RenderingStrategy.MYBATIS3));
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.783+05:30", comments="Source Table: TBL_ShowBookingStatus")
    default QueryExpressionDSL<MyBatis3SelectModelAdapter<List<ShowBookingStatus>>> selectByExample() {
        return SelectDSL.selectWithMapper(this::selectMany, showBookingStatusId, bookingId, statusId, createdAt, modifiedAt)
                .from(showBookingStatus);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.783+05:30", comments="Source Table: TBL_ShowBookingStatus")
    default QueryExpressionDSL<MyBatis3SelectModelAdapter<List<ShowBookingStatus>>> selectDistinctByExample() {
        return SelectDSL.selectDistinctWithMapper(this::selectMany, showBookingStatusId, bookingId, statusId, createdAt, modifiedAt)
                .from(showBookingStatus);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.783+05:30", comments="Source Table: TBL_ShowBookingStatus")
    default ShowBookingStatus selectByPrimaryKey(Integer showBookingStatusId_) {
        return SelectDSL.selectWithMapper(this::selectOne, showBookingStatusId, bookingId, statusId, createdAt, modifiedAt)
                .from(showBookingStatus)
                .where(showBookingStatusId, isEqualTo(showBookingStatusId_))
                .build()
                .execute();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.783+05:30", comments="Source Table: TBL_ShowBookingStatus")
    default UpdateDSL<MyBatis3UpdateModelAdapter<Integer>> updateByExample(ShowBookingStatus record) {
        return UpdateDSL.updateWithMapper(this::update, showBookingStatus)
                .set(bookingId).equalTo(record::getBookingId)
                .set(statusId).equalTo(record::getStatusId)
                .set(createdAt).equalTo(record::getCreatedAt)
                .set(modifiedAt).equalTo(record::getModifiedAt);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.783+05:30", comments="Source Table: TBL_ShowBookingStatus")
    default UpdateDSL<MyBatis3UpdateModelAdapter<Integer>> updateByExampleSelective(ShowBookingStatus record) {
        return UpdateDSL.updateWithMapper(this::update, showBookingStatus)
                .set(bookingId).equalToWhenPresent(record::getBookingId)
                .set(statusId).equalToWhenPresent(record::getStatusId)
                .set(createdAt).equalToWhenPresent(record::getCreatedAt)
                .set(modifiedAt).equalToWhenPresent(record::getModifiedAt);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.783+05:30", comments="Source Table: TBL_ShowBookingStatus")
    default int updateByPrimaryKey(ShowBookingStatus record) {
        return UpdateDSL.updateWithMapper(this::update, showBookingStatus)
                .set(bookingId).equalTo(record::getBookingId)
                .set(statusId).equalTo(record::getStatusId)
                .set(createdAt).equalTo(record::getCreatedAt)
                .set(modifiedAt).equalTo(record::getModifiedAt)
                .where(showBookingStatusId, isEqualTo(record::getShowBookingStatusId))
                .build()
                .execute();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.783+05:30", comments="Source Table: TBL_ShowBookingStatus")
    default int updateByPrimaryKeySelective(ShowBookingStatus record) {
        return UpdateDSL.updateWithMapper(this::update, showBookingStatus)
                .set(bookingId).equalToWhenPresent(record::getBookingId)
                .set(statusId).equalToWhenPresent(record::getStatusId)
                .set(createdAt).equalToWhenPresent(record::getCreatedAt)
                .set(modifiedAt).equalToWhenPresent(record::getModifiedAt)
                .where(showBookingStatusId, isEqualTo(record::getShowBookingStatusId))
                .build()
                .execute();
    }
}