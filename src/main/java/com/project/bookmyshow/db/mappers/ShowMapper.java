package com.project.bookmyshow.db.mappers;

import static com.project.bookmyshow.db.mappers.ShowDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

import com.project.bookmyshow.db.mappers.Show;
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
public interface ShowMapper {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.771+05:30", comments="Source Table: TBL_Show")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    long count(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.771+05:30", comments="Source Table: TBL_Show")
    @DeleteProvider(type=SqlProviderAdapter.class, method="delete")
    int delete(DeleteStatementProvider deleteStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.771+05:30", comments="Source Table: TBL_Show")
    @InsertProvider(type=SqlProviderAdapter.class, method="insert")
    @SelectKey(statement="SELECT LAST_INSERT_ID()", keyProperty="record.showId", before=false, resultType=Integer.class)
    int insert(InsertStatementProvider<Show> insertStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.771+05:30", comments="Source Table: TBL_Show")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @ResultMap("ShowResult")
    Show selectOne(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.771+05:30", comments="Source Table: TBL_Show")
    @SelectProvider(type=SqlProviderAdapter.class, method="select")
    @Results(id="ShowResult", value = {
        @Result(column="show_id", property="showId", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="show_type", property="showType", jdbcType=JdbcType.VARCHAR),
        @Result(column="show_name", property="showName", jdbcType=JdbcType.VARCHAR),
        @Result(column="created_at", property="createdAt", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="modified_at", property="modifiedAt", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="show_details", property="showDetails", jdbcType=JdbcType.LONGVARCHAR)
    })
    List<Show> selectMany(SelectStatementProvider selectStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.771+05:30", comments="Source Table: TBL_Show")
    @UpdateProvider(type=SqlProviderAdapter.class, method="update")
    int update(UpdateStatementProvider updateStatement);

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.771+05:30", comments="Source Table: TBL_Show")
    default QueryExpressionDSL<MyBatis3SelectModelAdapter<Long>> countByExample() {
        return SelectDSL.selectWithMapper(this::count, SqlBuilder.count())
                .from(show);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.771+05:30", comments="Source Table: TBL_Show")
    default DeleteDSL<MyBatis3DeleteModelAdapter<Integer>> deleteByExample() {
        return DeleteDSL.deleteFromWithMapper(this::delete, show);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.771+05:30", comments="Source Table: TBL_Show")
    default int deleteByPrimaryKey(Integer showId_) {
        return DeleteDSL.deleteFromWithMapper(this::delete, show)
                .where(showId, isEqualTo(showId_))
                .build()
                .execute();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.771+05:30", comments="Source Table: TBL_Show")
    default int insert(Show record) {
        return insert(SqlBuilder.insert(record)
                .into(show)
                .map(showType).toProperty("showType")
                .map(showName).toProperty("showName")
                .map(createdAt).toProperty("createdAt")
                .map(modifiedAt).toProperty("modifiedAt")
                .map(showDetails).toProperty("showDetails")
                .build()
                .render(RenderingStrategy.MYBATIS3));
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.772+05:30", comments="Source Table: TBL_Show")
    default int insertSelective(Show record) {
        return insert(SqlBuilder.insert(record)
                .into(show)
                .map(showType).toPropertyWhenPresent("showType", record::getShowType)
                .map(showName).toPropertyWhenPresent("showName", record::getShowName)
                .map(createdAt).toPropertyWhenPresent("createdAt", record::getCreatedAt)
                .map(modifiedAt).toPropertyWhenPresent("modifiedAt", record::getModifiedAt)
                .map(showDetails).toPropertyWhenPresent("showDetails", record::getShowDetails)
                .build()
                .render(RenderingStrategy.MYBATIS3));
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.772+05:30", comments="Source Table: TBL_Show")
    default QueryExpressionDSL<MyBatis3SelectModelAdapter<List<Show>>> selectByExample() {
        return SelectDSL.selectWithMapper(this::selectMany, showId, showType, showName, createdAt, modifiedAt, showDetails)
                .from(show);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.772+05:30", comments="Source Table: TBL_Show")
    default QueryExpressionDSL<MyBatis3SelectModelAdapter<List<Show>>> selectDistinctByExample() {
        return SelectDSL.selectDistinctWithMapper(this::selectMany, showId, showType, showName, createdAt, modifiedAt, showDetails)
                .from(show);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.772+05:30", comments="Source Table: TBL_Show")
    default Show selectByPrimaryKey(Integer showId_) {
        return SelectDSL.selectWithMapper(this::selectOne, showId, showType, showName, createdAt, modifiedAt, showDetails)
                .from(show)
                .where(showId, isEqualTo(showId_))
                .build()
                .execute();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.772+05:30", comments="Source Table: TBL_Show")
    default UpdateDSL<MyBatis3UpdateModelAdapter<Integer>> updateByExample(Show record) {
        return UpdateDSL.updateWithMapper(this::update, show)
                .set(showType).equalTo(record::getShowType)
                .set(showName).equalTo(record::getShowName)
                .set(createdAt).equalTo(record::getCreatedAt)
                .set(modifiedAt).equalTo(record::getModifiedAt)
                .set(showDetails).equalTo(record::getShowDetails);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.772+05:30", comments="Source Table: TBL_Show")
    default UpdateDSL<MyBatis3UpdateModelAdapter<Integer>> updateByExampleSelective(Show record) {
        return UpdateDSL.updateWithMapper(this::update, show)
                .set(showType).equalToWhenPresent(record::getShowType)
                .set(showName).equalToWhenPresent(record::getShowName)
                .set(createdAt).equalToWhenPresent(record::getCreatedAt)
                .set(modifiedAt).equalToWhenPresent(record::getModifiedAt)
                .set(showDetails).equalToWhenPresent(record::getShowDetails);
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.772+05:30", comments="Source Table: TBL_Show")
    default int updateByPrimaryKey(Show record) {
        return UpdateDSL.updateWithMapper(this::update, show)
                .set(showType).equalTo(record::getShowType)
                .set(showName).equalTo(record::getShowName)
                .set(createdAt).equalTo(record::getCreatedAt)
                .set(modifiedAt).equalTo(record::getModifiedAt)
                .set(showDetails).equalTo(record::getShowDetails)
                .where(showId, isEqualTo(record::getShowId))
                .build()
                .execute();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.772+05:30", comments="Source Table: TBL_Show")
    default int updateByPrimaryKeySelective(Show record) {
        return UpdateDSL.updateWithMapper(this::update, show)
                .set(showType).equalToWhenPresent(record::getShowType)
                .set(showName).equalToWhenPresent(record::getShowName)
                .set(createdAt).equalToWhenPresent(record::getCreatedAt)
                .set(modifiedAt).equalToWhenPresent(record::getModifiedAt)
                .set(showDetails).equalToWhenPresent(record::getShowDetails)
                .where(showId, isEqualTo(record::getShowId))
                .build()
                .execute();
    }
}