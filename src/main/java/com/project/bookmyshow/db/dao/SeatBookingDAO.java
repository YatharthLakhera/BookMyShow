package com.project.bookmyshow.db.dao;

import com.project.bookmyshow.constants.StatusConstant;
import com.project.bookmyshow.db.ConnectionFactory;
import com.project.bookmyshow.db.mappers.SeatsBooking;
import com.project.bookmyshow.db.mappers.SeatsBookingDynamicSqlSupport;
import com.project.bookmyshow.db.mappers.SeatsBookingMapper;
import com.project.bookmyshow.db.mappers.ShowBooking;
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
        long curTimeInMillisec = System.currentTimeMillis();
        for (SeatsBooking seatsBooking : getBookedSeats(showBookingIds, seatsBookingMapper)) {
            switch (seatsBooking.getSeatBookingStatus()) {
                case StatusConstant.INITIATED:
                    if (BookingUtils.isSeatOnHold(seatsBooking, curTimeInMillisec)) {
                        bookedSeatIds.add(seatsBooking.getSeatId());
                    }
                    break;
                case StatusConstant.SUCCESS:
                case StatusConstant.INPROGRESS:
                    bookedSeatIds.add(seatsBooking.getSeatId());
                    break;
                case StatusConstant.FAILED:
                    break;
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
        List<Integer> seatStatus = Arrays.asList(StatusConstant.INPROGRESS, StatusConstant.SUCCESS);
        @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
        SeatsBookingMapper seatsBookingMapper = sqlSession.getMapper(SeatsBookingMapper.class);
        SeatsBookingDynamicSqlSupport.SeatsBooking seatsBookingSqlSupport = new SeatsBookingDynamicSqlSupport.SeatsBooking();
        List<SeatsBooking> seatsBookings = seatsBookingMapper.selectByExample()
                .where(seatsBookingSqlSupport.seatId, SqlBuilder.isIn(seatIds))
                .and(seatsBookingSqlSupport.scheduledLiveShowId, SqlBuilder.isEqualTo(scheduledLiveShowId))
                .and(seatsBookingSqlSupport.seatBookingStatus, SqlBuilder.isNotIn(seatStatus))
                .build().execute();
        boolean areSeatsAvailable = true;
        if (!CollectionUtils.isEmpty(seatsBookings)) {
            for (SeatsBooking seatsBooking : seatsBookings) {
                if (BookingUtils.isSeatOnHold(seatsBooking)) {
                    areSeatsAvailable = false;
                    break;
                }
            }
        }
        return areSeatsAvailable;
    }

    public int insertSeatsForBooking(List<Integer> seatIds, ShowBooking showBooking, SqlSession sqlSession) {
        SeatsBookingMapper seatsBookingMapper = sqlSession.getMapper(SeatsBookingMapper.class);
        int entries = 0;
        for (Integer seatId : seatIds) {
            SeatsBooking seatsBooking = getSeatIdsForBooking(showBooking.getScheduledLiveShowId(), seatId);
            if (seatsBooking == null) {
                seatsBooking = new SeatsBooking();
                seatsBooking.setBookingId(showBooking.getBookingId());
                seatsBooking.setScheduledLiveShowId(showBooking.getScheduledLiveShowId());
                seatsBooking.setSeatId(seatId);
                seatsBooking.setSeatBookingStatus(StatusConstant.INITIATED);
                entries += seatsBookingMapper.insertSelective(seatsBooking);
            } else {
                seatsBooking.setBookingId(showBooking.getBookingId());
                seatsBooking.setSeatBookingStatus(StatusConstant.INITIATED);
                seatsBooking.setModifiedAt(new Date());
                entries += seatsBookingMapper.updateByPrimaryKeySelective(seatsBooking);
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
