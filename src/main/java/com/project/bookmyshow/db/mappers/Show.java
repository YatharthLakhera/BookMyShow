package com.project.bookmyshow.db.mappers;

import com.project.bookmyshow.enums.ShowType;
import java.util.Date;
import javax.annotation.Generated;

public class Show {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.767+05:30", comments="Source field: TBL_Show.show_id")
    private Integer showId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.768+05:30", comments="Source field: TBL_Show.show_type")
    private ShowType showType;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.768+05:30", comments="Source field: TBL_Show.show_name")
    private String showName;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.768+05:30", comments="Source field: TBL_Show.created_at")
    private Date createdAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.769+05:30", comments="Source field: TBL_Show.modified_at")
    private Date modifiedAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.769+05:30", comments="Source field: TBL_Show.show_details")
    private String showDetails;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.768+05:30", comments="Source field: TBL_Show.show_id")
    public Integer getShowId() {
        return showId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.768+05:30", comments="Source field: TBL_Show.show_id")
    public void setShowId(Integer showId) {
        this.showId = showId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.768+05:30", comments="Source field: TBL_Show.show_type")
    public ShowType getShowType() {
        return showType;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.768+05:30", comments="Source field: TBL_Show.show_type")
    public void setShowType(ShowType showType) {
        this.showType = showType;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.768+05:30", comments="Source field: TBL_Show.show_name")
    public String getShowName() {
        return showName;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.768+05:30", comments="Source field: TBL_Show.show_name")
    public void setShowName(String showName) {
        this.showName = showName == null ? null : showName.trim();
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.769+05:30", comments="Source field: TBL_Show.created_at")
    public Date getCreatedAt() {
        return createdAt;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.769+05:30", comments="Source field: TBL_Show.created_at")
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.769+05:30", comments="Source field: TBL_Show.modified_at")
    public Date getModifiedAt() {
        return modifiedAt;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.769+05:30", comments="Source field: TBL_Show.modified_at")
    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.769+05:30", comments="Source field: TBL_Show.show_details")
    public String getShowDetails() {
        return showDetails;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.769+05:30", comments="Source field: TBL_Show.show_details")
    public void setShowDetails(String showDetails) {
        this.showDetails = showDetails == null ? null : showDetails.trim();
    }
}