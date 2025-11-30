package com.kaif.jobms.job;

public class JobConstants {
    // Private constructor to prevent instantiation
    private JobConstants() {}

    public static final String ID_PATH = "/{jobId}";
    public static final String SORTED_PATH = "/sorted/{field}";
    public static final String LOCATION_PATH = "/location/{location}";
    public static final String SALARY_PATH = "/salary";
    public static final String SEARCH_PATH = "/search";
    public static final String PAGINATION_PATH = "/pagination/{page}/{pageSize}";
    public static final String STATS_LOCATION_PATH = "/stats/location-counts";
}