package com.sapfir.pageClasses;

import com.sapfir.helpers.Properties;
import com.sapfir.helpers.SeleniumMethods;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.time.Duration;

public class LoginPage {

    private static final Logger Log = LogManager.getLogger(LoginPage.class.getName());

    private final ChromeDriver driver;

    public LoginPage(ChromeDriver driver){
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    @FindBy(id = "login-username-sign")
    private WebElement usernameField;

    @FindBy(id = "login-password-sign-m")
    private WebElement passwordField;

    @FindBy(name = "login-submit")
    private WebElement loginButtonInsideModal;

    public void signIn() {
        Properties prop = new Properties();
        SeleniumMethods sm = new SeleniumMethods(driver);
        CommonElements ce = new CommonElements(driver);

        String siteUsername = prop.getSiteUsername();
        String sitePassword = prop.getSitePassword();

        Log.debug("Filling username...");
        usernameField.sendKeys(siteUsername);
        Log.debug("Filled username");

        Log.debug("Filling password...");
        passwordField.sendKeys(sitePassword);
        Log.debug("Filled password");

        Log.debug("Clicking Login button...");
        loginButtonInsideModal.click();
        Log.debug("Clicked Login button");

        sm.waitForElement(ce.username, Duration.ofSeconds(10));
        Log.info("Successfully logged in");
    }
}
