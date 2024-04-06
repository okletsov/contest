package com.sapfir.tests;

import com.sapfir.apiUtils.*;
import com.sapfir.helpers.*;
import com.sapfir.pageClasses.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.DevTools;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

public class Test_Predictions {

    private final DatabaseOperations dbOp = new DatabaseOperations();
    private Connection conn = null;
    private ChromeDriver driver;
    private String followingJson;

    private ApiHelpers apiHelpers;

    @BeforeClass
    public void setUp() {

        conn = dbOp.connectToDatabase();

        // Setting up ChromeDriver
        BrowserDriver bd = new BrowserDriver();
        driver = bd.getDriver();
        driver.manage().window().maximize();

        Properties prop = new Properties();
        String baseUrl = prop.getSiteUrl();
        driver.get(baseUrl);

        // Getting necessary page classes
        HomePageBeforeLogin hpbl = new HomePageBeforeLogin(driver);
        LoginPage lp = new LoginPage(driver);
        CommonElements ce = new CommonElements(driver);
        ProfilePage pp = new ProfilePage(driver);
        JsonHelpers jsonHelpers = new JsonHelpers();

        // Getting access to devTools
        DevTools devTools = bd.getDevTools();

        // Setting up a listener to monitor and save json response with the list of participants
        DevToolsHelpers dtHelpers = new DevToolsHelpers();
        dtHelpers.captureResponseBody(devTools, "ajax-following");
        dtHelpers.captureRequestHeaders(devTools, "/ajax-communityFeed/profile/24836901");

        // Setting up a listener to monitor and save user-data json
        DevToolsHelpers dtHelpers2 = new DevToolsHelpers();
        dtHelpers2.captureResponseBody(devTools, "ajax-user-data");

        // Performing actions in UI
        ce.clickRejectAllCookiesButton();
        hpbl.clickLogin();
        lp.signIn();
        ce.openProfilePage();
        pp.viewParticipants();
        pp.clickFeedTab();

        // Capturing json response with the list of participants
        this.followingJson = dtHelpers.getResponseBody();

        // Capturing request headers, usePremium and bookieHash to be used in subsequent API calls

        // Capturing request headers
        HashMap<String, String> requestHeaders = dtHelpers.getRequestHeaders();

        // Getting "bookieHash" and "usePremium" values (to be used in generating tournament results URL)
        String userDataJS = dtHelpers2.getResponseBody();
        String userDataJason = jsonHelpers.getJsonFromJsCode(userDataJS, "pageOutrightsVar");

        UserDataParser userDataParser = new UserDataParser(userDataJason);
        String usePremium = userDataParser.getUsePremium();
        String bookieHash = userDataParser.getBookieHash();
        assert !usePremium.isEmpty() && !bookieHash.isEmpty();

        // Creating an instance of ApiHelpers class
        this.apiHelpers = new ApiHelpers(usePremium, bookieHash, requestHeaders);
    }

    @AfterClass
    public void tearDown() {
        driver.quit();

//        Insert background job timestamp

        BackgroundJobs bj = new BackgroundJobs(conn);
        String jobName = Test_Predictions.class.getSimpleName();
        bj.addToBackgroundJobLog(jobName);

//        Close connection
        dbOp.closeConnection(conn);
        
    }

    @Test(dataProvider = "participants", dataProviderClass = Participants.class)
    public void testPredictions(String username) {

        // Getting user id from a json for a user from data provider
        FollowingUsersParser followingUsersParser = new FollowingUsersParser(followingJson);
        String jsonUserId = followingUsersParser.getUserIdByUsername(followingJson, username);

        int urlSuffix = 0;
        List<String> predictions;

        do {
            /*
                The logic in a do-while loop will:
                    - make a call to get first 20 predictions
                    - if the number of predictions equals to 20, API url will be changed to pull 20 more predictions
                    - it will keep making more calls until the number of returned predictions is less than 20
             */

            // Making a call to get a json with the list of predictions
            String requestUrl = apiHelpers.generatePredictionsRequestUrl(jsonUserId, urlSuffix);
            String predictionsJson = apiHelpers.makeApiRequest(requestUrl);

            // Getting the list of feed item ids from json
            JsonHelpers jsonHelpers = new JsonHelpers();
            predictions = jsonHelpers.getParentFieldNames(predictionsJson, "/d/feed");

            // Getting prediction metadata
            for (String predictionId: predictions) {

                // If statement has bad ids
                if (
                        !predictionId.equals("6652239303")
                        && !predictionId.equals("6660653703")
                ) {
                    PredictionOperations predOp = new PredictionOperations(conn, apiHelpers, predictionsJson, predictionId);
                    boolean predictionExist = predOp.checkIfExist(predictionId);

                    if (!predictionExist){
                        predOp.addPrediction(username);
                    } else {
                        boolean predictionFinalized = predOp.predictionFinalized(predictionId, username);

                        if (!predictionFinalized) {
                            predOp.updatePrediction();
                        }
                    }
                }
            }

            urlSuffix += 20;
        } while (predictions.size() == 20);

    }
}