package com.test.bookmyshow;

import com.project.bookmyshow.constants.StatusConstant;
import com.project.bookmyshow.db.mappers.SeatsBooking;
import com.project.bookmyshow.utils.BookingUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

/**
 * Covers the seat availability rule.
 *
 * The rule used to be implemented twice - once correctly in getBookedSeats and once
 * inverted in areSeatsAvailable, where a NOT IN (INPROGRESS, SUCCESS) filter removed
 * confirmed seats from the result set and so reported a sold seat as free. Both callers
 * now share BookingUtils.occupiesSeat, and these tests pin its behaviour down.
 *
 * The current time is passed in rather than read from the clock, so the boundary can be
 * asserted exactly without sleeping.
 */
public class BookingUtilsTest {

    private static final long NOW = 1_600_000_000_000L;
    private static final long HOLD_MILLIS = 300_000L;

    private SeatsBooking seatWith(int status, long modifiedAtMillis) {
        SeatsBooking seatsBooking = new SeatsBooking();
        seatsBooking.setSeatId(1);
        seatsBooking.setBookingId(1);
        seatsBooking.setScheduledLiveShowId(1);
        seatsBooking.setSeatBookingStatus(status);
        seatsBooking.setModifiedAt(new Date(modifiedAtMillis));
        return seatsBooking;
    }

    /** The regression that mattered: a paid seat must never be offered to anyone else. */
    @Test
    public void confirmedSeatIsOccupied() {
        Assert.assertTrue(BookingUtils.occupiesSeat(seatWith(StatusConstant.SUCCESS, NOW - 10 * HOLD_MILLIS), NOW));
    }

    @Test
    public void seatBeingPaidForIsOccupied() {
        Assert.assertTrue(BookingUtils.occupiesSeat(seatWith(StatusConstant.INPROGRESS, NOW - 10 * HOLD_MILLIS), NOW));
    }

    @Test
    public void failedBookingReleasesSeat() {
        Assert.assertFalse(BookingUtils.occupiesSeat(seatWith(StatusConstant.FAILED, NOW), NOW));
    }

    @Test
    public void freshHoldOccupiesSeat() {
        Assert.assertTrue(BookingUtils.occupiesSeat(seatWith(StatusConstant.INITIATED, NOW), NOW));
    }

    @Test
    public void holdOneMillisecondBeforeExpiryStillOccupiesSeat() {
        long modifiedAt = NOW - HOLD_MILLIS + 1;
        Assert.assertTrue(BookingUtils.occupiesSeat(seatWith(StatusConstant.INITIATED, modifiedAt), NOW));
    }

    @Test
    public void holdIsReleasedExactlyAtTheHoldWindow() {
        long modifiedAt = NOW - HOLD_MILLIS;
        Assert.assertFalse(BookingUtils.occupiesSeat(seatWith(StatusConstant.INITIATED, modifiedAt), NOW));
    }

    @Test
    public void lapsedHoldReleasesSeat() {
        long modifiedAt = NOW - HOLD_MILLIS - 1;
        Assert.assertFalse(BookingUtils.occupiesSeat(seatWith(StatusConstant.INITIATED, modifiedAt), NOW));
    }

    /** An unrecognised status must fail loudly rather than defaulting to "free". */
    @Test(expected = IllegalStateException.class)
    public void unknownStatusIsRejected() {
        BookingUtils.occupiesSeat(seatWith(99, NOW), NOW);
    }
}
