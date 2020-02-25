package com.project.bookmyshow.utils;

import com.project.bookmyshow.constants.StatusConstant;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import java.text.ParseException;
import java.util.Date;

@Slf4j
public class ApplicationUtils {

    private static final int DEFAULT_RANDOM_STRING_LENGTH = 20;

    public static Date getParsedDate(String rawDate, String parsingFormat) {
        FastDateFormat fastDateFormat = FastDateFormat.getInstance(parsingFormat);
        Date parsedDate = null;
        try {
            parsedDate = fastDateFormat.parse(rawDate);
        } catch (ParseException e) {
            log.error("Unable to parse date : {} using format {}", rawDate, parsedDate);
        }
        return parsedDate;
    }

    public static Date utcToIST(@NonNull Date utcDate) {
        Date istDate = DateUtils.addHours(utcDate, -5);
        istDate = DateUtils.addMinutes(istDate, -30);
        log.info("UTC Date : {}, IST Date : {}", utcDate, istDate);
        return istDate;
    }

    public static String getRandomString() {
        return getRandomString(DEFAULT_RANDOM_STRING_LENGTH);
    }

    public static String getRandomString(int length) {
        return RandomStringUtils.randomAlphabetic(length);
    }

    public static String getStatusString(int statusId) {
        String status = null;
        switch (statusId) {
            case StatusConstant.SUCCESS:
                status = StatusConstant.getName(StatusConstant.SUCCESS);
                break;
            case StatusConstant.FAILED:
                status = StatusConstant.getName(StatusConstant.FAILED);
                break;
            case StatusConstant.INPROGRESS:
                status = StatusConstant.getName(StatusConstant.INPROGRESS);
                break;
            case StatusConstant.INITIATED:
                status = StatusConstant.getName(StatusConstant.INITIATED);
                break;

        }
        return status;
    }
}
