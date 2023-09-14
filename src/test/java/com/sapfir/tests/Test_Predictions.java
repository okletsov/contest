package com.sapfir.tests;

import com.sapfir.apiUtils.JsonHelpers;
import com.sapfir.helpers.*;
import com.sapfir.pageClasses.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.DevTools;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class Test_Predictions {

    private static final Logger Log = LogManager.getLogger(Test_Predictions.class.getName());

    private final DatabaseOperations dbOp = new DatabaseOperations();
    private Connection conn = null;
    private ChromeDriver driver;
    private String followingJson;

    @BeforeClass
    public void setUp() {

//        conn = dbOp.connectToDatabase();

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

        // Getting access to devTools
        DevTools devTools = bd.getDevTools();

        // Setting up a listener to monitor and save json response with the list of participants
        DevToolsHelpers dtHelpers = new DevToolsHelpers();
        dtHelpers.captureResponseBody(devTools, "ajax-following");

        // Performing actions in UI
        ce.clickRejectAllCookiesButton();
        hpbl.clickLogin();
        lp.signIn();
        ce.openProfilePage();
        pp.viewParticipants();
        pp.clickFeedTab();

        // Capturing json response with the list of participants
        this.followingJson = dtHelpers.getResponseBody();
    }

    @AfterClass
    public void tearDown() {
        driver.quit();

//        Insert background job timestamp

        /*
        BackgroundJobs bj = new BackgroundJobs(conn);
        String jobName = Test_Predictions.class.getSimpleName();
        bj.addToBackgroundJobLog(jobName);

//        Close connection
        dbOp.closeConnection(conn);

         */
    }

    @Test(dataProvider = "participants", dataProviderClass = Participants.class)
    public void testPredictions(String username) {

        JsonHelpers jsonHelpers = new JsonHelpers();
        String jsonUserId = jsonHelpers.getUserIdByUsername(followingJson, username);

        /*
        ProfilePage pp = new ProfilePage(driver);
        PredictionsInspection pi = new PredictionsInspection(driver);
        PredictionOperations predOp = new PredictionOperations(driver, conn);

        pp.viewParticipants();
        pp.clickParticipantUsername(username);
        pp.viewPredictions(username);

        List<String> predictions = pi.getPredictions();

         */

//        List<String> predictions = new ArrayList<>();
//        predictions.add("feed_item_3191037203");

        /*

        for (String predictionID: predictions) {
            boolean predictionRemoved = pi.predictionRemoved(predictionID);
            boolean predictionExist = predOp.checkIfExist(predictionID);

            if (!predictionRemoved && !predictionExist){
                predOp.addPrediction(predictionID, username);
            } else if (predictionRemoved){
               Log.warn("Prediction " + predictionID + " was removed by " + username +
                       ". Exist in db? - " + predictionExist);
            } else{
                boolean predictionFinalized = predOp.predictionFinalized(predictionID, username);
                if (!predictionFinalized) {
                    predOp.updatePrediction(predictionID);
                }
            }
        }

         */
        CommonElements ce = new CommonElements(driver);

        /*
        ce.openProfilePage();

         */
    }
}
