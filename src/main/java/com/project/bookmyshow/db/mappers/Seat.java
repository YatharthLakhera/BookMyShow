package com.project.bookmyshow.db.mappers;

import java.util.Date;
import javax.annotation.Generated;

public class Seat {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.755+05:30", comments="Source field: TBL_Seat.seat_id")
    private Integer seatId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.755+05:30", comments="Source field: TBL_Seat.seat_code")
    private String seatCode;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.755+05:30", comments="Source field: TBL_Seat.hall_id")
    private Integer hallId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.756+05:30", comments="Source field: TBL_Seat.seat_row_loc")
    private Integer seatRowLoc;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.756+05:30", comments="Source field: TBL_Seat.seat_col_loc")
    private Integer seatColLoc;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.756+05:30", comments="Source field: TBL_Seat.created_at")
    private Date createdAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.757+05:30", comments="Source field: TBL_Seat.modified_at")
    private Date modifiedAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.757+05:30", comments="Source field: TBL_Seat.extraData")
    private String extradata;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.755+05:30", comments="Source field: TBL_Seat.seat_id")
    public Integer getSeatId() {
        return seatId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.755+05:30", comments="Source field: TBL_Seat.seat_id")
    public void setSeatId(Integer seatId) {
        this.seatId = seatId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.755+05:30", comments="Source field: TBL_Seat.seat_code")
    public String getSeatCode() {
        return seatCode;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.755+05:30", comments="Source field: TBL_Seat.seat_code")
    public void setSeatCode(String seatCode) {
        this.seatCode = seatCode == null ? null : seatCode.trim();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.756+05:30", comments="Source field: TBL_Seat.hall_id")
    public Integer getHallId() {
        return hallId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.756+05:30", comments="Source field: TBL_Seat.hall_id")
    public void setHallId(Integer hallId) {
        this.hallId = hallId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.756+05:30", comments="Source field: TBL_Seat.seat_row_loc")
    public Integer getSeatRowLoc() {
        return seatRowLoc;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.756+05:30", comments="Source field: TBL_Seat.seat_row_loc")
    public void setSeatRowLoc(Integer seatRowLoc) {
        this.seatRowLoc = seatRowLoc;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.756+05:30", comments="Source field: TBL_Seat.seat_col_loc")
    public Integer getSeatColLoc() {
        return seatColLoc;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.756+05:30", comments="Source field: TBL_Seat.seat_col_loc")
    public void setSeatColLoc(Integer seatColLoc) {
        this.seatColLoc = seatColLoc;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.756+05:30", comments="Source field: TBL_Seat.created_at")
    public Date getCreatedAt() {
        return createdAt;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.756+05:30", comments="Source field: TBL_Seat.created_at")
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.757+05:30", comments="Source field: TBL_Seat.modified_at")
    public Date getModifiedAt() {
        return modifiedAt;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.757+05:30", comments="Source field: TBL_Seat.modified_at")
    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.757+05:30", comments="Source field: TBL_Seat.extraData")
    public String getExtradata() {
        return extradata;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.757+05:30", comments="Source field: TBL_Seat.extraData")
    public void setExtradata(String extradata) {
        this.extradata = extradata == null ? null : extradata.trim();
    }
}