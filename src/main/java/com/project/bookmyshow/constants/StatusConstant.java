package com.project.bookmyshow.constants;

import java.util.HashMap;
import java.util.Map;

public class StatusConstant {
    public static final int SUCCESS = 1;
    public static final int FAILED = 2;
    public static final int INPROGRESS = 3;
    public static final int INITIATED = 4;
    private static final Map<Integer, String> StatusMap = new HashMap<Integer, String>(){{
        put(SUCCESS, "SUCCESS");
        put(FAILED, "FAILED");
        put(INPROGRESS, "INPROGRESS");
        put(INITIATED, "INITIATED");
    }};

    public static String getName(int statusId) {
        return StatusMap.get(statusId);
    }
}
