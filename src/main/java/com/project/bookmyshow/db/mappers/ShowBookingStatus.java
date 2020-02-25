package com.project.bookmyshow.db.mappers;

import java.util.Date;
import javax.annotation.Generated;

public class ShowBookingStatus {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.779+05:30", comments="Source field: TBL_ShowBookingStatus.show_booking_status_id")
    private Integer showBookingStatusId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.78+05:30", comments="Source field: TBL_ShowBookingStatus.booking_id")
    private Integer bookingId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.78+05:30", comments="Source field: TBL_ShowBookingStatus.status_id")
    private Integer statusId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.78+05:30", comments="Source field: TBL_ShowBookingStatus.created_at")
    private Date createdAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.78+05:30", comments="Source field: TBL_ShowBookingStatus.modified_at")
    private Date modifiedAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.779+05:30", comments="Source field: TBL_ShowBookingStatus.show_booking_status_id")
    public Integer getShowBookingStatusId() {
        return showBookingStatusId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.78+05:30", comments="Source field: TBL_ShowBookingStatus.show_booking_status_id")
    public void setShowBookingStatusId(Integer showBookingStatusId) {
        this.showBookingStatusId = showBookingStatusId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.78+05:30", comments="Source field: TBL_ShowBookingStatus.booking_id")
    public Integer getBookingId() {
        return bookingId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.78+05:30", comments="Source field: TBL_ShowBookingStatus.booking_id")
    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.78+05:30", comments="Source field: TBL_ShowBookingStatus.status_id")
    public Integer getStatusId() {
        return statusId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.78+05:30", comments="Source field: TBL_ShowBookingStatus.status_id")
    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.78+05:30", comments="Source field: TBL_ShowBookingStatus.created_at")
    public Date getCreatedAt() {
        return createdAt;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.78+05:30", comments="Source field: TBL_ShowBookingStatus.created_at")
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.78+05:30", comments="Source field: TBL_ShowBookingStatus.modified_at")
    public Date getModifiedAt() {
        return modifiedAt;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.78+05:30", comments="Source field: TBL_ShowBookingStatus.modified_at")
    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }
}