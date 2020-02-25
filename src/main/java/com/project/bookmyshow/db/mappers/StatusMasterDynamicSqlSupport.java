package com.project.bookmyshow.db.mappers;

import java.sql.JDBCType;
import javax.annotation.Generated;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public final class StatusMasterDynamicSqlSupport {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.784+05:30", comments="Source Table: TBL_StatusMaster")
    public static final StatusMaster statusMaster = new StatusMaster();

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.784+05:30", comments="Source field: TBL_StatusMaster.status_id")
    public static final SqlColumn<Integer> statusId = statusMaster.statusId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.785+05:30", comments="Source field: TBL_StatusMaster.status")
    public static final SqlColumn<String> status = statusMaster.status;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.784+05:30", comments="Source Table: TBL_StatusMaster")
    public static final class StatusMaster extends SqlTable {
        public final SqlColumn<Integer> statusId = column("status_id", JDBCType.INTEGER);

        public final SqlColumn<String> status = column("status", JDBCType.VARCHAR);

        public StatusMaster() {
            super("TBL_StatusMaster");
        }
    }
}