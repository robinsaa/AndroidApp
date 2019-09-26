package com.example.cuplogin;


public class ApiUtils {

    private ApiUtils() {}

    public static final String BASE_URL = "***REMOVED***sale/";

    public static APIService getAPIService() {

        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }
}
