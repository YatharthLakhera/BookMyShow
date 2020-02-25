package com.project.bookmyshow.db.mappers;

import java.sql.JDBCType;
import java.util.Date;
import javax.annotation.Generated;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public final class LiveShowDynamicSqlSupport {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.744+05:30", comments="Source Table: TBL_LiveShow")
    public static final LiveShow liveShow = new LiveShow();

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.745+05:30", comments="Source field: TBL_LiveShow.live_show_id")
    public static final SqlColumn<Integer> liveShowId = liveShow.liveShowId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.745+05:30", comments="Source field: TBL_LiveShow.show_id")
    public static final SqlColumn<Integer> showId = liveShow.showId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.745+05:30", comments="Source field: TBL_LiveShow.hall_id")
    public static final SqlColumn<Integer> hallId = liveShow.hallId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.745+05:30", comments="Source field: TBL_LiveShow.created_at")
    public static final SqlColumn<Date> createdAt = liveShow.createdAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.745+05:30", comments="Source field: TBL_LiveShow.modified_at")
    public static final SqlColumn<Date> modifiedAt = liveShow.modifiedAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.744+05:30", comments="Source Table: TBL_LiveShow")
    public static final class LiveShow extends SqlTable {
        public final SqlColumn<Integer> liveShowId = column("live_show_id", JDBCType.INTEGER);

        public final SqlColumn<Integer> showId = column("show_id", JDBCType.INTEGER);

        public final SqlColumn<Integer> hallId = column("hall_id", JDBCType.INTEGER);

        public final SqlColumn<Date> createdAt = column("created_at", JDBCType.TIMESTAMP);

        public final SqlColumn<Date> modifiedAt = column("modified_at", JDBCType.TIMESTAMP);

        public LiveShow() {
            super("TBL_LiveShow");
        }
    }
}