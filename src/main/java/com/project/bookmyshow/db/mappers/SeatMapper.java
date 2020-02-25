package com.project.bookmyshow.db.mappers;

import static com.project.bookmyshow.db.mappers.SeatDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import com.project.bookmyshow.db.mappers.Seat;
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
public interface SeatMapper {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.759+05:30", comments="Source Table: TBL_Seat")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    long count(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.759+05:30", comments="Source Table: TBL_Seat")
    @DeleteProvider(type=SqlProviderAdapter.class, method="delete")
    int delete(DeleteStatementProvider deleteStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.759+05:30", comments="Source Table: TBL_Seat")
    @InsertProvider(type=SqlProviderAdapter.class, method="insert")
    @SelectKey(statement="SELECT LAST_INSERT_ID()", keyProperty="record.seatId", before=false, resultType=Integer.class)
    int insert(InsertStatementProvider<Seat> insertStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.759+05:30", comments="Source Table: TBL_Seat")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("SeatResult")
    Seat selectOne(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.76+05:30", comments="Source Table: TBL_Seat")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Results(id="SeatResult", value = {
        @Result(column="seat_id", property="seatId", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="seat_code", property="seatCode", jdbcType=JdbcType.VARCHAR),
        @Result(column="hall_id", property="hallId", jdbcType=JdbcType.INTEGER),
        @Result(column="seat_row_loc", property="seatRowLoc", jdbcType=JdbcType.INTEGER),
        @Result(column="seat_col_loc", property="seatColLoc", jdbcType=JdbcType.INTEGER),
        @Result(column="created_at", property="createdAt", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="modified_at", property="modifiedAt", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="extraData", property="extradata", jdbcType=JdbcType.LONGVARCHAR)
    })
    List<Seat> selectMany(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.76+05:30", comments="Source Table: TBL_Seat")
    @UpdateProvider(type=SqlProviderAdapter.class, method="update")
    int update(UpdateStatementProvider updateStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.76+05:30", comments="Source Table: TBL_Seat")
    default QueryExpressionDSL<MyBatis3SelectModelAdapter<Long>> countByExample() {
        return SelectDSL.selectWithMapper(this::count, SqlBuilder.count())
                .from(seat);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.76+05:30", comments="Source Table: TBL_Seat")
    default DeleteDSL<MyBatis3DeleteModelAdapter<Integer>> deleteByExample() {
        return DeleteDSL.deleteFromWithMapper(this::delete, seat);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.76+05:30", comments="Source Table: TBL_Seat")
    default int deleteByPrimaryKey(Integer seatId_) {
        return DeleteDSL.deleteFromWithMapper(this::delete, seat)
                .where(seatId, isEqualTo(seatId_))
                .build()
                .execute();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.76+05:30", comments="Source Table: TBL_Seat")
    default int insert(Seat record) {
        return insert(SqlBuilder.insert(record)
                .into(seat)
                .map(seatCode).toProperty("seatCode")
                .map(hallId).toProperty("hallId")
                .map(seatRowLoc).toProperty("seatRowLoc")
                .map(seatColLoc).toProperty("seatColLoc")
                .map(createdAt).toProperty("createdAt")
                .map(modifiedAt).toProperty("modifiedAt")
                .map(extradata).toProperty("extradata")
                .build()
                .render(RenderingStrategy.MYBATIS3));
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.76+05:30", comments="Source Table: TBL_Seat")
    default int insertSelective(Seat record) {
        return insert(SqlBuilder.insert(record)
                .into(seat)
                .map(seatCode).toPropertyWhenPresent("seatCode", record::getSeatCode)
                .map(hallId).toPropertyWhenPresent("hallId", record::getHallId)
                .map(seatRowLoc).toPropertyWhenPresent("seatRowLoc", record::getSeatRowLoc)
                .map(seatColLoc).toPropertyWhenPresent("seatColLoc", record::getSeatColLoc)
                .map(createdAt).toPropertyWhenPresent("createdAt", record::getCreatedAt)
                .map(modifiedAt).toPropertyWhenPresent("modifiedAt", record::getModifiedAt)
                .map(extradata).toPropertyWhenPresent("extradata", record::getExtradata)
                .build()
                .render(RenderingStrategy.MYBATIS3));
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.76+05:30", comments="Source Table: TBL_Seat")
    default QueryExpressionDSL<MyBatis3SelectModelAdapter<List<Seat>>> selectByExample() {
        return SelectDSL.selectWithMapper(this::selectMany, seatId, seatCode, hallId, seatRowLoc, seatColLoc, createdAt, modifiedAt, extradata)
                .from(seat);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.761+05:30", comments="Source Table: TBL_Seat")
    default QueryExpressionDSL<MyBatis3SelectModelAdapter<List<Seat>>> selectDistinctByExample() {
        return SelectDSL.selectDistinctWithMapper(this::selectMany, seatId, seatCode, hallId, seatRowLoc, seatColLoc, createdAt, modifiedAt, extradata)
                .from(seat);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.761+05:30", comments="Source Table: TBL_Seat")
    default Seat selectByPrimaryKey(Integer seatId_) {
        return SelectDSL.selectWithMapper(this::selectOne, seatId, seatCode, hallId, seatRowLoc, seatColLoc, createdAt, modifiedAt, extradata)
                .from(seat)
                .where(seatId, isEqualTo(seatId_))
                .build()
                .execute();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.761+05:30", comments="Source Table: TBL_Seat")
    default UpdateDSL<MyBatis3UpdateModelAdapter<Integer>> updateByExample(Seat record) {
        return UpdateDSL.updateWithMapper(this::update, seat)
                .set(seatCode).equalTo(record::getSeatCode)
                .set(hallId).equalTo(record::getHallId)
                .set(seatRowLoc).equalTo(record::getSeatRowLoc)
                .set(seatColLoc).equalTo(record::getSeatColLoc)
                .set(createdAt).equalTo(record::getCreatedAt)
                .set(modifiedAt).equalTo(record::getModifiedAt)
                .set(extradata).equalTo(record::getExtradata);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.761+05:30", comments="Source Table: TBL_Seat")
    default UpdateDSL<MyBatis3UpdateModelAdapter<Integer>> updateByExampleSelective(Seat record) {
        return UpdateDSL.updateWithMapper(this::update, seat)
                .set(seatCode).equalToWhenPresent(record::getSeatCode)
                .set(hallId).equalToWhenPresent(record::getHallId)
                .set(seatRowLoc).equalToWhenPresent(record::getSeatRowLoc)
                .set(seatColLoc).equalToWhenPresent(record::getSeatColLoc)
                .set(createdAt).equalToWhenPresent(record::getCreatedAt)
                .set(modifiedAt).equalToWhenPresent(record::getModifiedAt)
                .set(extradata).equalToWhenPresent(record::getExtradata);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.761+05:30", comments="Source Table: TBL_Seat")
    default int updateByPrimaryKey(Seat record) {
        return UpdateDSL.updateWithMapper(this::update, seat)
                .set(seatCode).equalTo(record::getSeatCode)
                .set(hallId).equalTo(record::getHallId)
                .set(seatRowLoc).equalTo(record::getSeatRowLoc)
                .set(seatColLoc).equalTo(record::getSeatColLoc)
                .set(createdAt).equalTo(record::getCreatedAt)
                .set(modifiedAt).equalTo(record::getModifiedAt)
                .set(extradata).equalTo(record::getExtradata)
                .where(seatId, isEqualTo(record::getSeatId))
                .build()
                .execute();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.761+05:30", comments="Source Table: TBL_Seat")
    default int updateByPrimaryKeySelective(Seat record) {
        return UpdateDSL.updateWithMapper(this::update, seat)
                .set(seatCode).equalToWhenPresent(record::getSeatCode)
                .set(hallId).equalToWhenPresent(record::getHallId)
                .set(seatRowLoc).equalToWhenPresent(record::getSeatRowLoc)
                .set(seatColLoc).equalToWhenPresent(record::getSeatColLoc)
                .set(createdAt).equalToWhenPresent(record::getCreatedAt)
                .set(modifiedAt).equalToWhenPresent(record::getModifiedAt)
                .set(extradata).equalToWhenPresent(record::getExtradata)
                .where(seatId, isEqualTo(record::getSeatId))
                .build()
                .execute();
    }
}