package com.sapfir.tests;

import com.sapfir.helpers.*;
import com.sapfir.pageClasses.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.chrome.ChromeDriver;
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

    @BeforeClass
    public void setUp() {

        conn = dbOp.connectToDatabase();

        BrowserDriver bd = new BrowserDriver();
        driver = bd.getDriver();
        driver.manage().window().maximize();

        Properties prop = new Properties();
        String baseUrl = prop.getSiteUrl();
        driver.get(baseUrl);

        HomePageBeforeLogin hpbl = new HomePageBeforeLogin(driver);
        LoginPage lp = new LoginPage(driver);
        CommonElements ce = new CommonElements(driver);

        ce.clickRejectAllCookiesButton();
        hpbl.clickLogin();
        lp.signIn();
        ce.openProfilePage();
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

        ProfilePage pp = new ProfilePage(driver);
        PredictionsInspection pi = new PredictionsInspection(driver);
        PredictionOperations predOp = new PredictionOperations(driver, conn);

        pp.viewParticipants();
        pp.clickParticipantUsername(username);
        pp.viewPredictions(username);

        List<String> predictions = pi.getPredictions();
//        List<String> predictions = new ArrayList<>();
//        predictions.add("feed_item_3191037203");

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
        CommonElements ce = new CommonElements(driver);
        ce.openProfilePage();
    }
}
