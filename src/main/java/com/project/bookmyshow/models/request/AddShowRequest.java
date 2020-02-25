package com.project.bookmyshow.models.request;

import com.project.bookmyshow.enums.ShowType;
import com.project.bookmyshow.utils.ApplicationUtils;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class AddShowRequest {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private int customerId;
    private String showName;
    private ShowType showType;
    private int hallId;
    private String startTime;
    private String endTime;
    private double ticketPrice;

    public Date getStartTime() {
        return ApplicationUtils.getParsedDate(this.startTime, DATE_FORMAT);
    }

    public Date getEndTime() {
        return ApplicationUtils.getParsedDate(this.endTime, DATE_FORMAT);
    }
}
