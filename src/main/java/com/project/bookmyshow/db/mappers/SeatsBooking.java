package com.project.bookmyshow.db.mappers;

import java.util.Date;
import javax.annotation.Generated;

public class SeatsBooking {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.762+05:30", comments="Source field: TBL_SeatsBooking.seat_booking_id")
    private Integer seatBookingId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.762+05:30", comments="Source field: TBL_SeatsBooking.seat_id")
    private Integer seatId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.762+05:30", comments="Source field: TBL_SeatsBooking.booking_id")
    private Integer bookingId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.763+05:30", comments="Source field: TBL_SeatsBooking.scheduled_live_show_id")
    private Integer scheduledLiveShowId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.763+05:30", comments="Source field: TBL_SeatsBooking.seat_booking_status")
    private Integer seatBookingStatus;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.764+05:30", comments="Source field: TBL_SeatsBooking.created_at")
    private Date createdAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.764+05:30", comments="Source field: TBL_SeatsBooking.modified_at")
    private Date modifiedAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.762+05:30", comments="Source field: TBL_SeatsBooking.seat_booking_id")
    public Integer getSeatBookingId() {
        return seatBookingId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.762+05:30", comments="Source field: TBL_SeatsBooking.seat_booking_id")
    public void setSeatBookingId(Integer seatBookingId) {
        this.seatBookingId = seatBookingId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.762+05:30", comments="Source field: TBL_SeatsBooking.seat_id")
    public Integer getSeatId() {
        return seatId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.762+05:30", comments="Source field: TBL_SeatsBooking.seat_id")
    public void setSeatId(Integer seatId) {
        this.seatId = seatId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.763+05:30", comments="Source field: TBL_SeatsBooking.booking_id")
    public Integer getBookingId() {
        return bookingId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.763+05:30", comments="Source field: TBL_SeatsBooking.booking_id")
    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.763+05:30", comments="Source field: TBL_SeatsBooking.scheduled_live_show_id")
    public Integer getScheduledLiveShowId() {
        return scheduledLiveShowId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.763+05:30", comments="Source field: TBL_SeatsBooking.scheduled_live_show_id")
    public void setScheduledLiveShowId(Integer scheduledLiveShowId) {
        this.scheduledLiveShowId = scheduledLiveShowId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.763+05:30", comments="Source field: TBL_SeatsBooking.seat_booking_status")
    public Integer getSeatBookingStatus() {
        return seatBookingStatus;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.763+05:30", comments="Source field: TBL_SeatsBooking.seat_booking_status")
    public void setSeatBookingStatus(Integer seatBookingStatus) {
        this.seatBookingStatus = seatBookingStatus;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.764+05:30", comments="Source field: TBL_SeatsBooking.created_at")
    public Date getCreatedAt() {
        return createdAt;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.764+05:30", comments="Source field: TBL_SeatsBooking.created_at")
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.764+05:30", comments="Source field: TBL_SeatsBooking.modified_at")
    public Date getModifiedAt() {
        return modifiedAt;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.764+05:30", comments="Source field: TBL_SeatsBooking.modified_at")
    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }
}