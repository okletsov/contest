package com.sapfir.tests;

import com.sapfir.helpers.DatabaseOperations;
import com.sapfir.helpers.Properties;
import com.sapfir.helpers.UserOperations;
import com.sapfir.pageClasses.CommonElements;
import com.sapfir.pageClasses.HomePageBeforeLogin;
import com.sapfir.pageClasses.LoginPage;
import com.sapfir.pageClasses.ProfilePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class Test_Participants {

    private DatabaseOperations dbOp = new DatabaseOperations();
    private Connection conn = null;
    private Properties prop = new Properties();
    private WebDriver driver;
    private String baseUrl;

    @BeforeClass
    public void setUp() {

        conn = dbOp.connectToDatabase();
        driver = new ChromeDriver();
        baseUrl = prop.getSiteUrl();

        driver.manage().window().maximize();
    }

    @AfterClass
    public void tearDown() {
        driver.quit();
        dbOp.closeConnection(conn);
    }

    @Test
    public void getParticipants() {
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
        uo.inspectParticipantUsernames(participants);
//        uo.addUser("Deagle", "New Participant");

    }
}
