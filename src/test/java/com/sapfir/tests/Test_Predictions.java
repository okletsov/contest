package com.sapfir.tests;

import com.sapfir.apiUtils.*;
import com.sapfir.helpers.*;
import com.sapfir.pageClasses.*;
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
import java.util.HashMap;
import java.util.List;

public class Test_Predictions {

    private static final Logger Log = LogManager.getLogger(Test_Predictions.class.getName());

    private final DatabaseOperations dbOp = new DatabaseOperations();
    private Connection conn = null;
    private ChromeDriver driver;

    private ApiHelpers apiHelpers;

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

        // Setting up ChromeDriver
        BrowserDriver bd = new BrowserDriver();
        driver = bd.getDriver();

        Properties prop = new Properties();
        String baseUrl = prop.getSiteUrl();
        driver.get(baseUrl);

        // Getting necessary classes
        HomePageBeforeLogin hpbl = new HomePageBeforeLogin(driver);
        LoginPage lp = new LoginPage(driver);
        CommonElements ce = new CommonElements(driver);
        ProfilePage pp = new ProfilePage(driver);
        JsonHelpers jsonHelpers = new JsonHelpers();
        ResponseDecoder decoder = new ResponseDecoder();

        // Getting access to devTools
        DevTools devTools = bd.getDevTools();

        // Setting up a listener to monitor and save request headers and user-data json
        DevToolsHelpers dtHelpers = new DevToolsHelpers();
        dtHelpers.captureRequestHeaders(devTools, "/ajax-communityFeed/profile/24836901");
        dtHelpers.captureResponseBody(devTools, "ajax-user-data");

        // Performing actions in UI
        try {
            ce.clickRejectAllCookiesButton();
            hpbl.clickLogin();
            lp.signIn();
            ce.openProfilePage();
            pp.clickFeedTab();

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


        // Capturing request headers, usePremium and bookieHash to be used in subsequent API calls

        // Capturing request headers
        HashMap<String, String> requestHeaders = dtHelpers.getRequestHeaders();

        // Getting "bookieHash" and "usePremium" values (to be used in generating tournament results URL)
        String userDataJS = dtHelpers.getResponseBody();
        String userDataJason = jsonHelpers.getJsonFromJsCode(userDataJS, "pageOutrightsVar");

        UserDataParser userDataParser = new UserDataParser(userDataJason);
        String usePremium = userDataParser.getUsePremium();
        String bookieHash = userDataParser.getBookieHash();
        assert !usePremium.isEmpty() && !bookieHash.isEmpty();

        // Creating an instance of ApiHelpers class
        this.apiHelpers = new ApiHelpers(usePremium, bookieHash, requestHeaders);
    }

    @AfterSuite
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

//        Getting portal user id from database for a user from data provider
        UserOperations uo = new UserOperations(conn);
        String webPortalUserId = uo.getPortalUserIdByUsername(username);

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
            String requestUrl = apiHelpers.generatePredictionsRequestUrl(webPortalUserId, urlSuffix);
            String predictionsJson = apiHelpers.makeApiRequest(requestUrl);

            // Getting the list of feed item ids from json
            JsonHelpers jsonHelpers = new JsonHelpers();
            predictions = jsonHelpers.getParentFieldNames(predictionsJson, "/d/feed");

            // Getting prediction metadata
            for (String predictionId: predictions) {

                // If statement has bad ids
                PredictionParser parser = new PredictionParser(predictionsJson, predictionId);
                if (!parser.isPredictionBroken()) {
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

        // Individually inspect not settled predictions with dateScheduled in the past

        PredictionOperations predOp2 = new PredictionOperations(conn);
        predictions = predOp2.getInPlayPredictionsByUsername(username);

        for (String predictionId: predictions) {

            String requestUrl = apiHelpers.generateIndividualPredictionRequestUrl(predictionId);
            String predictionJson = apiHelpers.makeApiRequest(requestUrl);

            PredictionOperations predOp3 = new PredictionOperations(conn, apiHelpers, predictionJson, predictionId);
            predOp3.updatePrediction();
        }

    }
}