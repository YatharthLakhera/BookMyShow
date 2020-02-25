package com.project.bookmyshow.db.mappers;

import java.sql.JDBCType;
import java.util.Date;
import javax.annotation.Generated;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public final class HallDynamicSqlSupport {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.738+05:30", comments="Source Table: TBL_Hall")
    public static final Hall hall = new Hall();

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.738+05:30", comments="Source field: TBL_Hall.hall_id")
    public static final SqlColumn<Integer> hallId = hall.hallId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.738+05:30", comments="Source field: TBL_Hall.hall_code")
    public static final SqlColumn<String> hallCode = hall.hallCode;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.738+05:30", comments="Source field: TBL_Hall.cinema_id")
    public static final SqlColumn<Integer> cinemaId = hall.cinemaId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.739+05:30", comments="Source field: TBL_Hall.hall_row_count")
    public static final SqlColumn<Integer> hallRowCount = hall.hallRowCount;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.739+05:30", comments="Source field: TBL_Hall.hall_col_count")
    public static final SqlColumn<Integer> hallColCount = hall.hallColCount;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.739+05:30", comments="Source field: TBL_Hall.is_available")
    public static final SqlColumn<Boolean> isAvailable = hall.isAvailable;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.739+05:30", comments="Source field: TBL_Hall.created_at")
    public static final SqlColumn<Date> createdAt = hall.createdAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.739+05:30", comments="Source field: TBL_Hall.modified_at")
    public static final SqlColumn<Date> modifiedAt = hall.modifiedAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.738+05:30", comments="Source Table: TBL_Hall")
    public static final class Hall extends SqlTable {
        public final SqlColumn<Integer> hallId = column("hall_id", JDBCType.INTEGER);

        public final SqlColumn<String> hallCode = column("hall_code", JDBCType.VARCHAR);

        public final SqlColumn<Integer> cinemaId = column("cinema_id", JDBCType.INTEGER);

        public final SqlColumn<Integer> hallRowCount = column("hall_row_count", JDBCType.INTEGER);

        public final SqlColumn<Integer> hallColCount = column("hall_col_count", JDBCType.INTEGER);

        public final SqlColumn<Boolean> isAvailable = column("is_available", JDBCType.BIT);

        public final SqlColumn<Date> createdAt = column("created_at", JDBCType.TIMESTAMP);

        public final SqlColumn<Date> modifiedAt = column("modified_at", JDBCType.TIMESTAMP);

        public Hall() {
            super("TBL_Hall");
        }
    }
}