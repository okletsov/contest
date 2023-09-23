package com.sapfir.apiUtils;

import com.sapfir.helpers.DateTimeOperations;
import com.sapfir.helpers.Properties;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.HashMap;

public class ApiHelpers {

    private String usePremium;
    private String bookieHash;
    private HashMap<String, String> requestHeaders;

    public ApiHelpers(String usePremium, String bookieHash, HashMap<String, String> requestHeaders) {
        this.usePremium = usePremium;
        this.bookieHash = bookieHash;
        this.requestHeaders = requestHeaders;
    }

    public ApiHelpers() {
        // to be accessed in a Sandbox class
    }

    public String generatePredictionsRequestUrl(String userId) {

        Properties props = new Properties();
        String siteUrl = props.getSiteUrl();

        DateTimeOperations dateTimeOperations = new DateTimeOperations();
        String unixTimestamp = dateTimeOperations.getUnixTimestamp();

        return siteUrl + "ajax-communityFeed/profile/" + userId + "/" + unixTimestamp + "/";
    }

    public String makeApiRequest(String url) {

        String responseBody = "";

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .method("GET", null);

        // Add request headers
        for (String header : requestHeaders.keySet()) {
            requestBuilder.addHeader(header, requestHeaders.get(header));
        }

        // Build and execute the Request
        Request request = requestBuilder.build();

        // Make a call and save json response
        try {
            Response response = client.newCall(request).execute();
            assert response.body() != null;
            responseBody = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return responseBody;
    }
}
