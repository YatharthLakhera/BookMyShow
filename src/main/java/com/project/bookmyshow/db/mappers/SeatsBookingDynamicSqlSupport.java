package com.project.bookmyshow.db.mappers;

import java.sql.JDBCType;
import java.util.Date;
import javax.annotation.Generated;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public final class SeatsBookingDynamicSqlSupport {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.764+05:30", comments="Source Table: TBL_SeatsBooking")
    public static final SeatsBooking seatsBooking = new SeatsBooking();

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.764+05:30", comments="Source field: TBL_SeatsBooking.seat_booking_id")
    public static final SqlColumn<Integer> seatBookingId = seatsBooking.seatBookingId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.764+05:30", comments="Source field: TBL_SeatsBooking.seat_id")
    public static final SqlColumn<Integer> seatId = seatsBooking.seatId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.765+05:30", comments="Source field: TBL_SeatsBooking.booking_id")
    public static final SqlColumn<Integer> bookingId = seatsBooking.bookingId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.765+05:30", comments="Source field: TBL_SeatsBooking.scheduled_live_show_id")
    public static final SqlColumn<Integer> scheduledLiveShowId = seatsBooking.scheduledLiveShowId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.765+05:30", comments="Source field: TBL_SeatsBooking.seat_booking_status")
    public static final SqlColumn<Integer> seatBookingStatus = seatsBooking.seatBookingStatus;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.765+05:30", comments="Source field: TBL_SeatsBooking.created_at")
    public static final SqlColumn<Date> createdAt = seatsBooking.createdAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.765+05:30", comments="Source field: TBL_SeatsBooking.modified_at")
    public static final SqlColumn<Date> modifiedAt = seatsBooking.modifiedAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.764+05:30", comments="Source Table: TBL_SeatsBooking")
    public static final class SeatsBooking extends SqlTable {
        public final SqlColumn<Integer> seatBookingId = column("seat_booking_id", JDBCType.INTEGER);

        public final SqlColumn<Integer> seatId = column("seat_id", JDBCType.INTEGER);

        public final SqlColumn<Integer> bookingId = column("booking_id", JDBCType.INTEGER);

        public final SqlColumn<Integer> scheduledLiveShowId = column("scheduled_live_show_id", JDBCType.INTEGER);

        public final SqlColumn<Integer> seatBookingStatus = column("seat_booking_status", JDBCType.INTEGER);

        public final SqlColumn<Date> createdAt = column("created_at", JDBCType.TIMESTAMP);

        public final SqlColumn<Date> modifiedAt = column("modified_at", JDBCType.TIMESTAMP);

        public SeatsBooking() {
            super("TBL_SeatsBooking");
        }
    }
}