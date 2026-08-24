package com.project.bookmyshow.db.dao;

import com.project.bookmyshow.constants.ErrorMessages;
import com.project.bookmyshow.constants.StatusConstant;
import com.project.bookmyshow.db.ConnectionFactory;
import com.project.bookmyshow.db.mappers.SeatsBooking;
import com.project.bookmyshow.db.mappers.SeatsBookingDynamicSqlSupport;
import com.project.bookmyshow.db.mappers.SeatsBookingMapper;
import com.project.bookmyshow.db.mappers.ShowBooking;
import com.project.bookmyshow.exceptions.BookingException;
import com.project.bookmyshow.utils.BookingUtils;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.dynamic.sql.SqlBuilder;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Slf4j
@Repository
public class SeatBookingDAO {

    public Set<Integer> getBookedSeats(List<Integer> showBookingIds) {
        @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
        SeatsBookingMapper seatsBookingMapper = sqlSession.getMapper(SeatsBookingMapper.class);
        Set<Integer> bookedSeatIds = new HashSet<>();
        if (CollectionUtils.isEmpty(showBookingIds)) {
            // An empty IN() list is not valid SQL - no bookings means no seats taken.
            return bookedSeatIds;
        }
        long curTimeInMillisec = System.currentTimeMillis();
        for (SeatsBooking seatsBooking : getBookedSeats(showBookingIds, seatsBookingMapper)) {
            if (BookingUtils.occupiesSeat(seatsBooking, curTimeInMillisec)) {
                bookedSeatIds.add(seatsBooking.getSeatId());
            }
        }
        return bookedSeatIds;
    }

    public List<SeatsBooking> getBookedSeats(List<Integer> showBookingIds, SeatsBookingMapper seatsBookingMapper) {
        SeatsBookingDynamicSqlSupport.SeatsBooking seatsBookingSqlSupport = new SeatsBookingDynamicSqlSupport.SeatsBooking();
        return seatsBookingMapper.selectByExample()
                .where(seatsBookingSqlSupport.bookingId, SqlBuilder.isIn(showBookingIds))
                .build().execute();
    }

    public boolean areSeatsAvailable(List<Integer> seatIds, int scheduledLiveShowId) {
        return getOccupiedSeatIds(seatIds, scheduledLiveShowId).isEmpty();
    }

    /**
     * Returns the subset of the given seats that cannot currently be booked for the show.
     * The query deliberately fetches every row for those seats and lets
     * {@link BookingUtils#occupiesSeat} classify them, so the availability rule lives in
     * exactly one place.
     * @param seatIds
     * @param scheduledLiveShowId
     * @return
     */
    public Set<Integer> getOccupiedSeatIds(List<Integer> seatIds, int scheduledLiveShowId) {
        Set<Integer> occupiedSeatIds = new HashSet<>();
        if (CollectionUtils.isEmpty(seatIds)) {
            // An empty IN() list is not valid SQL - nothing was asked about, nothing is taken.
            return occupiedSeatIds;
        }
        @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
        SeatsBookingMapper seatsBookingMapper = sqlSession.getMapper(SeatsBookingMapper.class);
        SeatsBookingDynamicSqlSupport.SeatsBooking seatsBookingSqlSupport = new SeatsBookingDynamicSqlSupport.SeatsBooking();
        List<SeatsBooking> seatsBookings = seatsBookingMapper.selectByExample()
                .where(seatsBookingSqlSupport.seatId, SqlBuilder.isIn(seatIds))
                .and(seatsBookingSqlSupport.scheduledLiveShowId, SqlBuilder.isEqualTo(scheduledLiveShowId))
                .build().execute();
        long curTimeInMillisec = System.currentTimeMillis();
        if (!CollectionUtils.isEmpty(seatsBookings)) {
            for (SeatsBooking seatsBooking : seatsBookings) {
                if (BookingUtils.occupiesSeat(seatsBooking, curTimeInMillisec)) {
                    occupiedSeatIds.add(seatsBooking.getSeatId());
                }
            }
        }
        return occupiedSeatIds;
    }

    /**
     * Claims the given seats for the booking. An existing row may only be reused when it
     * belongs to a booking that has released the seat(failed, or a hold that ran out of
     * time); reassigning a row that still holds the seat would silently transfer another
     * customer's ticket, so that case is rejected instead.
     *
     * The caller commits only once every seat has been claimed, so throwing part way
     * through leaves nothing behind.
     * @param seatIds
     * @param showBooking
     * @param sqlSession
     * @return
     * @throws BookingException
     */
    public int insertSeatsForBooking(List<Integer> seatIds, ShowBooking showBooking, SqlSession sqlSession)
            throws BookingException {
        SeatsBookingMapper seatsBookingMapper = sqlSession.getMapper(SeatsBookingMapper.class);
        int entries = 0;
        long curTimeInMillisec = System.currentTimeMillis();
        for (Integer seatId : seatIds) {
            SeatsBooking seatsBooking = getSeatIdsForBooking(showBooking.getScheduledLiveShowId(), seatId);
            if (seatsBooking == null) {
                seatsBooking = new SeatsBooking();
                seatsBooking.setBookingId(showBooking.getBookingId());
                seatsBooking.setScheduledLiveShowId(showBooking.getScheduledLiveShowId());
                seatsBooking.setSeatId(seatId);
                seatsBooking.setSeatBookingStatus(StatusConstant.INITIATED);
                entries += seatsBookingMapper.insertSelective(seatsBooking);
            } else if (!BookingUtils.occupiesSeat(seatsBooking, curTimeInMillisec)) {
                seatsBooking.setBookingId(showBooking.getBookingId());
                seatsBooking.setSeatBookingStatus(StatusConstant.INITIATED);
                seatsBooking.setModifiedAt(new Date());
                entries += seatsBookingMapper.updateByPrimaryKeySelective(seatsBooking);
            } else {
                log.warn("Seat {} is already taken for scheduled live show {} by booking {}",
                        seatId, showBooking.getScheduledLiveShowId(), seatsBooking.getBookingId());
                throw new BookingException(ErrorMessages.SEAT_NOT_AVAILABLE);
            }
        }
        return entries;
    }

    public List<Integer> getSeatIdsForBooking(int bookingId) {
        @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
        SeatsBookingMapper seatsBookingMapper = sqlSession.getMapper(SeatsBookingMapper.class);
        List<SeatsBooking> seatsBookings = getSeatsForBookingId(bookingId, seatsBookingMapper);
        List<Integer> seatIds = new ArrayList<>();
        if (!CollectionUtils.isEmpty(seatsBookings)) {
            for (SeatsBooking seatsBooking : seatsBookings) {
                seatIds.add(seatsBooking.getSeatId());
            }
        }
        return seatIds;
    }

    public SeatsBooking getSeatIdsForBooking(int scheduledLiveShowId, int seatId) {
        @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
        SeatsBookingMapper seatsBookingMapper = sqlSession.getMapper(SeatsBookingMapper.class);
        SeatsBookingDynamicSqlSupport.SeatsBooking seatsBookingSqlSupport = new SeatsBookingDynamicSqlSupport.SeatsBooking();
        List<SeatsBooking> seatsBookings = seatsBookingMapper.selectByExample()
                .where(seatsBookingSqlSupport.scheduledLiveShowId, SqlBuilder.isEqualTo(scheduledLiveShowId))
                .and(seatsBookingSqlSupport.seatId, SqlBuilder.isEqualTo(seatId))
                .limit(1).build().execute();
        SeatsBooking seatsBooking = null;
        if (!CollectionUtils.isEmpty(seatsBookings)) {
            seatsBooking = seatsBookings.get(0);
        }
        return seatsBooking;
    }

    public List<SeatsBooking> getSeatsForBookingId(int bookingId, SeatsBookingMapper seatsBookingMapper) {
        SeatsBookingDynamicSqlSupport.SeatsBooking seatsBookingSqlSupport =
                new SeatsBookingDynamicSqlSupport.SeatsBooking();
        return seatsBookingMapper.selectByExample()
                .where(seatsBookingSqlSupport.bookingId, SqlBuilder.isEqualTo(bookingId))
                .build().execute();
    }

    public int updateSeatBookingStatus(int bookingId, int status, SqlSession sqlSession, Date curTime) {
        SeatsBookingMapper seatsBookingMapper = sqlSession.getMapper(SeatsBookingMapper.class);
        List<SeatsBooking> seatsBookings = getSeatsForBookingId(bookingId, seatsBookingMapper);
        int entries = 0;
        if (!CollectionUtils.isEmpty(seatsBookings)) {
            for (SeatsBooking seatsBooking : seatsBookings) {
                seatsBooking.setSeatBookingStatus(status);
                seatsBooking.setModifiedAt(curTime);
                entries += seatsBookingMapper.updateByPrimaryKey(seatsBooking);
            }
        }
        log.debug("Seats Booking Status Updated : {}", entries);
        return entries;
    }
}
