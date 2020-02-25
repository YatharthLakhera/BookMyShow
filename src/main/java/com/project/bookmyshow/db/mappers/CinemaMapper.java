package com.project.bookmyshow.db.mappers;

import static com.project.bookmyshow.db.mappers.CinemaDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import com.project.bookmyshow.db.mappers.Cinema;
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
public interface CinemaMapper {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.733+05:30", comments="Source Table: TBL_Cinema")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    long count(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.733+05:30", comments="Source Table: TBL_Cinema")
    @DeleteProvider(type=SqlProviderAdapter.class, method="delete")
    int delete(DeleteStatementProvider deleteStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.733+05:30", comments="Source Table: TBL_Cinema")
    @InsertProvider(type=SqlProviderAdapter.class, method="insert")
    @SelectKey(statement="SELECT LAST_INSERT_ID()", keyProperty="record.cinemaId", before=false, resultType=Integer.class)
    int insert(InsertStatementProvider<Cinema> insertStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.733+05:30", comments="Source Table: TBL_Cinema")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("CinemaResult")
    Cinema selectOne(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.733+05:30", comments="Source Table: TBL_Cinema")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Results(id="CinemaResult", value = {
        @Result(column="cinema_id", property="cinemaId", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="cinema_name", property="cinemaName", jdbcType=JdbcType.VARCHAR),
        @Result(column="created_at", property="createdAt", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="modified_at", property="modifiedAt", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="is_open", property="isOpen", jdbcType=JdbcType.BIT),
        @Result(column="extraData", property="extradata", jdbcType=JdbcType.LONGVARCHAR)
    })
    List<Cinema> selectMany(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.733+05:30", comments="Source Table: TBL_Cinema")
    @UpdateProvider(type=SqlProviderAdapter.class, method="update")
    int update(UpdateStatementProvider updateStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.733+05:30", comments="Source Table: TBL_Cinema")
    default QueryExpressionDSL<MyBatis3SelectModelAdapter<Long>> countByExample() {
        return SelectDSL.selectWithMapper(this::count, SqlBuilder.count())
                .from(cinema);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.733+05:30", comments="Source Table: TBL_Cinema")
    default DeleteDSL<MyBatis3DeleteModelAdapter<Integer>> deleteByExample() {
        return DeleteDSL.deleteFromWithMapper(this::delete, cinema);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.733+05:30", comments="Source Table: TBL_Cinema")
    default int deleteByPrimaryKey(Integer cinemaId_) {
        return DeleteDSL.deleteFromWithMapper(this::delete, cinema)
                .where(cinemaId, isEqualTo(cinemaId_))
                .build()
                .execute();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.734+05:30", comments="Source Table: TBL_Cinema")
    default int insert(Cinema record) {
        return insert(SqlBuilder.insert(record)
                .into(cinema)
                .map(cinemaName).toProperty("cinemaName")
                .map(createdAt).toProperty("createdAt")
                .map(modifiedAt).toProperty("modifiedAt")
                .map(isOpen).toProperty("isOpen")
                .map(extradata).toProperty("extradata")
                .build()
                .render(RenderingStrategy.MYBATIS3));
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.734+05:30", comments="Source Table: TBL_Cinema")
    default int insertSelective(Cinema record) {
        return insert(SqlBuilder.insert(record)
                .into(cinema)
                .map(cinemaName).toPropertyWhenPresent("cinemaName", record::getCinemaName)
                .map(createdAt).toPropertyWhenPresent("createdAt", record::getCreatedAt)
                .map(modifiedAt).toPropertyWhenPresent("modifiedAt", record::getModifiedAt)
                .map(isOpen).toPropertyWhenPresent("isOpen", record::getIsOpen)
                .map(extradata).toPropertyWhenPresent("extradata", record::getExtradata)
                .build()
                .render(RenderingStrategy.MYBATIS3));
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.734+05:30", comments="Source Table: TBL_Cinema")
    default QueryExpressionDSL<MyBatis3SelectModelAdapter<List<Cinema>>> selectByExample() {
        return SelectDSL.selectWithMapper(this::selectMany, cinemaId, cinemaName, createdAt, modifiedAt, isOpen, extradata)
                .from(cinema);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.734+05:30", comments="Source Table: TBL_Cinema")
    default QueryExpressionDSL<MyBatis3SelectModelAdapter<List<Cinema>>> selectDistinctByExample() {
        return SelectDSL.selectDistinctWithMapper(this::selectMany, cinemaId, cinemaName, createdAt, modifiedAt, isOpen, extradata)
                .from(cinema);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.734+05:30", comments="Source Table: TBL_Cinema")
    default Cinema selectByPrimaryKey(Integer cinemaId_) {
        return SelectDSL.selectWithMapper(this::selectOne, cinemaId, cinemaName, createdAt, modifiedAt, isOpen, extradata)
                .from(cinema)
                .where(cinemaId, isEqualTo(cinemaId_))
                .build()
                .execute();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.734+05:30", comments="Source Table: TBL_Cinema")
    default UpdateDSL<MyBatis3UpdateModelAdapter<Integer>> updateByExample(Cinema record) {
        return UpdateDSL.updateWithMapper(this::update, cinema)
                .set(cinemaName).equalTo(record::getCinemaName)
                .set(createdAt).equalTo(record::getCreatedAt)
                .set(modifiedAt).equalTo(record::getModifiedAt)
                .set(isOpen).equalTo(record::getIsOpen)
                .set(extradata).equalTo(record::getExtradata);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.734+05:30", comments="Source Table: TBL_Cinema")
    default UpdateDSL<MyBatis3UpdateModelAdapter<Integer>> updateByExampleSelective(Cinema record) {
        return UpdateDSL.updateWithMapper(this::update, cinema)
                .set(cinemaName).equalToWhenPresent(record::getCinemaName)
                .set(createdAt).equalToWhenPresent(record::getCreatedAt)
                .set(modifiedAt).equalToWhenPresent(record::getModifiedAt)
                .set(isOpen).equalToWhenPresent(record::getIsOpen)
                .set(extradata).equalToWhenPresent(record::getExtradata);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.734+05:30", comments="Source Table: TBL_Cinema")
    default int updateByPrimaryKey(Cinema record) {
        return UpdateDSL.updateWithMapper(this::update, cinema)
                .set(cinemaName).equalTo(record::getCinemaName)
                .set(createdAt).equalTo(record::getCreatedAt)
                .set(modifiedAt).equalTo(record::getModifiedAt)
                .set(isOpen).equalTo(record::getIsOpen)
                .set(extradata).equalTo(record::getExtradata)
                .where(cinemaId, isEqualTo(record::getCinemaId))
                .build()
                .execute();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.735+05:30", comments="Source Table: TBL_Cinema")
    default int updateByPrimaryKeySelective(Cinema record) {
        return UpdateDSL.updateWithMapper(this::update, cinema)
                .set(cinemaName).equalToWhenPresent(record::getCinemaName)
                .set(createdAt).equalToWhenPresent(record::getCreatedAt)
                .set(modifiedAt).equalToWhenPresent(record::getModifiedAt)
                .set(isOpen).equalToWhenPresent(record::getIsOpen)
                .set(extradata).equalToWhenPresent(record::getExtradata)
                .where(cinemaId, isEqualTo(record::getCinemaId))
                .build()
                .execute();
    }
}