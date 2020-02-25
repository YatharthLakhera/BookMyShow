package com.project.bookmyshow.db.mappers;

import static com.project.bookmyshow.db.mappers.HallDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import com.project.bookmyshow.db.mappers.Hall;
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
public interface HallMapper {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.739+05:30", comments="Source Table: TBL_Hall")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    long count(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.74+05:30", comments="Source Table: TBL_Hall")
    @DeleteProvider(type=SqlProviderAdapter.class, method="delete")
    int delete(DeleteStatementProvider deleteStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.74+05:30", comments="Source Table: TBL_Hall")
    @InsertProvider(type=SqlProviderAdapter.class, method="insert")
    @SelectKey(statement="SELECT LAST_INSERT_ID()", keyProperty="record.hallId", before=false, resultType=Integer.class)
    int insert(InsertStatementProvider<Hall> insertStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.74+05:30", comments="Source Table: TBL_Hall")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("HallResult")
    Hall selectOne(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.74+05:30", comments="Source Table: TBL_Hall")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Results(id="HallResult", value = {
        @Result(column="hall_id", property="hallId", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="hall_code", property="hallCode", jdbcType=JdbcType.VARCHAR),
        @Result(column="cinema_id", property="cinemaId", jdbcType=JdbcType.INTEGER),
        @Result(column="hall_row_count", property="hallRowCount", jdbcType=JdbcType.INTEGER),
        @Result(column="hall_col_count", property="hallColCount", jdbcType=JdbcType.INTEGER),
        @Result(column="is_available", property="isAvailable", jdbcType=JdbcType.BIT),
        @Result(column="created_at", property="createdAt", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="modified_at", property="modifiedAt", jdbcType=JdbcType.TIMESTAMP)
    })
    List<Hall> selectMany(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.74+05:30", comments="Source Table: TBL_Hall")
    @UpdateProvider(type=SqlProviderAdapter.class, method="update")
    int update(UpdateStatementProvider updateStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.74+05:30", comments="Source Table: TBL_Hall")
    default QueryExpressionDSL<MyBatis3SelectModelAdapter<Long>> countByExample() {
        return SelectDSL.selectWithMapper(this::count, SqlBuilder.count())
                .from(hall);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.741+05:30", comments="Source Table: TBL_Hall")
    default DeleteDSL<MyBatis3DeleteModelAdapter<Integer>> deleteByExample() {
        return DeleteDSL.deleteFromWithMapper(this::delete, hall);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.741+05:30", comments="Source Table: TBL_Hall")
    default int deleteByPrimaryKey(Integer hallId_) {
        return DeleteDSL.deleteFromWithMapper(this::delete, hall)
                .where(hallId, isEqualTo(hallId_))
                .build()
                .execute();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.741+05:30", comments="Source Table: TBL_Hall")
    default int insert(Hall record) {
        return insert(SqlBuilder.insert(record)
                .into(hall)
                .map(hallCode).toProperty("hallCode")
                .map(cinemaId).toProperty("cinemaId")
                .map(hallRowCount).toProperty("hallRowCount")
                .map(hallColCount).toProperty("hallColCount")
                .map(isAvailable).toProperty("isAvailable")
                .map(createdAt).toProperty("createdAt")
                .map(modifiedAt).toProperty("modifiedAt")
                .build()
                .render(RenderingStrategy.MYBATIS3));
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.741+05:30", comments="Source Table: TBL_Hall")
    default int insertSelective(Hall record) {
        return insert(SqlBuilder.insert(record)
                .into(hall)
                .map(hallCode).toPropertyWhenPresent("hallCode", record::getHallCode)
                .map(cinemaId).toPropertyWhenPresent("cinemaId", record::getCinemaId)
                .map(hallRowCount).toPropertyWhenPresent("hallRowCount", record::getHallRowCount)
                .map(hallColCount).toPropertyWhenPresent("hallColCount", record::getHallColCount)
                .map(isAvailable).toPropertyWhenPresent("isAvailable", record::getIsAvailable)
                .map(createdAt).toPropertyWhenPresent("createdAt", record::getCreatedAt)
                .map(modifiedAt).toPropertyWhenPresent("modifiedAt", record::getModifiedAt)
                .build()
                .render(RenderingStrategy.MYBATIS3));
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.741+05:30", comments="Source Table: TBL_Hall")
    default QueryExpressionDSL<MyBatis3SelectModelAdapter<List<Hall>>> selectByExample() {
        return SelectDSL.selectWithMapper(this::selectMany, hallId, hallCode, cinemaId, hallRowCount, hallColCount, isAvailable, createdAt, modifiedAt)
                .from(hall);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.741+05:30", comments="Source Table: TBL_Hall")
    default QueryExpressionDSL<MyBatis3SelectModelAdapter<List<Hall>>> selectDistinctByExample() {
        return SelectDSL.selectDistinctWithMapper(this::selectMany, hallId, hallCode, cinemaId, hallRowCount, hallColCount, isAvailable, createdAt, modifiedAt)
                .from(hall);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.741+05:30", comments="Source Table: TBL_Hall")
    default Hall selectByPrimaryKey(Integer hallId_) {
        return SelectDSL.selectWithMapper(this::selectOne, hallId, hallCode, cinemaId, hallRowCount, hallColCount, isAvailable, createdAt, modifiedAt)
                .from(hall)
                .where(hallId, isEqualTo(hallId_))
                .build()
                .execute();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.741+05:30", comments="Source Table: TBL_Hall")
    default UpdateDSL<MyBatis3UpdateModelAdapter<Integer>> updateByExample(Hall record) {
        return UpdateDSL.updateWithMapper(this::update, hall)
                .set(hallCode).equalTo(record::getHallCode)
                .set(cinemaId).equalTo(record::getCinemaId)
                .set(hallRowCount).equalTo(record::getHallRowCount)
                .set(hallColCount).equalTo(record::getHallColCount)
                .set(isAvailable).equalTo(record::getIsAvailable)
                .set(createdAt).equalTo(record::getCreatedAt)
                .set(modifiedAt).equalTo(record::getModifiedAt);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.742+05:30", comments="Source Table: TBL_Hall")
    default UpdateDSL<MyBatis3UpdateModelAdapter<Integer>> updateByExampleSelective(Hall record) {
        return UpdateDSL.updateWithMapper(this::update, hall)
                .set(hallCode).equalToWhenPresent(record::getHallCode)
                .set(cinemaId).equalToWhenPresent(record::getCinemaId)
                .set(hallRowCount).equalToWhenPresent(record::getHallRowCount)
                .set(hallColCount).equalToWhenPresent(record::getHallColCount)
                .set(isAvailable).equalToWhenPresent(record::getIsAvailable)
                .set(createdAt).equalToWhenPresent(record::getCreatedAt)
                .set(modifiedAt).equalToWhenPresent(record::getModifiedAt);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.742+05:30", comments="Source Table: TBL_Hall")
    default int updateByPrimaryKey(Hall record) {
        return UpdateDSL.updateWithMapper(this::update, hall)
                .set(hallCode).equalTo(record::getHallCode)
                .set(cinemaId).equalTo(record::getCinemaId)
                .set(hallRowCount).equalTo(record::getHallRowCount)
                .set(hallColCount).equalTo(record::getHallColCount)
                .set(isAvailable).equalTo(record::getIsAvailable)
                .set(createdAt).equalTo(record::getCreatedAt)
                .set(modifiedAt).equalTo(record::getModifiedAt)
                .where(hallId, isEqualTo(record::getHallId))
                .build()
                .execute();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.742+05:30", comments="Source Table: TBL_Hall")
    default int updateByPrimaryKeySelective(Hall record) {
        return UpdateDSL.updateWithMapper(this::update, hall)
                .set(hallCode).equalToWhenPresent(record::getHallCode)
                .set(cinemaId).equalToWhenPresent(record::getCinemaId)
                .set(hallRowCount).equalToWhenPresent(record::getHallRowCount)
                .set(hallColCount).equalToWhenPresent(record::getHallColCount)
                .set(isAvailable).equalToWhenPresent(record::getIsAvailable)
                .set(createdAt).equalToWhenPresent(record::getCreatedAt)
                .set(modifiedAt).equalToWhenPresent(record::getModifiedAt)
                .where(hallId, isEqualTo(record::getHallId))
                .build()
                .execute();
    }
}