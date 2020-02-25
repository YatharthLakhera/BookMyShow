package com.project.bookmyshow.services;

import com.project.bookmyshow.constants.AppConstants;
import com.project.bookmyshow.db.dao.LiveShowDAO;
import com.project.bookmyshow.db.dao.ScheduledLiveShowDAO;
import com.project.bookmyshow.db.dao.ShowDAO;
import com.project.bookmyshow.db.mappers.*;
import com.project.bookmyshow.models.request.AddShowRequest;
import com.project.bookmyshow.models.response.ShowDetail;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class ShowService {

    @Autowired
    private CinemaService cinemaService;

    public List<ShowDetail> getShowDetailsList(boolean isLive) {
        List<ShowDetail> showDetails = new ArrayList<>();
        ShowDAO showDAO = new ShowDAO();
        List<Show> showList = showDAO.getAllShow();
        if (!CollectionUtils.isEmpty(showList)) {
            LiveShowDAO liveShowDAO = new LiveShowDAO();
            Date currentDate = DateUtils.addMonths(new Date(), -AppConstants.DISPLAY_SHOW_FOR_MONTHS);
            for (Show show : showList) {
                ShowDetail showDetail = getShowDetails(show);
                if (isLive && liveShowDAO.isShowLive(show.getShowId())) {
                    showDetails.add(showDetail);
                } else if (!isLive && currentDate.before(show.getCreatedAt())) {
                    showDetails.add(showDetail);
                }
            }
        }

        return showDetails;
    }

    public ShowDetail getShowDetails(int showId) {
        ShowDAO showDAO = new ShowDAO();
        Show show = showDAO.getById(showId);
        return getShowDetails(show);
    }

    private ShowDetail getShowDetails(@NonNull Show show) {
        return ShowDetail.builder()
                .showId(show.getShowId())
                .showName(show.getShowName())
                .showType(show.getShowType())
                .cinemaDetails(cinemaService.getCinemaDetailsForShow(show.getShowId(), false))
                .build();
    }

    /**
     * This function add the show to DB for all the tables i.e
     * {@link Show}, {@link LiveShow} and {@link ScheduledLiveShow}
     * @param addShowRequest
     * @return
     */
    public ShowDetail addNewShowToDB(AddShowRequest addShowRequest) {
        log.info(ToStringBuilder.reflectionToString(addShowRequest));
        Show show = addShowToDB(addShowRequest);
        LiveShow liveShow = addLiveShowToDB(show.getShowId(), addShowRequest.getHallId());
        ScheduledLiveShow scheduledLiveShow = addScheduledLiveShowToDB(liveShow.getLiveShowId(), addShowRequest);
        return getShowDetails(show);
    }

    private Show addShowToDB(AddShowRequest addShowRequest) {
        ShowDAO showDAO = new ShowDAO();
        return showDAO.insertOrUpdateShow(addShowRequest.getShowType(), addShowRequest.getShowName());
    }

    private LiveShow addLiveShowToDB(int showId, int hallId) {
        LiveShowDAO liveShowDAO = new LiveShowDAO();
        return liveShowDAO.insertOrUpdateLiveShow(showId, hallId);
    }

    private ScheduledLiveShow addScheduledLiveShowToDB(int liveShowId, AddShowRequest addShowRequest) {
        ScheduledLiveShowDAO scheduledLiveShowDAO = new ScheduledLiveShowDAO();
        return scheduledLiveShowDAO.insertOrUpdateScheduledLiveShowBy(
                liveShowId,
                addShowRequest.getStartTime(),
                addShowRequest.getEndTime(),
                addShowRequest.getTicketPrice()
        );
    }
}
