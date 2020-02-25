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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class BookingService {

    /**
     * This function initiates the booking and hold the
     * seats for sometime for completion of the payment
     * @param bookingRequest
     * @return
     * @throws BookingException
     */
    public synchronized BookingDetails initateBooking(BookingRequest bookingRequest)
            throws BookingException {

        ScheduledLiveShowDAO scheduledLiveShowDAO = new ScheduledLiveShowDAO();
        ScheduledLiveShow scheduledLiveShow = scheduledLiveShowDAO.
                getScheduledLiveShowById(bookingRequest.getScheduledLiveShowId());
        ShowBookingDAO showBookingDAO = new ShowBookingDAO();
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
        ShowBookingDAO showBookingDAO = new ShowBookingDAO();
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
        ShowBookingDAO showBookingDAO = new ShowBookingDAO();
        ShowBooking showBooking = showBookingDAO.getShowBookingBy(bookingRefNo);
        return getBookingDetails(showBooking);
    }

    /**
     * This function returns {@link BookingDetails} for the ticket
     * booked by the customer
     * @param showBooking
     * @return
     */
    public BookingDetails getBookingDetails(ShowBooking showBooking) {
        ScheduledLiveShowDAO scheduledLiveShowDAO = new ScheduledLiveShowDAO();
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
    public BookingDetails getBookingDetails(ShowBooking showBooking, ScheduledLiveShow scheduledLiveShow) {
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
        LiveShow liveShow = new LiveShowDAO().getById(scheduledLiveShow.getLiveShowId());
        Show show = new ShowDAO().getById(liveShow.getShowId());
        Hall hall = new HallDAO().getHallById(liveShow.getHallId());
        Cinema cinema = new CinemaDAO().getCinemayId(hall.getCinemaId());
        List<Integer> seatIdList = new SeatBookingDAO().getSeatIdsForBooking(showBooking.getBookingId());
        List<Seat> seatList = new SeatDAO().getSeatList(seatIdList);
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
        ShowDetail showDetail = ShowDetail.builder()
                .showId(show.getShowId())
                .showName(show.getShowName())
                .showType(show.getShowType())
                .cinemaDetails(Arrays.asList(cinemaDetail))
                .build();
        return showDetail;
    }
}
