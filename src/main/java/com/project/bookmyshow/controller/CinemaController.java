package com.project.bookmyshow.controller;

import com.project.bookmyshow.constants.ErrorMessages;
import com.project.bookmyshow.db.mappers.Hall;
import com.project.bookmyshow.db.mappers.ScheduledLiveShow;
import com.project.bookmyshow.models.APIResponse;
import com.project.bookmyshow.models.response.CinemaDetail;
import com.project.bookmyshow.models.response.ErrorResponse;
import com.project.bookmyshow.models.response.HallDetail;
import com.project.bookmyshow.models.response.ScheduledTimingDetails;
import com.project.bookmyshow.services.CinemaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/cinemas")
public class CinemaController {

    @Autowired
    private CinemaService cinemaService;

    /**
     * This API returns the whole list of the cinema available
     * with their details(ie. {@link CinemaDetail}
     * @return
     */
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "", method = RequestMethod.GET)
    public List<CinemaDetail> getCinemaList() {
        return cinemaService.getCinemaList(false);
    }

    /**
     * This API returns the cinema details (ie. {@link CinemaDetail}
     * based on the input {@link CinemaDetail#getCinemaId()}
     * @param cinemaId
     * @return
     */
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/{cinemaId}", method = RequestMethod.GET)
    public CinemaDetail getCinemaDetails(@PathVariable(name = "cinemaId") int cinemaId) {
        return cinemaService.getCinemaDetails(cinemaId, false);
    }

    /**
     * This API returns list of all the hall with details (ie. {@link HallDetail}
     * present in a cinema based on the input {@link CinemaDetail#getCinemaId()}
     * @param cinemaId
     * @return
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/{cinemaId}/halls", method = RequestMethod.GET)
    public ResponseEntity getHallDetails(@PathVariable(name = "cinemaId") int cinemaId) {
        CinemaDetail cinemaDetail = cinemaService.getCinemaDetails(cinemaId, false);
        ResponseEntity responseEntity;
        if (cinemaDetail != null && !CollectionUtils.isEmpty(cinemaDetail.getHallDetails())) {
            responseEntity = new ResponseEntity(cinemaDetail.getHallDetails(), HttpStatus.OK);
        } else {
            responseEntity = new ResponseEntity(new ErrorResponse(ErrorMessages.NO_HALL_FOUND), HttpStatus.OK);
        }
        return responseEntity;
    }

    /**
     * This API returns hall details (ie. {@link HallDetail} by {@link Hall#getHallId()}
     * if present in a cinema based on the input {@link CinemaDetail#getCinemaId()}
     * @param cinemaId
     * @param hallId
     * @return
     */
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/{cinemaId}/halls/{hallId}", method = RequestMethod.GET)
    public APIResponse getHallDetails(@PathVariable(name = "cinemaId") int cinemaId,
                                           @PathVariable(name = "hallId") int hallId) {
        APIResponse apiResponse;
        HallDetail hallDetail = cinemaService.getHallDetail(cinemaId, hallId, false);
        if (hallDetail != null) {
            apiResponse = hallDetail;
        } else {
            apiResponse = new ErrorResponse(ErrorMessages.NO_HALL_FOUND);
        }
        return apiResponse;
    }

    /**
     * This API returns all {@link ScheduledTimingDetails} that are available in the
     * hall by {@link Hall#getHallId()} and if the hall is present in a cinema based
     * on the input {@link CinemaDetail#getCinemaId()}
     * @param cinemaId
     * @param hallId
     * @return
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/{cinemaId}/halls/{hallId}/timings", method = RequestMethod.GET)
    public ResponseEntity getScheduledTimeDetails(@PathVariable(name = "cinemaId") int cinemaId,
                                      @PathVariable(name = "hallId") int hallId) {
        ResponseEntity responseEntity;
        HallDetail hallDetail = cinemaService.getHallDetail(cinemaId, hallId, false);
        if (hallDetail != null) {
            responseEntity = new ResponseEntity(cinemaService.getScheduledLiveShowByHallId(hallId, false), HttpStatus.OK);
        } else {
            responseEntity = new ResponseEntity(new ErrorResponse(ErrorMessages.NO_SHOW_FOR_TIMING), HttpStatus.OK);
        }
        return responseEntity;
    }

    /**
     * This API returns {@link ScheduledTimingDetails} by {@link ScheduledLiveShow#getLiveShowId()}
     * that is available in the hall by {@link Hall#getHallId()} and if the hall is present in a
     * cinema based on the input {@link CinemaDetail#getCinemaId()}
     * @param cinemaId
     * @param hallId
     * @param timingId
     * @return
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/{cinemaId}/halls/{hallId}/timings/{timingId}", method = RequestMethod.GET)
    public ResponseEntity getScheduledTimeDetailsById(@PathVariable(name = "cinemaId") int cinemaId,
                                                      @PathVariable(name = "hallId") int hallId,
                                                      @PathVariable(name = "timingId") int timingId) {
        ResponseEntity responseEntity;
        HallDetail hallDetail = cinemaService.getHallDetail(cinemaId, hallId, false);
        if (hallDetail != null) {
            responseEntity = new ResponseEntity(cinemaService.getScheduledLiveShowByHallId(hallId, true), HttpStatus.OK);
        } else {
            responseEntity = new ResponseEntity(new ErrorResponse(ErrorMessages.GENERAL_ERROR), HttpStatus.OK);
        }
        return responseEntity;
    }

    // TODO : Addition of cinema and halls
//    @RequestMapping(value = "/add/{hallId}", method = RequestMethod.GET)
//    public String addCinema(@PathVariable("hallId") int hallId) {
//        SeatDAO seatDAO = new SeatDAO();
//        seatDAO.createSeatsForHall(new HallDAO().getHallById(hallId));
//        return null;
//    }
}
