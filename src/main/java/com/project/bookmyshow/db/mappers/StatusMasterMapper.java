package com.project.bookmyshow.db.mappers;

import static com.project.bookmyshow.db.mappers.StatusMasterDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import com.project.bookmyshow.db.mappers.StatusMaster;
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
public interface StatusMasterMapper {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.785+05:30", comments="Source Table: TBL_StatusMaster")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    long count(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.785+05:30", comments="Source Table: TBL_StatusMaster")
    @DeleteProvider(type=SqlProviderAdapter.class, method="delete")
    int delete(DeleteStatementProvider deleteStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.785+05:30", comments="Source Table: TBL_StatusMaster")
    @InsertProvider(type=SqlProviderAdapter.class, method="insert")
    @SelectKey(statement="SELECT LAST_INSERT_ID()", keyProperty="record.statusId", before=false, resultType=Integer.class)
    int insert(InsertStatementProvider<StatusMaster> insertStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.785+05:30", comments="Source Table: TBL_StatusMaster")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("StatusMasterResult")
    StatusMaster selectOne(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.785+05:30", comments="Source Table: TBL_StatusMaster")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Results(id="StatusMasterResult", value = {
        @Result(column="status_id", property="statusId", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="status", property="status", jdbcType=JdbcType.VARCHAR)
    })
    List<StatusMaster> selectMany(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.785+05:30", comments="Source Table: TBL_StatusMaster")
    @UpdateProvider(type=SqlProviderAdapter.class, method="update")
    int update(UpdateStatementProvider updateStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.785+05:30", comments="Source Table: TBL_StatusMaster")
    default QueryExpressionDSL<MyBatis3SelectModelAdapter<Long>> countByExample() {
        return SelectDSL.selectWithMapper(this::count, SqlBuilder.count())
                .from(statusMaster);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.785+05:30", comments="Source Table: TBL_StatusMaster")
    default DeleteDSL<MyBatis3DeleteModelAdapter<Integer>> deleteByExample() {
        return DeleteDSL.deleteFromWithMapper(this::delete, statusMaster);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.785+05:30", comments="Source Table: TBL_StatusMaster")
    default int deleteByPrimaryKey(Integer statusId_) {
        return DeleteDSL.deleteFromWithMapper(this::delete, statusMaster)
                .where(statusId, isEqualTo(statusId_))
                .build()
                .execute();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.786+05:30", comments="Source Table: TBL_StatusMaster")
    default int insert(StatusMaster record) {
        return insert(SqlBuilder.insert(record)
                .into(statusMaster)
                .map(status).toProperty("status")
                .build()
                .render(RenderingStrategy.MYBATIS3));
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.786+05:30", comments="Source Table: TBL_StatusMaster")
    default int insertSelective(StatusMaster record) {
        return insert(SqlBuilder.insert(record)
                .into(statusMaster)
                .map(status).toPropertyWhenPresent("status", record::getStatus)
                .build()
                .render(RenderingStrategy.MYBATIS3));
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.786+05:30", comments="Source Table: TBL_StatusMaster")
    default QueryExpressionDSL<MyBatis3SelectModelAdapter<List<StatusMaster>>> selectByExample() {
        return SelectDSL.selectWithMapper(this::selectMany, statusId, status)
                .from(statusMaster);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.786+05:30", comments="Source Table: TBL_StatusMaster")
    default QueryExpressionDSL<MyBatis3SelectModelAdapter<List<StatusMaster>>> selectDistinctByExample() {
        return SelectDSL.selectDistinctWithMapper(this::selectMany, statusId, status)
                .from(statusMaster);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.786+05:30", comments="Source Table: TBL_StatusMaster")
    default StatusMaster selectByPrimaryKey(Integer statusId_) {
        return SelectDSL.selectWithMapper(this::selectOne, statusId, status)
                .from(statusMaster)
                .where(statusId, isEqualTo(statusId_))
                .build()
                .execute();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.786+05:30", comments="Source Table: TBL_StatusMaster")
    default UpdateDSL<MyBatis3UpdateModelAdapter<Integer>> updateByExample(StatusMaster record) {
        return UpdateDSL.updateWithMapper(this::update, statusMaster)
                .set(status).equalTo(record::getStatus);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.786+05:30", comments="Source Table: TBL_StatusMaster")
    default UpdateDSL<MyBatis3UpdateModelAdapter<Integer>> updateByExampleSelective(StatusMaster record) {
        return UpdateDSL.updateWithMapper(this::update, statusMaster)
                .set(status).equalToWhenPresent(record::getStatus);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.786+05:30", comments="Source Table: TBL_StatusMaster")
    default int updateByPrimaryKey(StatusMaster record) {
        return UpdateDSL.updateWithMapper(this::update, statusMaster)
                .set(status).equalTo(record::getStatus)
                .where(statusId, isEqualTo(record::getStatusId))
                .build()
                .execute();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.787+05:30", comments="Source Table: TBL_StatusMaster")
    default int updateByPrimaryKeySelective(StatusMaster record) {
        return UpdateDSL.updateWithMapper(this::update, statusMaster)
                .set(status).equalToWhenPresent(record::getStatus)
                .where(statusId, isEqualTo(record::getStatusId))
                .build()
                .execute();
    }
}