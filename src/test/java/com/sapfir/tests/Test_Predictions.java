package com.sapfir.tests;

import com.sapfir.helpers.DatabaseOperations;
import com.sapfir.helpers.Properties;
import com.sapfir.helpers.UserOperations;
import com.sapfir.pageClasses.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import sun.rmi.runtime.Log;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class Test_Predictions {

    private static final Logger Log = LogManager.getLogger(Test_Predictions.class.getName());

    private DatabaseOperations dbOp = new DatabaseOperations();
    private Connection conn = null;
    private WebDriver driver;

    @BeforeClass
    public void setUp() {

        conn = dbOp.connectToDatabase();
        driver = new ChromeDriver();
        driver.manage().window().maximize();

        Properties prop = new Properties();
        String baseUrl = prop.getSiteUrl();
        driver.get(baseUrl);

        HomePageBeforeLogin hpbl = new HomePageBeforeLogin(driver);
        LoginPage lp = new LoginPage(driver);
        CommonElements ce = new CommonElements(driver);

        hpbl.clickLogin();
        lp.signIn();
        ce.openProfilePage();
    }

    @AfterClass
    public void tearDown() {
        driver.quit();
        dbOp.closeConnection(conn);
    }

    @Test(dataProvider = "participants", dataProviderClass = Participants.class)
    public void testPredictions(String username) {

        ProfilePage pp = new ProfilePage(driver);
        PredictionsInspection pi = new PredictionsInspection(driver);

        pp.viewParticipants();
        pp.clickParticipantUsername(username);
        pp.viewPredictions();

        List<String> predictions = pi.getPredictions();
//        List<String> predictions = new ArrayList<>();
//        predictions.add("feed_item_3090192303");

//        String sport;
        for (String predictionID: predictions) {
            if (!pi.checkIfRemoved(predictionID)){

                String sport = pi.getSport(predictionID);
                String region = pi.getRegion(predictionID);
                String tournament = pi.getTournament(predictionID);
                System.out.println(sport + " --> " + region + " --> " + tournament);

                ArrayList<String> options = pi.getOptionNames(predictionID);
                ArrayList<String> values = pi.getOptionValues(predictionID);
                for (int i = 0; i < options.size(); i++) {
                    System.out.println(options.get(i) + ": " + values.get(i));
                }

            } else {
               Log.warn("Prediction was removed by " + username);
            }
        }
        CommonElements ce = new CommonElements(driver);
        ce.openProfilePage();
    }
}
