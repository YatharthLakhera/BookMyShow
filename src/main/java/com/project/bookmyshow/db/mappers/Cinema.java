package com.project.bookmyshow.db.mappers;

import java.util.Date;
import javax.annotation.Generated;

public class Cinema {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.729+05:30", comments="Source field: TBL_Cinema.cinema_id")
    private Integer cinemaId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.729+05:30", comments="Source field: TBL_Cinema.cinema_name")
    private String cinemaName;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.729+05:30", comments="Source field: TBL_Cinema.created_at")
    private Date createdAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.73+05:30", comments="Source field: TBL_Cinema.modified_at")
    private Date modifiedAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.73+05:30", comments="Source field: TBL_Cinema.is_open")
    private Boolean isOpen;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.73+05:30", comments="Source field: TBL_Cinema.extraData")
    private String extradata;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.729+05:30", comments="Source field: TBL_Cinema.cinema_id")
    public Integer getCinemaId() {
        return cinemaId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.729+05:30", comments="Source field: TBL_Cinema.cinema_id")
    public void setCinemaId(Integer cinemaId) {
        this.cinemaId = cinemaId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.729+05:30", comments="Source field: TBL_Cinema.cinema_name")
    public String getCinemaName() {
        return cinemaName;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.729+05:30", comments="Source field: TBL_Cinema.cinema_name")
    public void setCinemaName(String cinemaName) {
        this.cinemaName = cinemaName == null ? null : cinemaName.trim();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.729+05:30", comments="Source field: TBL_Cinema.created_at")
    public Date getCreatedAt() {
        return createdAt;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.73+05:30", comments="Source field: TBL_Cinema.created_at")
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.73+05:30", comments="Source field: TBL_Cinema.modified_at")
    public Date getModifiedAt() {
        return modifiedAt;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.73+05:30", comments="Source field: TBL_Cinema.modified_at")
    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.73+05:30", comments="Source field: TBL_Cinema.is_open")
    public Boolean getIsOpen() {
        return isOpen;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.73+05:30", comments="Source field: TBL_Cinema.is_open")
    public void setIsOpen(Boolean isOpen) {
        this.isOpen = isOpen;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.731+05:30", comments="Source field: TBL_Cinema.extraData")
    public String getExtradata() {
        return extradata;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.731+05:30", comments="Source field: TBL_Cinema.extraData")
    public void setExtradata(String extradata) {
        this.extradata = extradata == null ? null : extradata.trim();
    }
}