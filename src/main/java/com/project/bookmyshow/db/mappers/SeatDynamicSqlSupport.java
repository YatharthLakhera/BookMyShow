package com.project.bookmyshow.db.mappers;

import java.sql.JDBCType;
import java.util.Date;
import javax.annotation.Generated;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

public final class SeatDynamicSqlSupport {
    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.757+05:30", comments="Source Table: TBL_Seat")
    public static final Seat seat = new Seat();

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.758+05:30", comments="Source field: TBL_Seat.seat_id")
    public static final SqlColumn<Integer> seatId = seat.seatId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.758+05:30", comments="Source field: TBL_Seat.seat_code")
    public static final SqlColumn<String> seatCode = seat.seatCode;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.758+05:30", comments="Source field: TBL_Seat.hall_id")
    public static final SqlColumn<Integer> hallId = seat.hallId;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.758+05:30", comments="Source field: TBL_Seat.seat_row_loc")
    public static final SqlColumn<Integer> seatRowLoc = seat.seatRowLoc;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.758+05:30", comments="Source field: TBL_Seat.seat_col_loc")
    public static final SqlColumn<Integer> seatColLoc = seat.seatColLoc;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.759+05:30", comments="Source field: TBL_Seat.created_at")
    public static final SqlColumn<Date> createdAt = seat.createdAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.759+05:30", comments="Source field: TBL_Seat.modified_at")
    public static final SqlColumn<Date> modifiedAt = seat.modifiedAt;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.759+05:30", comments="Source field: TBL_Seat.extraData")
    public static final SqlColumn<String> extradata = seat.extradata;

    @Generated(value="org.mybatis.generator.api.MyBatisGenerator", date="2020-02-19T14:45:14.758+05:30", comments="Source Table: TBL_Seat")
    public static final class Seat extends SqlTable {
        public final SqlColumn<Integer> seatId = column("seat_id", JDBCType.INTEGER);

        public final SqlColumn<String> seatCode = column("seat_code", JDBCType.VARCHAR);

        public final SqlColumn<Integer> hallId = column("hall_id", JDBCType.INTEGER);

        public final SqlColumn<Integer> seatRowLoc = column("seat_row_loc", JDBCType.INTEGER);

        public final SqlColumn<Integer> seatColLoc = column("seat_col_loc", JDBCType.INTEGER);

        public final SqlColumn<Date> createdAt = column("created_at", JDBCType.TIMESTAMP);

        public final SqlColumn<Date> modifiedAt = column("modified_at", JDBCType.TIMESTAMP);

        public final SqlColumn<String> extradata = column("extraData", JDBCType.LONGVARCHAR);

        public Seat() {
            super("TBL_Seat");
        }
    }
}