package com.project.bookmyshow.db.mappers;

import javax.annotation.Generated;

public class StatusMaster {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.784+05:30", comments="Source field: TBL_StatusMaster.status_id")
    private Integer statusId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.784+05:30", comments="Source field: TBL_StatusMaster.status")
    private String status;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.784+05:30", comments="Source field: TBL_StatusMaster.status_id")
    public Integer getStatusId() {
        return statusId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.784+05:30", comments="Source field: TBL_StatusMaster.status_id")
    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.784+05:30", comments="Source field: TBL_StatusMaster.status")
    public String getStatus() {
        return status;
    }

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.784+05:30", comments="Source field: TBL_StatusMaster.status")
    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }
}