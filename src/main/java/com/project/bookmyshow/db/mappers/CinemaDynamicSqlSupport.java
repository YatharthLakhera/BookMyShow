package com.project.bookmyshow.db.mappers;

import java.sql.JDBCType;
import java.util.Date;
import javax.annotation.Generated;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public final class CinemaDynamicSqlSupport {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.731+05:30", comments="Source Table: TBL_Cinema")
    public static final Cinema cinema = new Cinema();

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.731+05:30", comments="Source field: TBL_Cinema.cinema_id")
    public static final SqlColumn<Integer> cinemaId = cinema.cinemaId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.731+05:30", comments="Source field: TBL_Cinema.cinema_name")
    public static final SqlColumn<String> cinemaName = cinema.cinemaName;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.732+05:30", comments="Source field: TBL_Cinema.created_at")
    public static final SqlColumn<Date> createdAt = cinema.createdAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.732+05:30", comments="Source field: TBL_Cinema.modified_at")
    public static final SqlColumn<Date> modifiedAt = cinema.modifiedAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.732+05:30", comments="Source field: TBL_Cinema.is_open")
    public static final SqlColumn<Boolean> isOpen = cinema.isOpen;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.732+05:30", comments="Source field: TBL_Cinema.extraData")
    public static final SqlColumn<String> extradata = cinema.extradata;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.731+05:30", comments="Source Table: TBL_Cinema")
    public static final class Cinema extends SqlTable {
        public final SqlColumn<Integer> cinemaId = column("cinema_id", JDBCType.INTEGER);

        public final SqlColumn<String> cinemaName = column("cinema_name", JDBCType.VARCHAR);

        public final SqlColumn<Date> createdAt = column("created_at", JDBCType.TIMESTAMP);

        public final SqlColumn<Date> modifiedAt = column("modified_at", JDBCType.TIMESTAMP);

        public final SqlColumn<Boolean> isOpen = column("is_open", JDBCType.BIT);

        public final SqlColumn<String> extradata = column("extraData", JDBCType.LONGVARCHAR);

        public Cinema() {
            super("TBL_Cinema");
        }
    }
}