package com.project.bookmyshow.db.mappers;

import static com.project.bookmyshow.db.mappers.LiveShowDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import com.project.bookmyshow.db.mappers.LiveShow;
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
public interface LiveShowMapper {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.745+05:30", comments="Source Table: TBL_LiveShow")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    long count(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.745+05:30", comments="Source Table: TBL_LiveShow")
    @DeleteProvider(type=SqlProviderAdapter.class, method="delete")
    int delete(DeleteStatementProvider deleteStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.745+05:30", comments="Source Table: TBL_LiveShow")
    @InsertProvider(type=SqlProviderAdapter.class, method="insert")
    @SelectKey(statement="SELECT LAST_INSERT_ID()", keyProperty="record.liveShowId", before=false, resultType=Integer.class)
    int insert(InsertStatementProvider<LiveShow> insertStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.746+05:30", comments="Source Table: TBL_LiveShow")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("LiveShowResult")
    LiveShow selectOne(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.746+05:30", comments="Source Table: TBL_LiveShow")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Results(id="LiveShowResult", value = {
        @Result(column="live_show_id", property="liveShowId", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="show_id", property="showId", jdbcType=JdbcType.INTEGER),
        @Result(column="hall_id", property="hallId", jdbcType=JdbcType.INTEGER),
        @Result(column="created_at", property="createdAt", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="modified_at", property="modifiedAt", jdbcType=JdbcType.TIMESTAMP)
    })
    List<LiveShow> selectMany(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.746+05:30", comments="Source Table: TBL_LiveShow")
    @UpdateProvider(type=SqlProviderAdapter.class, method="update")
    int update(UpdateStatementProvider updateStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.746+05:30", comments="Source Table: TBL_LiveShow")
    default QueryExpressionDSL<MyBatis3SelectModelAdapter<Long>> countByExample() {
        return SelectDSL.selectWithMapper(this::count, SqlBuilder.count())
                .from(liveShow);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.746+05:30", comments="Source Table: TBL_LiveShow")
    default DeleteDSL<MyBatis3DeleteModelAdapter<Integer>> deleteByExample() {
        return DeleteDSL.deleteFromWithMapper(this::delete, liveShow);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.746+05:30", comments="Source Table: TBL_LiveShow")
    default int deleteByPrimaryKey(Integer liveShowId_) {
        return DeleteDSL.deleteFromWithMapper(this::delete, liveShow)
                .where(liveShowId, isEqualTo(liveShowId_))
                .build()
                .execute();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.746+05:30", comments="Source Table: TBL_LiveShow")
    default int insert(LiveShow record) {
        return insert(SqlBuilder.insert(record)
                .into(liveShow)
                .map(showId).toProperty("showId")
                .map(hallId).toProperty("hallId")
                .map(createdAt).toProperty("createdAt")
                .map(modifiedAt).toProperty("modifiedAt")
                .build()
                .render(RenderingStrategy.MYBATIS3));
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.747+05:30", comments="Source Table: TBL_LiveShow")
    default int insertSelective(LiveShow record) {
        return insert(SqlBuilder.insert(record)
                .into(liveShow)
                .map(showId).toPropertyWhenPresent("showId", record::getShowId)
                .map(hallId).toPropertyWhenPresent("hallId", record::getHallId)
                .map(createdAt).toPropertyWhenPresent("createdAt", record::getCreatedAt)
                .map(modifiedAt).toPropertyWhenPresent("modifiedAt", record::getModifiedAt)
                .build()
                .render(RenderingStrategy.MYBATIS3));
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.747+05:30", comments="Source Table: TBL_LiveShow")
    default QueryExpressionDSL<MyBatis3SelectModelAdapter<List<LiveShow>>> selectByExample() {
        return SelectDSL.selectWithMapper(this::selectMany, liveShowId, showId, hallId, createdAt, modifiedAt)
                .from(liveShow);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.747+05:30", comments="Source Table: TBL_LiveShow")
    default QueryExpressionDSL<MyBatis3SelectModelAdapter<List<LiveShow>>> selectDistinctByExample() {
        return SelectDSL.selectDistinctWithMapper(this::selectMany, liveShowId, showId, hallId, createdAt, modifiedAt)
                .from(liveShow);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.747+05:30", comments="Source Table: TBL_LiveShow")
    default LiveShow selectByPrimaryKey(Integer liveShowId_) {
        return SelectDSL.selectWithMapper(this::selectOne, liveShowId, showId, hallId, createdAt, modifiedAt)
                .from(liveShow)
                .where(liveShowId, isEqualTo(liveShowId_))
                .build()
                .execute();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.747+05:30", comments="Source Table: TBL_LiveShow")
    default UpdateDSL<MyBatis3UpdateModelAdapter<Integer>> updateByExample(LiveShow record) {
        return UpdateDSL.updateWithMapper(this::update, liveShow)
                .set(showId).equalTo(record::getShowId)
                .set(hallId).equalTo(record::getHallId)
                .set(createdAt).equalTo(record::getCreatedAt)
                .set(modifiedAt).equalTo(record::getModifiedAt);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.747+05:30", comments="Source Table: TBL_LiveShow")
    default UpdateDSL<MyBatis3UpdateModelAdapter<Integer>> updateByExampleSelective(LiveShow record) {
        return UpdateDSL.updateWithMapper(this::update, liveShow)
                .set(showId).equalToWhenPresent(record::getShowId)
                .set(hallId).equalToWhenPresent(record::getHallId)
                .set(createdAt).equalToWhenPresent(record::getCreatedAt)
                .set(modifiedAt).equalToWhenPresent(record::getModifiedAt);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.748+05:30", comments="Source Table: TBL_LiveShow")
    default int updateByPrimaryKey(LiveShow record) {
        return UpdateDSL.updateWithMapper(this::update, liveShow)
                .set(showId).equalTo(record::getShowId)
                .set(hallId).equalTo(record::getHallId)
                .set(createdAt).equalTo(record::getCreatedAt)
                .set(modifiedAt).equalTo(record::getModifiedAt)
                .where(liveShowId, isEqualTo(record::getLiveShowId))
                .build()
                .execute();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.748+05:30", comments="Source Table: TBL_LiveShow")
    default int updateByPrimaryKeySelective(LiveShow record) {
        return UpdateDSL.updateWithMapper(this::update, liveShow)
                .set(showId).equalToWhenPresent(record::getShowId)
                .set(hallId).equalToWhenPresent(record::getHallId)
                .set(createdAt).equalToWhenPresent(record::getCreatedAt)
                .set(modifiedAt).equalToWhenPresent(record::getModifiedAt)
                .where(liveShowId, isEqualTo(record::getLiveShowId))
                .build()
                .execute();
    }
}