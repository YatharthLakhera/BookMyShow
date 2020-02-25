package com.project.bookmyshow.db.mappers;

import java.util.Date;
import javax.annotation.Generated;

public class Hall {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.735+05:30", comments="Source field: TBL_Hall.hall_id")
    private Integer hallId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.735+05:30", comments="Source field: TBL_Hall.hall_code")
    private String hallCode;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.736+05:30", comments="Source field: TBL_Hall.cinema_id")
    private Integer cinemaId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.736+05:30", comments="Source field: TBL_Hall.hall_row_count")
    private Integer hallRowCount;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.736+05:30", comments="Source field: TBL_Hall.hall_col_count")
    private Integer hallColCount;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.737+05:30", comments="Source field: TBL_Hall.is_available")
    private Boolean isAvailable;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.737+05:30", comments="Source field: TBL_Hall.created_at")
    private Date createdAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.737+05:30", comments="Source field: TBL_Hall.modified_at")
    private Date modifiedAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.735+05:30", comments="Source field: TBL_Hall.hall_id")
    public Integer getHallId() {
        return hallId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.735+05:30", comments="Source field: TBL_Hall.hall_id")
    public void setHallId(Integer hallId) {
        this.hallId = hallId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.735+05:30", comments="Source field: TBL_Hall.hall_code")
    public String getHallCode() {
        return hallCode;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.736+05:30", comments="Source field: TBL_Hall.hall_code")
    public void setHallCode(String hallCode) {
        this.hallCode = hallCode == null ? null : hallCode.trim();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.736+05:30", comments="Source field: TBL_Hall.cinema_id")
    public Integer getCinemaId() {
        return cinemaId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.736+05:30", comments="Source field: TBL_Hall.cinema_id")
    public void setCinemaId(Integer cinemaId) {
        this.cinemaId = cinemaId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.736+05:30", comments="Source field: TBL_Hall.hall_row_count")
    public Integer getHallRowCount() {
        return hallRowCount;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.736+05:30", comments="Source field: TBL_Hall.hall_row_count")
    public void setHallRowCount(Integer hallRowCount) {
        this.hallRowCount = hallRowCount;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.736+05:30", comments="Source field: TBL_Hall.hall_col_count")
    public Integer getHallColCount() {
        return hallColCount;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.737+05:30", comments="Source field: TBL_Hall.hall_col_count")
    public void setHallColCount(Integer hallColCount) {
        this.hallColCount = hallColCount;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.737+05:30", comments="Source field: TBL_Hall.is_available")
    public Boolean getIsAvailable() {
        return isAvailable;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.737+05:30", comments="Source field: TBL_Hall.is_available")
    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.737+05:30", comments="Source field: TBL_Hall.created_at")
    public Date getCreatedAt() {
        return createdAt;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.737+05:30", comments="Source field: TBL_Hall.created_at")
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.737+05:30", comments="Source field: TBL_Hall.modified_at")
    public Date getModifiedAt() {
        return modifiedAt;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.738+05:30", comments="Source field: TBL_Hall.modified_at")
    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }
}