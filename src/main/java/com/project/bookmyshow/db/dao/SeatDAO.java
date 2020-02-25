package com.project.bookmyshow.db.dao;

import com.project.bookmyshow.db.ConnectionFactory;
import com.project.bookmyshow.db.mappers.*;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.dynamic.sql.SqlBuilder;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Repository
public class SeatDAO {

    public int createSeatsForHall(Hall hall) {

        @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
        SeatMapper seatMapper = sqlSession.getMapper(SeatMapper.class);
        List<Seat> seatList = getAllSeatList(hall.getHallId(), seatMapper);
        int entries = 0;
        if (CollectionUtils.isEmpty(seatList)) {
            int index = 0;
            for (int r = 0; r < hall.getHallRowCount(); r++) {
                for (int c = 0; c < hall.getHallColCount(); c++) {
                    Seat seat = new Seat();
                    seat.setHallId(hall.getHallId());
                    seat.setSeatCode(hall.getHallCode() + index);
                    seat.setSeatColLoc(c);
                    seat.setSeatRowLoc(r);
                    entries += insert(seat, seatMapper);
                    log.debug("Seat Inserted : {}", ToStringBuilder.reflectionToString(seat));
                    index++;
                }
            }
            sqlSession.commit();
        }
        return entries;
    }

    public List<Seat> getAllSeatList(int hallId) {
        @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
        SeatMapper seatMapper = sqlSession.getMapper(SeatMapper.class);
        return getAllSeatList(hallId, seatMapper);
    }

    public List<Seat> getAllSeatList(int hallId, SeatMapper seatMapper) {
        SeatDynamicSqlSupport.Seat seatSqlSupport = new SeatDynamicSqlSupport.Seat();
        return seatMapper.selectByExample()
                .where(seatSqlSupport.hallId, SqlBuilder.isEqualTo(hallId))
                .build().execute();
    }

    public List<Seat> getAvailableSeatList(int hallId, int scheduledShowId) {
        @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
        SeatMapper seatMapper = sqlSession.getMapper(SeatMapper.class);
        return getAvailableSeatList(hallId, scheduledShowId, seatMapper);
    }

    public List<Seat> getAvailableSeatList(int hallId, int scheduledShowId, SeatMapper seatMapper) {
        ShowBookingDAO showBookingDAO = new ShowBookingDAO();
        List<Integer> showBookingIds = showBookingDAO.getShowBookingForShow(scheduledShowId);
        SeatBookingDAO seatBookingDAO = new SeatBookingDAO();
        Set<Integer> acquiredSeatIds = seatBookingDAO.getBookedSeats(showBookingIds);
        List<Seat> availableSeats = new ArrayList<>();
        for (Seat seat : getAllSeatList(hallId, seatMapper)) {
            if (!acquiredSeatIds.contains(seat.getSeatId())) {
                availableSeats.add(seat);
            }
        }
        return availableSeats;
    }

    private int insert(Seat seat) {
        @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
        SeatMapper seatMapper = sqlSession.getMapper(SeatMapper.class);
        int entries = insert(seat, seatMapper);
        sqlSession.commit();
        log.debug("Seat Inserted : {}", ToStringBuilder.reflectionToString(seat));
        return entries;
    }

    public List<Seat> getSeatList(List<Integer> seatIds) {
        @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
        SeatMapper seatMapper = sqlSession.getMapper(SeatMapper.class);
        SeatDynamicSqlSupport.Seat seatSqlSupport = new SeatDynamicSqlSupport.Seat();
        return seatMapper.selectByExample()
                .where(seatSqlSupport.seatId, SqlBuilder.isIn(seatIds))
                .build().execute();
    }

    private int insert(Seat seat, SeatMapper seatMapper) {
        return seatMapper.insertSelective(seat);
    }
}
