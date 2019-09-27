package com.example.cuplogin.Api;


public class ApiUtils {

    private ApiUtils() {}

    public static final String BASE_URL = "***REMOVED***";

    public static APIService getAPIService() {

        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }
}
