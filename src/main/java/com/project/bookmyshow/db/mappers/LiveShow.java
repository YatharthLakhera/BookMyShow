package com.project.bookmyshow.db.mappers;

import java.util.Date;
import javax.annotation.Generated;

public class LiveShow {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.742+05:30", comments="Source field: TBL_LiveShow.live_show_id")
    private Integer liveShowId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.743+05:30", comments="Source field: TBL_LiveShow.show_id")
    private Integer showId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.743+05:30", comments="Source field: TBL_LiveShow.hall_id")
    private Integer hallId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.743+05:30", comments="Source field: TBL_LiveShow.created_at")
    private Date createdAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.744+05:30", comments="Source field: TBL_LiveShow.modified_at")
    private Date modifiedAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.742+05:30", comments="Source field: TBL_LiveShow.live_show_id")
    public Integer getLiveShowId() {
        return liveShowId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.743+05:30", comments="Source field: TBL_LiveShow.live_show_id")
    public void setLiveShowId(Integer liveShowId) {
        this.liveShowId = liveShowId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.743+05:30", comments="Source field: TBL_LiveShow.show_id")
    public Integer getShowId() {
        return showId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.743+05:30", comments="Source field: TBL_LiveShow.show_id")
    public void setShowId(Integer showId) {
        this.showId = showId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.743+05:30", comments="Source field: TBL_LiveShow.hall_id")
    public Integer getHallId() {
        return hallId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.743+05:30", comments="Source field: TBL_LiveShow.hall_id")
    public void setHallId(Integer hallId) {
        this.hallId = hallId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.744+05:30", comments="Source field: TBL_LiveShow.created_at")
    public Date getCreatedAt() {
        return createdAt;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.744+05:30", comments="Source field: TBL_LiveShow.created_at")
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.744+05:30", comments="Source field: TBL_LiveShow.modified_at")
    public Date getModifiedAt() {
        return modifiedAt;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.744+05:30", comments="Source field: TBL_LiveShow.modified_at")
    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }
}