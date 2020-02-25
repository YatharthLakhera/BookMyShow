package com.project.bookmyshow.db.mappers;

import com.project.bookmyshow.enums.ShowType;
import java.sql.JDBCType;
import java.util.Date;
import javax.annotation.Generated;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public final class ShowDynamicSqlSupport {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.769+05:30", comments="Source Table: TBL_Show")
    public static final Show show = new Show();

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.77+05:30", comments="Source field: TBL_Show.show_id")
    public static final SqlColumn<Integer> showId = show.showId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.77+05:30", comments="Source field: TBL_Show.show_type")
    public static final SqlColumn<ShowType> showType = show.showType;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.77+05:30", comments="Source field: TBL_Show.show_name")
    public static final SqlColumn<String> showName = show.showName;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.77+05:30", comments="Source field: TBL_Show.created_at")
    public static final SqlColumn<Date> createdAt = show.createdAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.77+05:30", comments="Source field: TBL_Show.modified_at")
    public static final SqlColumn<Date> modifiedAt = show.modifiedAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.77+05:30", comments="Source field: TBL_Show.show_details")
    public static final SqlColumn<String> showDetails = show.showDetails;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.77+05:30", comments="Source Table: TBL_Show")
    public static final class Show extends SqlTable {
        public final SqlColumn<Integer> showId = column("show_id", JDBCType.INTEGER);

        public final SqlColumn<ShowType> showType = column("show_type", JDBCType.VARCHAR);

        public final SqlColumn<String> showName = column("show_name", JDBCType.VARCHAR);

        public final SqlColumn<Date> createdAt = column("created_at", JDBCType.TIMESTAMP);

        public final SqlColumn<Date> modifiedAt = column("modified_at", JDBCType.TIMESTAMP);

        public final SqlColumn<String> showDetails = column("show_details", JDBCType.LONGVARCHAR);

        public Show() {
            super("TBL_Show");
        }
    }
}