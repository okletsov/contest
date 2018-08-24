package com.sapfir.tests;

import com.sapfir.helpers.Properties;
import com.sapfir.pageClasses.CommonElements;
import com.sapfir.pageClasses.HomePageBeforeLogin;
import com.sapfir.pageClasses.LoginPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class Test_Participants {

    private Properties prop = new Properties();
    private WebDriver driver;
    private String baseUrl;

    @BeforeClass
    public void setUp() {
        driver = new ChromeDriver();
        baseUrl = prop.getSiteUrl();

        driver.manage().window().maximize();
    }

    @AfterClass
    public void tearDown() {
        driver.quit();
    }

    @Test
    public void getParticipants(){
        driver.get(baseUrl);

        HomePageBeforeLogin hpbl = new HomePageBeforeLogin(driver);
        LoginPage lp = new LoginPage(driver);
        CommonElements ce = new CommonElements(driver);

        hpbl.clickLogin();
        lp.signIn();
        ce.clickUsername();
    }
}
