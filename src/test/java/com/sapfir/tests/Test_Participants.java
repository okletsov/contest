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
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.DevTools;
import org.testng.annotations.*;

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
        driver.manage().window().maximize();
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
        hpbl.clickLogin();
        lp.signIn();
        ce.openProfilePage();
        pp.viewParticipants();

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

//        Insert background job timestamp
        BackgroundJobs bj = new BackgroundJobs(conn);
        String jobName = Test_Participants.class.getSimpleName();
        bj.addToBackgroundJobLog(jobName);
    }
}
