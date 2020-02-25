package com.project.bookmyshow.db.mappers;

import java.sql.JDBCType;
import java.util.Date;
import javax.annotation.Generated;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public final class ShowBookingDynamicSqlSupport {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.775+05:30", comments="Source Table: TBL_ShowBooking")
    public static final ShowBooking showBooking = new ShowBooking();

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.776+05:30", comments="Source field: TBL_ShowBooking.booking_id")
    public static final SqlColumn<Integer> bookingId = showBooking.bookingId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.776+05:30", comments="Source field: TBL_ShowBooking.customer_id")
    public static final SqlColumn<Integer> customerId = showBooking.customerId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.776+05:30", comments="Source field: TBL_ShowBooking.scheduled_live_show_id")
    public static final SqlColumn<Integer> scheduledLiveShowId = showBooking.scheduledLiveShowId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.776+05:30", comments="Source field: TBL_ShowBooking.total_seats_booked")
    public static final SqlColumn<Integer> totalSeatsBooked = showBooking.totalSeatsBooked;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.776+05:30", comments="Source field: TBL_ShowBooking.convenience_fee")
    public static final SqlColumn<Integer> convenienceFee = showBooking.convenienceFee;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.776+05:30", comments="Source field: TBL_ShowBooking.total_booking_amount")
    public static final SqlColumn<Double> totalBookingAmount = showBooking.totalBookingAmount;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.777+05:30", comments="Source field: TBL_ShowBooking.booking_ref_no")
    public static final SqlColumn<String> bookingRefNo = showBooking.bookingRefNo;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.777+05:30", comments="Source field: TBL_ShowBooking.status_id")
    public static final SqlColumn<Integer> statusId = showBooking.statusId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.777+05:30", comments="Source field: TBL_ShowBooking.created_at")
    public static final SqlColumn<Date> createdAt = showBooking.createdAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.777+05:30", comments="Source field: TBL_ShowBooking.modified_at")
    public static final SqlColumn<Date> modifiedAt = showBooking.modifiedAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.776+05:30", comments="Source Table: TBL_ShowBooking")
    public static final class ShowBooking extends SqlTable {
        public final SqlColumn<Integer> bookingId = column("booking_id", JDBCType.INTEGER);

        public final SqlColumn<Integer> customerId = column("customer_id", JDBCType.INTEGER);

        public final SqlColumn<Integer> scheduledLiveShowId = column("scheduled_live_show_id", JDBCType.INTEGER);

        public final SqlColumn<Integer> totalSeatsBooked = column("total_seats_booked", JDBCType.INTEGER);

        public final SqlColumn<Integer> convenienceFee = column("convenience_fee", JDBCType.INTEGER);

        public final SqlColumn<Double> totalBookingAmount = column("total_booking_amount", JDBCType.INTEGER, "com.project.bookmyshow.db.typehandlers.AmountTypeHandler");

        public final SqlColumn<String> bookingRefNo = column("booking_ref_no", JDBCType.VARCHAR);

        public final SqlColumn<Integer> statusId = column("status_id", JDBCType.INTEGER);

        public final SqlColumn<Date> createdAt = column("created_at", JDBCType.TIMESTAMP);

        public final SqlColumn<Date> modifiedAt = column("modified_at", JDBCType.TIMESTAMP);

        public ShowBooking() {
            super("TBL_ShowBooking");
        }
    }
}