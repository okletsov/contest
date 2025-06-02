package com.sapfir.tests;

import com.sapfir.apiUtils.FollowingUsersParser;
import com.sapfir.apiUtils.ResponseDecoder;
import com.sapfir.helpers.*;
import com.sapfir.pageClasses.CommonElements;
import com.sapfir.pageClasses.HomePageBeforeLogin;
import com.sapfir.pageClasses.LoginPage;
import com.sapfir.pageClasses.ProfilePage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.DevTools;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.util.ArrayList;

public class Test_Participants {

    private static final Logger Log = LogManager.getLogger(Test_Participants.class.getName());

    private final DatabaseOperations dbOp = new DatabaseOperations();
    private Connection conn = null;
    private ChromeDriver driver;
    private String baseUrl;
    private DevTools devTools;

    @BeforeSuite
    public void setUp() {

        // Register the shutdown hook for fallback cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Log.info("Shutdown hook triggered. Performing cleanup...");
            if (driver != null) {
                driver.quit();
                Log.info("WebDriver closed via shutdown hook.");
            }
            if (conn != null) {
                dbOp.closeConnection(conn);
                Log.info("Database connection closed via shutdown hook.");
            }
        }));

        conn = dbOp.connectToDatabase();

        BrowserDriver bd = new BrowserDriver();
        driver = bd.getDriver();
        devTools = bd.getDevTools();

        Properties prop = new Properties();
        baseUrl = prop.getSiteUrl();
    }

    @AfterSuite
    public void tearDown() {
        driver.quit();
        dbOp.closeConnection(conn);
    }

    @Test
    public void testParticipants() {
        driver.get(baseUrl);

        HomePageBeforeLogin hpbl = new HomePageBeforeLogin(driver);
        LoginPage lp = new LoginPage(driver);
        CommonElements ce = new CommonElements(driver);
        ProfilePage pp = new ProfilePage(driver);
        UserOperations uo = new UserOperations(conn);
        DevToolsHelpers dtHelpers = new DevToolsHelpers();

        // Setting up a listener to monitor and save json data
        dtHelpers.captureResponseBody(devTools, "ajax-following");

        // Logging in and viewing the "Following tab" to trigger API call with the list of participants
        try {
            ce.clickRejectAllCookiesButton();
            hpbl.clickLogin();
            lp.signIn();
            ce.openProfilePage();
            pp.viewParticipants();
        } catch (Exception e) {
            File scr = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File destination = new File("failure-screenshot.png");
            try {
                Files.copy(scr.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Log.error("Screenshot saved to " + destination.getAbsolutePath());
            } catch (IOException ex) {
                Log.error("Failed to save screenshot: " + ex.getMessage());
                throw new RuntimeException(ex);
            }
            throw e;
        }

        // Getting the list of participants from json response
        ResponseDecoder decoder = new ResponseDecoder();
        String followingJson;
        try {
            followingJson = decoder.decodeResponse(dtHelpers.getResponseBody());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        FollowingUsersParser followingUsersParser = new FollowingUsersParser(followingJson);
        ArrayList<String> participants = followingUsersParser.getUsernames(followingJson);

        // Inspecting participants
        uo.inspectParticipants(participants);

        // Update portal id for participants
        for (String username : participants) {
            String dbPortalUserId = uo.getPortalUserIdByUsername(username);
            if (dbPortalUserId == null) {
                Log.info(username + ": DB portal_id is null. Writing portal id...");
                String webPortalUserId = followingUsersParser.getUserIdByUsername(followingJson, username);
                uo.addPortalUserId(username, webPortalUserId);
            }
        }

//        Insert background job timestamp
        BackgroundJobs bj = new BackgroundJobs(conn);
        String jobName = Test_Participants.class.getSimpleName();
        bj.addToBackgroundJobLog(jobName);
    }
}
