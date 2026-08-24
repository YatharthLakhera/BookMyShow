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

    /**
     * Single authoritative answer to "does this row make its seat unavailable?".
     *
     * A seat is taken when it is confirmed({@link StatusConstant#SUCCESS}), when payment
     * is under way({@link StatusConstant#INPROGRESS}), or when it is held by a checkout
     * that has not yet run out of time({@link StatusConstant#INITIATED}). A booking that
     * failed({@link StatusConstant#FAILED}) releases the seat.
     *
     * Every availability check must go through this method so the rule cannot drift
     * between callers.
     * @param seatsBooking
     * @param currentTimeInMilliSeconds
     * @return
     */
    public static boolean occupiesSeat(@NonNull SeatsBooking seatsBooking, long currentTimeInMilliSeconds) {
        switch (seatsBooking.getSeatBookingStatus()) {
            case StatusConstant.SUCCESS:
            case StatusConstant.INPROGRESS:
                return true;
            case StatusConstant.INITIATED:
                return isSeatOnHold(seatsBooking, currentTimeInMilliSeconds);
            case StatusConstant.FAILED:
                return false;
            default:
                // An unrecognised status must never be read as "free" - refusing to
                // guess is the safe direction for a seat availability check.
                throw new IllegalStateException(
                        "Unknown seat booking status : " + seatsBooking.getSeatBookingStatus());
        }
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
