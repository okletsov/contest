package com.sapfir.tests;

import com.sapfir.helpers.BrowserDriver;
import com.sapfir.helpers.DatabaseOperations;
import com.sapfir.helpers.Properties;
import com.sapfir.helpers.UserOperations;
import com.sapfir.pageClasses.CommonElements;
import com.sapfir.pageClasses.HomePageBeforeLogin;
import com.sapfir.pageClasses.LoginPage;
import com.sapfir.pageClasses.ProfilePage;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.util.ArrayList;

public class Test_Participants {

    private final DatabaseOperations dbOp = new DatabaseOperations();
    private Connection conn = null;
    private WebDriver driver;
    private String baseUrl;

    @BeforeClass
    public void setUp() {

        conn = dbOp.connectToDatabase();

        BrowserDriver bd = new BrowserDriver();
        driver = bd.getDriver();
        driver.manage().window().maximize();

        Properties prop = new Properties();
        baseUrl = prop.getSiteUrl();
    }

    @AfterClass
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

        hpbl.clickLogin();
        lp.signIn();
        ce.openProfilePage();
        pp.viewParticipants();

        ArrayList <String> participants =  pp.getParticipantUsernames();
        uo.inspectParticipants(participants);
    }
}
