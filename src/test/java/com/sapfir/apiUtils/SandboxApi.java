package com.sapfir.apiUtils;

import com.sapfir.helpers.BrowserDriver;
import com.sapfir.helpers.Properties;
import com.sapfir.pageClasses.HomePageBeforeLogin;
import com.sapfir.pageClasses.LoginPage;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.IOException;
import java.util.Set;

public class SandboxApi {

    public static void main(final String... args) throws IOException {

        // Get chromedriver
        BrowserDriver bd = new BrowserDriver();
        ChromeDriver driver = bd.getDriver();
        driver.manage().window().maximize();

        // Login
        Properties prop = new Properties();
        String baseUrl = prop.getSiteUrl();

        driver.get(baseUrl);

        HomePageBeforeLogin hpbl = new HomePageBeforeLogin(driver);
        LoginPage lp = new LoginPage(driver);

        hpbl.clickLogin();
        lp.signIn();

        // Capture cookies
        Set<Cookie> cookies = driver.manage().getCookies();
        //todo: store cookies in an object for easy access

        // Make an API call
            // todo: get more headers from postman and modify the code below
            // todo: make headers dynamic based on the current session

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url("https://www.url.com")
                .method("GET", null)
                .addHeader("name", "value")
                .addHeader("Cookie", "name1=value1; name2=value2;")
                .build();
        Response response = client.newCall(request).execute();
        ResponseBody responseBody = response.body();
        String jsonResponse = responseBody.string();
        System.out.println("zxc body: " + jsonResponse);

        System.out.println("Done");

        driver.quit();
    }
}
