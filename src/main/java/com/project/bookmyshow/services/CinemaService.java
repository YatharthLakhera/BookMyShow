package com.project.bookmyshow.services;

import com.project.bookmyshow.db.dao.*;
import com.project.bookmyshow.db.mappers.*;
import com.project.bookmyshow.enums.SeatStatus;
import com.project.bookmyshow.models.SeatDetails;
import com.project.bookmyshow.models.response.CinemaDetail;
import com.project.bookmyshow.models.response.HallDetail;
import com.project.bookmyshow.models.response.ScheduledTimingDetails;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Slf4j
@Service
public class CinemaService {

    @Autowired
    private HallDAO hallDAO;
    @Autowired
    private SeatDAO seatDAO;
    @Autowired
    private CinemaDAO cinemaDAO;
    @Autowired
    private LiveShowDAO liveShowDAO;
    @Autowired
    private ScheduledLiveShowDAO scheduledLiveShowDAO;

    public List<CinemaDetail> getCinemaList(boolean shouldShowSeatDetails) {
        List<CinemaDetail> cinemaDetailList = new ArrayList<>();
        List<Cinema> cinemaList = cinemaDAO.getAllCinema();
        if (!CollectionUtils.isEmpty(cinemaList)) {
            for (Cinema cinema : cinemaList) {
                cinemaDetailList.add(getCinemaDetails(cinema, shouldShowSeatDetails));
            }
        }
        return cinemaDetailList;
    }

    public CinemaDetail getCinemaDetails(int cinemaId, boolean shouldShowSeatDetails) {
        return getCinemaDetails(cinemaDAO.getCinemayId(cinemaId), shouldShowSeatDetails);
    }

    private CinemaDetail getCinemaDetails(Cinema cinema, boolean shouldShowSeatDetails) {
        CinemaDetail.CinemaDetailBuilder cinemaDetailBuilder = CinemaDetail.builder()
                .cinemaId(cinema.getCinemaId())
                .cinemaName(cinema.getCinemaName());
        List<Hall> hallList = hallDAO.getHallsByCinemaId(cinema.getCinemaId());
        if (!CollectionUtils.isEmpty(hallList)) {
            for (Hall hall : hallList) {
                cinemaDetailBuilder.hallDetail(getHallDetail(hall, shouldShowSeatDetails));
            }
        }
        return cinemaDetailBuilder.build();
    }

    public HallDetail getHallDetail(int cinemaId, int hallId, boolean shouldShowSeatDetails) {
        HallDetail hallDetail = null;
        Hall hall = hallDAO.getHallBy(cinemaId, hallId);
        if (hall != null) {
            hallDetail = getHallDetail(hallId, shouldShowSeatDetails);
        }
        return hallDetail;
    }

    public HallDetail getHallDetail(int hallId, boolean shouldShowSeatDetails) {
        return getHallDetail(hallDAO.getHallById(hallId), shouldShowSeatDetails);
    }

    public HallDetail getHallDetail(Hall hall, boolean shouldShowSeatDetails) {
        return HallDetail.builder().hallId(hall.getHallId())
                .hallCode(hall.getHallCode())
                .hallRowCount(hall.getHallRowCount())
                .hallColCount(hall.getHallColCount())
                .scheduledTimings(getScheduledLiveShowByHallId(hall.getHallId(), shouldShowSeatDetails))
                .build();
    }

    public List<ScheduledTimingDetails> getScheduledLiveShowByHallId(int hallId, boolean shouldShowSeatDetails) {
        List<ScheduledTimingDetails> scheduledLiveShowList = new ArrayList<>();
        List<LiveShow> liveShowList = liveShowDAO.getLiveShowByHallId(hallId);
        if (!CollectionUtils.isEmpty(liveShowList)) {
            for(LiveShow liveShow : liveShowList) {
                List<ScheduledLiveShow> scheduledLiveShows = scheduledLiveShowDAO.
                        getScheduledLiveShowByLiveShowId(liveShow.getLiveShowId());
                if (!CollectionUtils.isEmpty(scheduledLiveShows)) {
                    scheduledLiveShowList.addAll(getScheduledTimings(scheduledLiveShows, hallId, shouldShowSeatDetails));
                }
            }
        }
        return scheduledLiveShowList;
    }

    public List<ScheduledTimingDetails> getScheduledTimings(@NonNull List<ScheduledLiveShow> scheduledLiveShows,
                                                                   int hallId, boolean shouldShowSeatDetails) {
        List<ScheduledTimingDetails> scheduledTimings = new ArrayList<>();
        for (ScheduledLiveShow scheduledLiveShow : scheduledLiveShows) {
            scheduledTimings.add(getScheduledTimingDetails(scheduledLiveShow, hallId, shouldShowSeatDetails));
        }
        return scheduledTimings;
    }

    /**
     * This function returns list of all {@link CinemaDetail}
     * in which the show will be available
     * @param showId
     * @return
     */
    public List<CinemaDetail> getCinemaDetailsForShow(int showId, boolean shouldShowSeatDetails) {
        List<LiveShow> liveShowList = liveShowDAO.getAllLiveShowBy(showId);
        return new ArrayList<>(getCinemaDetailsForShow(liveShowList, shouldShowSeatDetails));
    }

    /**
     * This function returns list of all {@link CinemaDetail}
     * in which the show will be available
     * @param liveShowList
     * @return
     */
    private Collection<CinemaDetail> getCinemaDetailsForShow(List<LiveShow> liveShowList, boolean shouldShowSeatDetails) {
        Map<Integer, CinemaDetail> cinemaDetailHashMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(liveShowList)) {
            for (LiveShow liveShow : liveShowList) {
                CinemaDetail cinemaDetail = getCinemaDetailsByShow(liveShow, shouldShowSeatDetails);
                if (cinemaDetailHashMap.containsKey(cinemaDetail.getCinemaId())) {
                    CinemaDetail cachedCinemaDetails = cinemaDetailHashMap.get(cinemaDetail.getCinemaId());
                    for (HallDetail hallDetail : cinemaDetail.getHallDetails()) {
                        for (HallDetail newHallDetail : cachedCinemaDetails.getHallDetails()) {
                            if (hallDetail.getHallId() != newHallDetail.getHallId()) {
                                cachedCinemaDetails.addHallDetail(newHallDetail);
                            }
                        }
                    }
                    cinemaDetailHashMap.put(cinemaDetail.getCinemaId(), cachedCinemaDetails);
                } else {
                    cinemaDetailHashMap.put(cinemaDetail.getCinemaId(), cinemaDetail);
                }
            }
        }
        return cinemaDetailHashMap.values();
    }

    private CinemaDetail getCinemaDetailsByShow(@NonNull LiveShow liveShow, boolean shouldShowSeatDetails) {
        CinemaDetail cinemaDetail = null;
        Hall hall = hallDAO.getHallById(liveShow.getHallId());
        List<ScheduledLiveShow> scheduledLiveShowList =
                scheduledLiveShowDAO.getScheduledLiveShowByLiveShowId(liveShow.getLiveShowId());
        if (!CollectionUtils.isEmpty(scheduledLiveShowList) && hall != null) {
            Cinema cinema = cinemaDAO.getCinemayId(hall.getCinemaId());
            cinemaDetail = CinemaDetail.builder()
                    .cinemaId(cinema.getCinemaId())
                    .cinemaName(cinema.getCinemaName())
                    .hallDetail(getHallDetails(hall, scheduledLiveShowList, shouldShowSeatDetails))
                    .build();
        }
        return cinemaDetail;
    }

    private HallDetail getHallDetails(@NonNull Hall hall, @NonNull List<ScheduledLiveShow> scheduledLiveShowList, @NonNull boolean shouldShowSeatDetails) {
        HallDetail.HallDetailBuilder hallDetailBuilder = HallDetail.builder();
        hallDetailBuilder.hallId(hall.getHallId())
                .hallCode(hall.getHallCode())
                .hallRowCount(hall.getHallRowCount())
                .hallColCount(hall.getHallColCount());
        for (ScheduledLiveShow scheduledLiveShow : scheduledLiveShowList) {
            ScheduledTimingDetails scheduledTimings = getScheduledTimingDetails(scheduledLiveShow, hall.getHallId(), shouldShowSeatDetails);
            hallDetailBuilder.scheduledTiming(scheduledTimings);
        }
        return hallDetailBuilder.build();
    }

    private ScheduledTimingDetails getScheduledTimingDetails(ScheduledLiveShow scheduledLiveShow, int hallId, boolean shouldShowSeatDetails) {
        ScheduledTimingDetails.ScheduledTimingDetailsBuilder scheduledTimingDetailsBuilder = ScheduledTimingDetails.builder()
                .scheduledTimingId(scheduledLiveShow.getScheduledLiveShowId())
                .startTime(scheduledLiveShow.getShowStartTime())
                .endTime(scheduledLiveShow.getShowEndTime())
                .ticketAmount(scheduledLiveShow.getTicketsPrice());
        if (shouldShowSeatDetails) {
            scheduledTimingDetailsBuilder.seatDetailsList(getSeatDetailsList(scheduledLiveShow, hallId));
        }
        return scheduledTimingDetailsBuilder.build();
    }

    public List<SeatDetails> getSeatDetailsList(ScheduledLiveShow scheduledLiveShow, int hallId) {
        List<Seat> availableSeatList = seatDAO.getAvailableSeatList(hallId, scheduledLiveShow.getLiveShowId());
        List<SeatDetails> seatDetailsList = new ArrayList<>();
        for (Seat seat : availableSeatList) {
            seatDetailsList.add(
                    SeatDetails.builder()
                            .seatCode(seat.getSeatCode())
                            .seatId(seat.getSeatId())
                            .seatColLocation(seat.getSeatColLoc())
                            .seatRowLocation(seat.getSeatRowLoc())
                            .seatStatus(SeatStatus.AVAILABLE)
                            .build()
            );
        }
        return seatDetailsList;
    }
}