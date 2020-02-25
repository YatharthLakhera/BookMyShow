package com.project.bookmyshow.db.dao;

import com.project.bookmyshow.constants.ErrorMessages;
import com.project.bookmyshow.constants.StatusConstant;
import com.project.bookmyshow.db.ConnectionFactory;
import com.project.bookmyshow.db.mappers.ShowBooking;
import com.project.bookmyshow.db.mappers.ShowBookingDynamicSqlSupport;
import com.project.bookmyshow.db.mappers.ShowBookingMapper;
import com.project.bookmyshow.exceptions.BookingException;
import com.project.bookmyshow.models.request.BookingPaymentRequest;
import com.project.bookmyshow.models.request.BookingRequest;
import com.project.bookmyshow.utils.ApplicationUtils;
import com.project.bookmyshow.utils.BookingUtils;
import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.dynamic.sql.SqlBuilder;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Slf4j
@Repository
public class ShowBookingDAO {

    public List<Integer> getShowBookingForShow(int scheduledShowId) {
        @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
        ShowBookingMapper showBookingMapper = sqlSession.getMapper(ShowBookingMapper.class);
        List<Integer> showBookingIds = new ArrayList<>();
        for (ShowBooking showBooking : getShowBookingForShow(scheduledShowId, showBookingMapper)) {
            showBookingIds.add(showBooking.getBookingId());
        }
        return showBookingIds;
    }

    public List<ShowBooking> getShowBookingForShow(int scheduledShowId, ShowBookingMapper showBookingMapper) {
        ShowBookingDynamicSqlSupport.ShowBooking showBookingSqlSupport = new ShowBookingDynamicSqlSupport.ShowBooking();
        return showBookingMapper.selectByExample()
                .where(showBookingSqlSupport.scheduledLiveShowId, SqlBuilder.isEqualTo(scheduledShowId))
                .build().execute();
    }

    public ShowBooking initiateBooking(BookingRequest bookingRequest, double ticketPrice) throws BookingException {
        SeatBookingDAO seatBookingDAO = new SeatBookingDAO();
        if (seatBookingDAO.areSeatsAvailable(bookingRequest.getSeatIdList(), bookingRequest.getScheduledLiveShowId())) {
            @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
            ShowBooking showBooking = insertBookingEntry(bookingRequest, ticketPrice, sqlSession);
            int seatEntries = seatBookingDAO.insertSeatsForBooking(
                    bookingRequest.getSeatIdList(), showBooking, sqlSession);
            log.info("Seats Inserted : {}", seatEntries);
            sqlSession.commit();
            return showBooking;
        } else {
            throw new BookingException(ErrorMessages.SEAT_NOT_AVAILABLE);
        }
    }

    private ShowBooking insertBookingEntry(BookingRequest bookingRequest, double ticketPrice, SqlSession sqlSession) {
        ShowBooking showBooking = new ShowBooking();
        showBooking.setBookingRefNo(ApplicationUtils.getRandomString());
        showBooking.setConvenienceFee(0);
        showBooking.setCustomerId(bookingRequest.getCustomerId());
        showBooking.setScheduledLiveShowId(bookingRequest.getScheduledLiveShowId());
        showBooking.setTotalSeatsBooked(bookingRequest.getSeatIdList().size());
        showBooking.setTotalBookingAmount(ticketPrice * bookingRequest.getSeatIdList().size());
        showBooking.setStatusId(StatusConstant.INITIATED);
        ShowBookingMapper showBookingMapper = sqlSession.getMapper(ShowBookingMapper.class);
        showBookingMapper.insertSelective(showBooking);
        return showBooking;
    }

    public ShowBooking updateBookingStatus(@NonNull ShowBooking showBooking, int status) {
        @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
        ShowBookingMapper showBookingMapper = sqlSession.getMapper(ShowBookingMapper.class);
        Date curTime = new Date();
        showBooking.setStatusId(status);
        showBooking.setModifiedAt(curTime);
        showBookingMapper.updateByPrimaryKey(showBooking);
        SeatBookingDAO seatBookingDAO = new SeatBookingDAO();
        seatBookingDAO.updateSeatBookingStatus(showBooking.getBookingId(), status, sqlSession, curTime);
        sqlSession.commit();
        return showBooking;
    }

    public ShowBooking getInitiatedBookingBy(BookingPaymentRequest request) throws BookingException {
        @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
        ShowBookingMapper showBookingMapper = sqlSession.getMapper(ShowBookingMapper.class);
        ShowBookingDynamicSqlSupport.ShowBooking showBookingSqlSupport = new ShowBookingDynamicSqlSupport.ShowBooking();
        List<ShowBooking> showBookings = showBookingMapper.selectByExample()
                .where(showBookingSqlSupport.bookingRefNo, SqlBuilder.isEqualTo(request.getBookingRefNo()))
                .and(showBookingSqlSupport.customerId, SqlBuilder.isEqualTo(request.getCustomerId()))
                .and(showBookingSqlSupport.totalBookingAmount, SqlBuilder.isEqualTo(request.getTicketAmount()))
                .limit(1).build().execute();
        if (!CollectionUtils.isEmpty(showBookings)) {
            if (BookingUtils.isBookingSessionOnHold(showBookings.get(0))) {
                return showBookings.get(0);
            } else {
                throw new BookingException(ErrorMessages.BOOKING_SESSION_EXPIRED);
            }
        } else {
            throw new BookingException(ErrorMessages.NO_BOOKING_FOUND);
        }
    }

    public ShowBooking getShowBookingBy(String bookingRefNo) {
        @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
        ShowBookingMapper showBookingMapper = sqlSession.getMapper(ShowBookingMapper.class);
        ShowBookingDynamicSqlSupport.ShowBooking showBookingSqlSupport = new ShowBookingDynamicSqlSupport.ShowBooking();
        List<ShowBooking> showBookings = showBookingMapper.selectByExample()
                .where(showBookingSqlSupport.bookingRefNo, SqlBuilder.isEqualTo(bookingRefNo))
                .and(showBookingSqlSupport.statusId, SqlBuilder.isIn(Arrays.asList(StatusConstant.SUCCESS, StatusConstant.INPROGRESS)))
                .limit(1).build().execute();
        ShowBooking showBooking = null;
        if (!CollectionUtils.isEmpty(showBookings)) {
            showBooking = showBookings.get(0);
        }
        return showBooking;
    }

    public List<ShowBooking> getBookedShowByCustomerId(int customerId) {
        @Cleanup SqlSession sqlSession = ConnectionFactory.INSTANCE.getSqlSession();
        ShowBookingMapper showBookingMapper = sqlSession.getMapper(ShowBookingMapper.class);
        ShowBookingDynamicSqlSupport.ShowBooking showBookingSqlSupport = new ShowBookingDynamicSqlSupport.ShowBooking();
        return showBookingMapper.selectByExample()
                .where(showBookingSqlSupport.customerId, SqlBuilder.isEqualTo(customerId))
                .and(showBookingSqlSupport.statusId, SqlBuilder.isIn(Arrays.asList(StatusConstant.SUCCESS, StatusConstant.INPROGRESS)))
                .build().execute();
    }
}
