package com.sapfir.tests;

import com.sapfir.helpers.DatabaseOperations;
import com.sapfir.helpers.Properties;
import com.sapfir.helpers.UserOperations;
import com.sapfir.pageClasses.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class Test_Predictions {

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

        CommonElements ce = new CommonElements(driver);
        ProfilePage pp = new ProfilePage(driver);
        PredictionsInspection pi = new PredictionsInspection(driver);

        pp.viewParticipants();
        pp.clickParticipantUsername(username);
        pp.viewPredictions();

        List<String> predictions = pi.getPredictions();
        String sport;
        for (String predictionID: predictions) {
            sport = pi.getSport(predictionID);
            System.out.println(sport);
        }

        ce.openProfilePage();
    }
}
