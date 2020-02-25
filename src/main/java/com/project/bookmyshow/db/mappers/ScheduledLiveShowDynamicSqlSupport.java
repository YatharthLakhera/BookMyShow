package com.project.bookmyshow.db.mappers;

import java.sql.JDBCType;
import java.util.Date;
import javax.annotation.Generated;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public final class ScheduledLiveShowDynamicSqlSupport {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.75+05:30", comments="Source Table: TBL_ScheduledLiveShow")
    public static final ScheduledLiveShow scheduledLiveShow = new ScheduledLiveShow();

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.751+05:30", comments="Source field: TBL_ScheduledLiveShow.scheduled_live_show_id")
    public static final SqlColumn<Integer> scheduledLiveShowId = scheduledLiveShow.scheduledLiveShowId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.751+05:30", comments="Source field: TBL_ScheduledLiveShow.live_show_id")
    public static final SqlColumn<Integer> liveShowId = scheduledLiveShow.liveShowId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.751+05:30", comments="Source field: TBL_ScheduledLiveShow.show_start_time")
    public static final SqlColumn<Date> showStartTime = scheduledLiveShow.showStartTime;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.751+05:30", comments="Source field: TBL_ScheduledLiveShow.show_end_time")
    public static final SqlColumn<Date> showEndTime = scheduledLiveShow.showEndTime;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.751+05:30", comments="Source field: TBL_ScheduledLiveShow.tickets_price")
    public static final SqlColumn<Double> ticketsPrice = scheduledLiveShow.ticketsPrice;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.752+05:30", comments="Source field: TBL_ScheduledLiveShow.created_at")
    public static final SqlColumn<Date> createdAt = scheduledLiveShow.createdAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.752+05:30", comments="Source field: TBL_ScheduledLiveShow.modified_at")
    public static final SqlColumn<Date> modifiedAt = scheduledLiveShow.modifiedAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.751+05:30", comments="Source Table: TBL_ScheduledLiveShow")
    public static final class ScheduledLiveShow extends SqlTable {
        public final SqlColumn<Integer> scheduledLiveShowId = column("scheduled_live_show_id", JDBCType.INTEGER);

        public final SqlColumn<Integer> liveShowId = column("live_show_id", JDBCType.INTEGER);

        public final SqlColumn<Date> showStartTime = column("show_start_time", JDBCType.TIMESTAMP);

        public final SqlColumn<Date> showEndTime = column("show_end_time", JDBCType.TIMESTAMP);

        public final SqlColumn<Double> ticketsPrice = column("tickets_price", JDBCType.INTEGER, "com.project.bookmyshow.db.typehandlers.AmountTypeHandler");

        public final SqlColumn<Date> createdAt = column("created_at", JDBCType.TIMESTAMP);

        public final SqlColumn<Date> modifiedAt = column("modified_at", JDBCType.TIMESTAMP);

        public ScheduledLiveShow() {
            super("TBL_ScheduledLiveShow");
        }
    }
}