package com.project.bookmyshow.services;

import com.project.bookmyshow.constants.StatusConstant;
import com.project.bookmyshow.db.dao.*;
import com.project.bookmyshow.db.mappers.*;
import com.project.bookmyshow.exceptions.BookingException;
import com.project.bookmyshow.models.SeatDetails;
import com.project.bookmyshow.models.request.BookingPaymentRequest;
import com.project.bookmyshow.models.request.BookingRequest;
import com.project.bookmyshow.models.response.*;
import com.project.bookmyshow.utils.ApplicationUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class BookingService {

    @Autowired
    private HallDAO hallDAO;
    @Autowired
    private ShowDAO showDAO;
    @Autowired
    private SeatDAO seatDAO;
    @Autowired
    private CinemaDAO cinemaDAO;
    @Autowired
    private LiveShowDAO liveShowDAO;
    @Autowired
    private SeatBookingDAO seatBookingDAO;
    @Autowired
    private ShowBookingDAO showBookingDAO;
    @Autowired
    private ScheduledLiveShowDAO scheduledLiveShowDAO;

    /**
     * This function initiates the booking and hold the
     * seats for sometime for completion of the payment
     * @param bookingRequest
     * @return
     * @throws BookingException
     */
    public synchronized BookingDetails initateBooking(BookingRequest bookingRequest)
            throws BookingException {

        ScheduledLiveShow scheduledLiveShow = scheduledLiveShowDAO.
                getScheduledLiveShowById(bookingRequest.getScheduledLiveShowId());
        ShowBooking showBooking = showBookingDAO.initiateBooking(bookingRequest, scheduledLiveShow.getTicketsPrice());
        return getBookingDetails(showBooking, scheduledLiveShow);
    }

    /**
     * This function takes makes the payment for the booking id
     * and confirms the booking
     * @param bookingPaymentRequest
     * @return
     * @throws BookingException
     */
    public synchronized BookingDetails finalizeBooking(BookingPaymentRequest bookingPaymentRequest)
            throws BookingException {
        BookingDetails bookingDetails;
        ShowBooking showBooking = showBookingDAO.getInitiatedBookingBy(bookingPaymentRequest);
        showBooking = showBookingDAO.updateBookingStatus(showBooking, StatusConstant.INPROGRESS);
        // Mocking Payment
        PaymentService.Status status = PaymentService.processPayment();
        switch (status) {
            case FAILED:
                showBooking = showBookingDAO.updateBookingStatus(showBooking, StatusConstant.FAILED);
                break;
            case SUCCESS:
                showBooking = showBookingDAO.updateBookingStatus(showBooking, StatusConstant.SUCCESS);
                break;
            case INPROGRESS:
                break;
        }
        bookingDetails = getBookingDetails(showBooking);
        return bookingDetails;
    }

    /**
     * This function takes in {@link ShowBooking#getBookingRefNo()}
     * and return the booking details
     * @param bookingRefNo
     * @return
     */
    public BookingDetails getBookingDetails(String bookingRefNo) {
        ShowBooking showBooking = showBookingDAO.getShowBookingBy(bookingRefNo);
        return getBookingDetails(showBooking);
    }

    /**
     * This function returns list of {@link BookingDetails} for the ticket
     * booked by the customer based on customerId
     * @param customerId
     * @return
     */
    public List<BookingDetails> getBookingDetails(int customerId) {
        List<ShowBooking> showBookings = showBookingDAO.getBookedShowByCustomerId(customerId);
        List<BookingDetails> bookingDetails = new ArrayList<>();
        if (!CollectionUtils.isEmpty(showBookings)) {
            for (ShowBooking showBooking : showBookings) {
                bookingDetails.add(getBookingDetails(showBooking));
            }
        }
        return bookingDetails;
    }

    /**
     * This function returns {@link BookingDetails} for the ticket
     * booked by the customer
     * @param showBooking
     * @return
     */
    private BookingDetails getBookingDetails(ShowBooking showBooking) {
        ScheduledLiveShow scheduledLiveShow = scheduledLiveShowDAO.
                getScheduledLiveShowById(showBooking.getScheduledLiveShowId());
        return getBookingDetails(showBooking, scheduledLiveShow);
    }

    /**
     * This function returns {@link BookingDetails} for the ticket
     * booked by the customer
     * @param showBooking
     * @param scheduledLiveShow
     * @return
     */
    private BookingDetails getBookingDetails(ShowBooking showBooking, ScheduledLiveShow scheduledLiveShow) {
        return BookingDetails.builder()
                .bookingRefNo(showBooking.getBookingRefNo())
                .bookingStatus(ApplicationUtils.getStatusString(showBooking.getStatusId()))
                .showDetail(getShowDetails(scheduledLiveShow, showBooking))
                .totalAmount(showBooking.getTotalBookingAmount())
                .build();
    }

    /**
     * This function returns the {@link ShowDetail} for the show
     * booked by the customer
     * @param scheduledLiveShow
     * @param showBooking
     * @return
     */
    private ShowDetail getShowDetails(ScheduledLiveShow scheduledLiveShow, ShowBooking showBooking) {
        LiveShow liveShow = liveShowDAO.getById(scheduledLiveShow.getLiveShowId());
        Show show = showDAO.getById(liveShow.getShowId());
        Hall hall = hallDAO.getHallById(liveShow.getHallId());
        Cinema cinema = cinemaDAO.getCinemayId(hall.getCinemaId());
        List<Integer> seatIdList = seatBookingDAO.getSeatIdsForBooking(showBooking.getBookingId());
        List<Seat> seatList = seatDAO.getSeatList(seatIdList);
        List<SeatDetails> seatDetailsList = new ArrayList<>();
        for (Seat seat : seatList) {
            seatDetailsList.add(
                    SeatDetails.builder()
                            .seatId(seat.getSeatId())
                            .seatCode(seat.getSeatCode())
                            .seatColLocation(seat.getSeatColLoc())
                            .seatRowLocation(seat.getSeatRowLoc())
                            .build()
            );
        }
        ScheduledTimingDetails scheduledTimingDetails = ScheduledTimingDetails.builder()
                .scheduledTimingId(scheduledLiveShow.getScheduledLiveShowId())
                .startTime(scheduledLiveShow.getShowStartTime())
                .endTime(scheduledLiveShow.getShowEndTime())
                .ticketAmount(scheduledLiveShow.getTicketsPrice())
                .seatDetailsList(seatDetailsList)
                .build();
        HallDetail hallDetail = HallDetail.builder()
                .hallId(hall.getHallId())
                .hallCode(hall.getHallCode())
                .scheduledTiming(scheduledTimingDetails)
                .build();
        CinemaDetail cinemaDetail = CinemaDetail.builder()
                .cinemaId(cinema.getCinemaId())
                .cinemaName(cinema.getCinemaName())
                .hallDetail(hallDetail)
                .build();
        return ShowDetail.builder()
                .showId(show.getShowId())
                .showName(show.getShowName())
                .showType(show.getShowType())
                .cinemaDetails(Arrays.asList(cinemaDetail))
                .build();
    }
}
