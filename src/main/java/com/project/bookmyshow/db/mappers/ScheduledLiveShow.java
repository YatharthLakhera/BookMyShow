package com.project.bookmyshow.db.mappers;

import java.util.Date;
import javax.annotation.Generated;

public class ScheduledLiveShow {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.748+05:30", comments="Source field: TBL_ScheduledLiveShow.scheduled_live_show_id")
    private Integer scheduledLiveShowId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.748+05:30", comments="Source field: TBL_ScheduledLiveShow.live_show_id")
    private Integer liveShowId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.749+05:30", comments="Source field: TBL_ScheduledLiveShow.show_start_time")
    private Date showStartTime;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.749+05:30", comments="Source field: TBL_ScheduledLiveShow.show_end_time")
    private Date showEndTime;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.749+05:30", comments="Source field: TBL_ScheduledLiveShow.tickets_price")
    private Double ticketsPrice;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.75+05:30", comments="Source field: TBL_ScheduledLiveShow.created_at")
    private Date createdAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.75+05:30", comments="Source field: TBL_ScheduledLiveShow.modified_at")
    private Date modifiedAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.748+05:30", comments="Source field: TBL_ScheduledLiveShow.scheduled_live_show_id")
    public Integer getScheduledLiveShowId() {
        return scheduledLiveShowId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.748+05:30", comments="Source field: TBL_ScheduledLiveShow.scheduled_live_show_id")
    public void setScheduledLiveShowId(Integer scheduledLiveShowId) {
        this.scheduledLiveShowId = scheduledLiveShowId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.748+05:30", comments="Source field: TBL_ScheduledLiveShow.live_show_id")
    public Integer getLiveShowId() {
        return liveShowId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.749+05:30", comments="Source field: TBL_ScheduledLiveShow.live_show_id")
    public void setLiveShowId(Integer liveShowId) {
        this.liveShowId = liveShowId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.749+05:30", comments="Source field: TBL_ScheduledLiveShow.show_start_time")
    public Date getShowStartTime() {
        return showStartTime;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.749+05:30", comments="Source field: TBL_ScheduledLiveShow.show_start_time")
    public void setShowStartTime(Date showStartTime) {
        this.showStartTime = showStartTime;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.749+05:30", comments="Source field: TBL_ScheduledLiveShow.show_end_time")
    public Date getShowEndTime() {
        return showEndTime;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.749+05:30", comments="Source field: TBL_ScheduledLiveShow.show_end_time")
    public void setShowEndTime(Date showEndTime) {
        this.showEndTime = showEndTime;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.749+05:30", comments="Source field: TBL_ScheduledLiveShow.tickets_price")
    public Double getTicketsPrice() {
        return ticketsPrice;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.75+05:30", comments="Source field: TBL_ScheduledLiveShow.tickets_price")
    public void setTicketsPrice(Double ticketsPrice) {
        this.ticketsPrice = ticketsPrice;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.75+05:30", comments="Source field: TBL_ScheduledLiveShow.created_at")
    public Date getCreatedAt() {
        return createdAt;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.75+05:30", comments="Source field: TBL_ScheduledLiveShow.created_at")
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.75+05:30", comments="Source field: TBL_ScheduledLiveShow.modified_at")
    public Date getModifiedAt() {
        return modifiedAt;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.75+05:30", comments="Source field: TBL_ScheduledLiveShow.modified_at")
    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }
}