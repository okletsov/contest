package com.sapfir.apiUtils;

import com.sapfir.helpers.DateTimeOperations;
import com.sapfir.helpers.Properties;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class ApiHelpers {

    private static final Logger Log = LogManager.getLogger(ApiHelpers.class.getName());

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

    public String generatePredictionsRequestUrl(String userId, int suffix) {

        Properties props = new Properties();
        String siteUrl = props.getSiteUrl();

        DateTimeOperations dateTimeOperations = new DateTimeOperations();
        String unixTimestamp = dateTimeOperations.getUnixTimestamp();

        String corePath = siteUrl + "ajax-communityFeed/profile/" + userId + "/" + unixTimestamp + "/";

        if (suffix == 0) {
            return corePath;
        } else {
            return corePath + suffix + "/";
        }

    }

    public String makeApiRequest(String url) {

        String responseBody = "";

        // Client configuration
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(60, TimeUnit.SECONDS);
        builder.readTimeout(60, TimeUnit.SECONDS);
        builder.writeTimeout(60, TimeUnit.SECONDS);
        OkHttpClient client = builder.build();

        // Starting to build API request
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
            boolean requestFailed;
            int callNumber = 1;

            //  A do-while loop implements a retry for the failed call attempts
            do {

                Log.info("Call " + callNumber + ": " + url);

                // Delaying the subsequent calls if the first call failed
                if (callNumber != 1) { Thread.sleep(2000); }
                Response response = client.newCall(request).execute();

                // Inspecting the response body to make sure the call succeeded
                assert response.body() != null;
                responseBody = response.body().string();
                requestFailed = responseBody.contains("{'e':'503'}"); // this indicates request failure
                callNumber++;
            } while (requestFailed && callNumber < 20);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return responseBody;
    }

    public String getUsePremium() {
        return usePremium;
    }

    public String getBookieHash() {
        return bookieHash;
    }
}
