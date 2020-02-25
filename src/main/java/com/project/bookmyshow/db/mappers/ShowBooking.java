package com.project.bookmyshow.db.mappers;

import java.util.Date;
import javax.annotation.Generated;

public class ShowBooking {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.773+05:30", comments="Source field: TBL_ShowBooking.booking_id")
    private Integer bookingId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.773+05:30", comments="Source field: TBL_ShowBooking.customer_id")
    private Integer customerId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.773+05:30", comments="Source field: TBL_ShowBooking.scheduled_live_show_id")
    private Integer scheduledLiveShowId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.774+05:30", comments="Source field: TBL_ShowBooking.total_seats_booked")
    private Integer totalSeatsBooked;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.774+05:30", comments="Source field: TBL_ShowBooking.convenience_fee")
    private Integer convenienceFee;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.774+05:30", comments="Source field: TBL_ShowBooking.total_booking_amount")
    private Double totalBookingAmount;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.774+05:30", comments="Source field: TBL_ShowBooking.booking_ref_no")
    private String bookingRefNo;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.775+05:30", comments="Source field: TBL_ShowBooking.status_id")
    private Integer statusId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.775+05:30", comments="Source field: TBL_ShowBooking.created_at")
    private Date createdAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.775+05:30", comments="Source field: TBL_ShowBooking.modified_at")
    private Date modifiedAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.773+05:30", comments="Source field: TBL_ShowBooking.booking_id")
    public Integer getBookingId() {
        return bookingId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.773+05:30", comments="Source field: TBL_ShowBooking.booking_id")
    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.773+05:30", comments="Source field: TBL_ShowBooking.customer_id")
    public Integer getCustomerId() {
        return customerId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.773+05:30", comments="Source field: TBL_ShowBooking.customer_id")
    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.773+05:30", comments="Source field: TBL_ShowBooking.scheduled_live_show_id")
    public Integer getScheduledLiveShowId() {
        return scheduledLiveShowId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.773+05:30", comments="Source field: TBL_ShowBooking.scheduled_live_show_id")
    public void setScheduledLiveShowId(Integer scheduledLiveShowId) {
        this.scheduledLiveShowId = scheduledLiveShowId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.774+05:30", comments="Source field: TBL_ShowBooking.total_seats_booked")
    public Integer getTotalSeatsBooked() {
        return totalSeatsBooked;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.774+05:30", comments="Source field: TBL_ShowBooking.total_seats_booked")
    public void setTotalSeatsBooked(Integer totalSeatsBooked) {
        this.totalSeatsBooked = totalSeatsBooked;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.774+05:30", comments="Source field: TBL_ShowBooking.convenience_fee")
    public Integer getConvenienceFee() {
        return convenienceFee;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.774+05:30", comments="Source field: TBL_ShowBooking.convenience_fee")
    public void setConvenienceFee(Integer convenienceFee) {
        this.convenienceFee = convenienceFee;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.774+05:30", comments="Source field: TBL_ShowBooking.total_booking_amount")
    public Double getTotalBookingAmount() {
        return totalBookingAmount;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.774+05:30", comments="Source field: TBL_ShowBooking.total_booking_amount")
    public void setTotalBookingAmount(Double totalBookingAmount) {
        this.totalBookingAmount = totalBookingAmount;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.774+05:30", comments="Source field: TBL_ShowBooking.booking_ref_no")
    public String getBookingRefNo() {
        return bookingRefNo;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.774+05:30", comments="Source field: TBL_ShowBooking.booking_ref_no")
    public void setBookingRefNo(String bookingRefNo) {
        this.bookingRefNo = bookingRefNo == null ? null : bookingRefNo.trim();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.775+05:30", comments="Source field: TBL_ShowBooking.status_id")
    public Integer getStatusId() {
        return statusId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.775+05:30", comments="Source field: TBL_ShowBooking.status_id")
    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.775+05:30", comments="Source field: TBL_ShowBooking.created_at")
    public Date getCreatedAt() {
        return createdAt;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.775+05:30", comments="Source field: TBL_ShowBooking.created_at")
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.775+05:30", comments="Source field: TBL_ShowBooking.modified_at")
    public Date getModifiedAt() {
        return modifiedAt;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.775+05:30", comments="Source field: TBL_ShowBooking.modified_at")
    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }
}