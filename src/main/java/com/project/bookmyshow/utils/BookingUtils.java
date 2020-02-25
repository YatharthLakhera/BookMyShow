package com.project.bookmyshow.utils;

import com.project.bookmyshow.constants.StatusConstant;
import com.project.bookmyshow.db.mappers.SeatsBooking;
import com.project.bookmyshow.db.mappers.ShowBooking;
import lombok.NonNull;

import java.util.Date;

public class BookingUtils {

    private static final int TICKET_HOLD_TIME_IN_MILLISEC = 300000;

    public static boolean isSeatOnHold(@NonNull SeatsBooking seatsBooking) {
        return isOnHold(seatsBooking.getSeatBookingStatus(), seatsBooking.getModifiedAt());
    }

    public static boolean isSeatOnHold(@NonNull SeatsBooking seatsBooking, long currentTimeInMilliSeconds) {
        return isOnHold(seatsBooking.getSeatBookingStatus(), seatsBooking.getModifiedAt(), currentTimeInMilliSeconds);
    }

    public static boolean isBookingSessionOnHold(ShowBooking showBooking) {
        return isOnHold(showBooking.getStatusId(), showBooking.getModifiedAt());
    }

    public static boolean isBookingSessionOnHold(ShowBooking showBooking, long currentTimeInMilliSeconds) {
        return isOnHold(showBooking.getStatusId(), showBooking.getModifiedAt(), currentTimeInMilliSeconds);
    }

    private static boolean isOnHold(int statusId, Date modifiedTime) {
        return isOnHold(statusId, modifiedTime, System.currentTimeMillis());
    }

    private static boolean isOnHold(int statusId, Date modifiedTime, long currentTimeInMilliSeconds) {
        boolean isSeatOnHold = false;
        if (statusId == StatusConstant.INITIATED
                && (currentTimeInMilliSeconds - modifiedTime.getTime()
                < TICKET_HOLD_TIME_IN_MILLISEC)) {
            isSeatOnHold = true;
        }
        return isSeatOnHold;
    }
}
