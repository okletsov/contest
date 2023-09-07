package com.sapfir.apiUtils;

import com.sapfir.helpers.Properties;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.IOException;
import java.util.Set;

public class ApiHelpers {

    private ChromeDriver driver;
    private Set<Cookie> cookies;

    public ApiHelpers(ChromeDriver driver) {
        this.driver = driver;
        this.cookies = this.driver.manage().getCookies();
    }

    private static final Logger Log = LogManager.getLogger(ApiHelpers.class.getName());

    public void makeRequest() throws IOException {

        Properties props = new Properties();
        String siteUrl = props.getSiteUrl();

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();

        Request.Builder requestBuilder = new Request.Builder()
                .url(siteUrl + "/ajax-following/24836901/")
                .method("GET", null)
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .addHeader("name", "value");

        // Iterate over the cookies and add them as headers
        // (it's not how it works because Cookie is a single field in headers)
        // the code needs to change (if needed) to capture all cookies and assign it as a single field
        for (Cookie cookie : cookies) {
            requestBuilder.addHeader(cookie.getName(), cookie.getValue());
        }

        // Build the Request with dynamic cookies (see for loop above)
        Request request = requestBuilder.build();

        // Execute the request with dynamic cookies and print json response
        Response response = client.newCall(request).execute();
        ResponseBody responseBody = response.body();
        String jsonResponse = responseBody.string();
        System.out.println("zxc body: " + jsonResponse);

        // Working request without dynamic cookies
        Request request2 = new Request.Builder()
                .url(siteUrl + "/ajax-following/24836901/")
                .method("GET", null)
                .addHeader("X-Requested-With", "XMLHttpRequest")
//                .addHeader("Cookie", "name1=value1; name2=value2;")
                .build();
        Response response2 = client.newCall(request2).execute();
        ResponseBody responseBody2 = response2.body();
        String jsonResponse2 = responseBody2.string();
        System.out.println("zxc body: " + jsonResponse2);

        // Need create a request with dynamic headers
        System.out.println("Done");
    }
}
