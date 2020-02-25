package com.project.bookmyshow.db.mappers;

import static com.project.bookmyshow.db.mappers.ScheduledLiveShowDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import com.project.bookmyshow.db.mappers.ScheduledLiveShow;
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
public interface ScheduledLiveShowMapper {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.752+05:30", comments="Source Table: TBL_ScheduledLiveShow")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    long count(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.752+05:30", comments="Source Table: TBL_ScheduledLiveShow")
    @DeleteProvider(type=SqlProviderAdapter.class, method="delete")
    int delete(DeleteStatementProvider deleteStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.752+05:30", comments="Source Table: TBL_ScheduledLiveShow")
    @InsertProvider(type=SqlProviderAdapter.class, method="insert")
    @SelectKey(statement="SELECT LAST_INSERT_ID()", keyProperty="record.scheduledLiveShowId", before=false, resultType=Integer.class)
    int insert(InsertStatementProvider<ScheduledLiveShow> insertStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.752+05:30", comments="Source Table: TBL_ScheduledLiveShow")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("ScheduledLiveShowResult")
    ScheduledLiveShow selectOne(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.752+05:30", comments="Source Table: TBL_ScheduledLiveShow")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Results(id="ScheduledLiveShowResult", value = {
        @Result(column="scheduled_live_show_id", property="scheduledLiveShowId", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="live_show_id", property="liveShowId", jdbcType=JdbcType.INTEGER),
        @Result(column="show_start_time", property="showStartTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="show_end_time", property="showEndTime", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="tickets_price", property="ticketsPrice", typeHandler=AmountTypeHandler.class, jdbcType=JdbcType.INTEGER),
        @Result(column="created_at", property="createdAt", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="modified_at", property="modifiedAt", jdbcType=JdbcType.TIMESTAMP)
    })
    List<ScheduledLiveShow> selectMany(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.753+05:30", comments="Source Table: TBL_ScheduledLiveShow")
    @UpdateProvider(type=SqlProviderAdapter.class, method="update")
    int update(UpdateStatementProvider updateStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.753+05:30", comments="Source Table: TBL_ScheduledLiveShow")
    default QueryExpressionDSL<MyBatis3SelectModelAdapter<Long>> countByExample() {
        return SelectDSL.selectWithMapper(this::count, SqlBuilder.count())
                .from(scheduledLiveShow);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.753+05:30", comments="Source Table: TBL_ScheduledLiveShow")
    default DeleteDSL<MyBatis3DeleteModelAdapter<Integer>> deleteByExample() {
        return DeleteDSL.deleteFromWithMapper(this::delete, scheduledLiveShow);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.753+05:30", comments="Source Table: TBL_ScheduledLiveShow")
    default int deleteByPrimaryKey(Integer scheduledLiveShowId_) {
        return DeleteDSL.deleteFromWithMapper(this::delete, scheduledLiveShow)
                .where(scheduledLiveShowId, isEqualTo(scheduledLiveShowId_))
                .build()
                .execute();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.753+05:30", comments="Source Table: TBL_ScheduledLiveShow")
    default int insert(ScheduledLiveShow record) {
        return insert(SqlBuilder.insert(record)
                .into(scheduledLiveShow)
                .map(liveShowId).toProperty("liveShowId")
                .map(showStartTime).toProperty("showStartTime")
                .map(showEndTime).toProperty("showEndTime")
                .map(ticketsPrice).toProperty("ticketsPrice")
                .map(createdAt).toProperty("createdAt")
                .map(modifiedAt).toProperty("modifiedAt")
                .build()
                .render(RenderingStrategy.MYBATIS3));
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.753+05:30", comments="Source Table: TBL_ScheduledLiveShow")
    default int insertSelective(ScheduledLiveShow record) {
        return insert(SqlBuilder.insert(record)
                .into(scheduledLiveShow)
                .map(liveShowId).toPropertyWhenPresent("liveShowId", record::getLiveShowId)
                .map(showStartTime).toPropertyWhenPresent("showStartTime", record::getShowStartTime)
                .map(showEndTime).toPropertyWhenPresent("showEndTime", record::getShowEndTime)
                .map(ticketsPrice).toPropertyWhenPresent("ticketsPrice", record::getTicketsPrice)
                .map(createdAt).toPropertyWhenPresent("createdAt", record::getCreatedAt)
                .map(modifiedAt).toPropertyWhenPresent("modifiedAt", record::getModifiedAt)
                .build()
                .render(RenderingStrategy.MYBATIS3));
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.754+05:30", comments="Source Table: TBL_ScheduledLiveShow")
    default QueryExpressionDSL<MyBatis3SelectModelAdapter<List<ScheduledLiveShow>>> selectByExample() {
        return SelectDSL.selectWithMapper(this::selectMany, scheduledLiveShowId, liveShowId, showStartTime, showEndTime, ticketsPrice, createdAt, modifiedAt)
                .from(scheduledLiveShow);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.754+05:30", comments="Source Table: TBL_ScheduledLiveShow")
    default QueryExpressionDSL<MyBatis3SelectModelAdapter<List<ScheduledLiveShow>>> selectDistinctByExample() {
        return SelectDSL.selectDistinctWithMapper(this::selectMany, scheduledLiveShowId, liveShowId, showStartTime, showEndTime, ticketsPrice, createdAt, modifiedAt)
                .from(scheduledLiveShow);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.754+05:30", comments="Source Table: TBL_ScheduledLiveShow")
    default ScheduledLiveShow selectByPrimaryKey(Integer scheduledLiveShowId_) {
        return SelectDSL.selectWithMapper(this::selectOne, scheduledLiveShowId, liveShowId, showStartTime, showEndTime, ticketsPrice, createdAt, modifiedAt)
                .from(scheduledLiveShow)
                .where(scheduledLiveShowId, isEqualTo(scheduledLiveShowId_))
                .build()
                .execute();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.754+05:30", comments="Source Table: TBL_ScheduledLiveShow")
    default UpdateDSL<MyBatis3UpdateModelAdapter<Integer>> updateByExample(ScheduledLiveShow record) {
        return UpdateDSL.updateWithMapper(this::update, scheduledLiveShow)
                .set(liveShowId).equalTo(record::getLiveShowId)
                .set(showStartTime).equalTo(record::getShowStartTime)
                .set(showEndTime).equalTo(record::getShowEndTime)
                .set(ticketsPrice).equalTo(record::getTicketsPrice)
                .set(createdAt).equalTo(record::getCreatedAt)
                .set(modifiedAt).equalTo(record::getModifiedAt);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.754+05:30", comments="Source Table: TBL_ScheduledLiveShow")
    default UpdateDSL<MyBatis3UpdateModelAdapter<Integer>> updateByExampleSelective(ScheduledLiveShow record) {
        return UpdateDSL.updateWithMapper(this::update, scheduledLiveShow)
                .set(liveShowId).equalToWhenPresent(record::getLiveShowId)
                .set(showStartTime).equalToWhenPresent(record::getShowStartTime)
                .set(showEndTime).equalToWhenPresent(record::getShowEndTime)
                .set(ticketsPrice).equalToWhenPresent(record::getTicketsPrice)
                .set(createdAt).equalToWhenPresent(record::getCreatedAt)
                .set(modifiedAt).equalToWhenPresent(record::getModifiedAt);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.754+05:30", comments="Source Table: TBL_ScheduledLiveShow")
    default int updateByPrimaryKey(ScheduledLiveShow record) {
        return UpdateDSL.updateWithMapper(this::update, scheduledLiveShow)
                .set(liveShowId).equalTo(record::getLiveShowId)
                .set(showStartTime).equalTo(record::getShowStartTime)
                .set(showEndTime).equalTo(record::getShowEndTime)
                .set(ticketsPrice).equalTo(record::getTicketsPrice)
                .set(createdAt).equalTo(record::getCreatedAt)
                .set(modifiedAt).equalTo(record::getModifiedAt)
                .where(scheduledLiveShowId, isEqualTo(record::getScheduledLiveShowId))
                .build()
                .execute();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.754+05:30", comments="Source Table: TBL_ScheduledLiveShow")
    default int updateByPrimaryKeySelective(ScheduledLiveShow record) {
        return UpdateDSL.updateWithMapper(this::update, scheduledLiveShow)
                .set(liveShowId).equalToWhenPresent(record::getLiveShowId)
                .set(showStartTime).equalToWhenPresent(record::getShowStartTime)
                .set(showEndTime).equalToWhenPresent(record::getShowEndTime)
                .set(ticketsPrice).equalToWhenPresent(record::getTicketsPrice)
                .set(createdAt).equalToWhenPresent(record::getCreatedAt)
                .set(modifiedAt).equalToWhenPresent(record::getModifiedAt)
                .where(scheduledLiveShowId, isEqualTo(record::getScheduledLiveShowId))
                .build()
                .execute();
    }
}