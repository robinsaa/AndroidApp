package com.example.cuplogin.Api;


import com.example.cuplogin.BuildConfig;

public class ApiUtils {

    private ApiUtils() {}

    public static final String BASE_URL = "***REMOVED***";

    public static APIService getAPIService() {

        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }
}
