package com.project.bookmyshow.db.mappers;

import java.sql.JDBCType;
import java.util.Date;
import javax.annotation.Generated;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public final class ShowBookingStatusDynamicSqlSupport {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.781+05:30", comments="Source Table: TBL_ShowBookingStatus")
    public static final ShowBookingStatus showBookingStatus = new ShowBookingStatus();

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.781+05:30", comments="Source field: TBL_ShowBookingStatus.show_booking_status_id")
    public static final SqlColumn<Integer> showBookingStatusId = showBookingStatus.showBookingStatusId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.781+05:30", comments="Source field: TBL_ShowBookingStatus.booking_id")
    public static final SqlColumn<Integer> bookingId = showBookingStatus.bookingId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.781+05:30", comments="Source field: TBL_ShowBookingStatus.status_id")
    public static final SqlColumn<Integer> statusId = showBookingStatus.statusId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.782+05:30", comments="Source field: TBL_ShowBookingStatus.created_at")
    public static final SqlColumn<Date> createdAt = showBookingStatus.createdAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.782+05:30", comments="Source field: TBL_ShowBookingStatus.modified_at")
    public static final SqlColumn<Date> modifiedAt = showBookingStatus.modifiedAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.781+05:30", comments="Source Table: TBL_ShowBookingStatus")
    public static final class ShowBookingStatus extends SqlTable {
        public final SqlColumn<Integer> showBookingStatusId = column("show_booking_status_id", JDBCType.INTEGER);

        public final SqlColumn<Integer> bookingId = column("booking_id", JDBCType.INTEGER);

        public final SqlColumn<Integer> statusId = column("status_id", JDBCType.INTEGER);

        public final SqlColumn<Date> createdAt = column("created_at", JDBCType.TIMESTAMP);

        public final SqlColumn<Date> modifiedAt = column("modified_at", JDBCType.TIMESTAMP);

        public ShowBookingStatus() {
            super("TBL_ShowBookingStatus");
        }
    }
}