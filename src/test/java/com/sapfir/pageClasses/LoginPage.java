package com.sapfir.pageClasses;

import com.sapfir.helpers.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class LoginPage {

    private static final Logger Log = LogManager.getLogger(LoginPage.class.getName());

    private WebDriver driver;

    public LoginPage(WebDriver driver){
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    @FindBy(id = "login-username1")
    private WebElement usernameField;

    @FindBy(id = "login-password1")
    private WebElement passwordField;

    @FindBy(css = "#col-content [name=login-submit]")
    private WebElement loginButton;

    public void signIn(){
        Properties prop = new Properties();

        String siteUsername = prop.getSiteUsername();
        String sitePassword = prop.getSitePassword();

        Log.debug("Filling username...");
        usernameField.sendKeys(siteUsername);
        Log.debug("Filled username");

        Log.debug("Filling password...");
        passwordField.sendKeys(sitePassword);
        Log.debug("Filled password");

        Log.debug("Clicking Login button...");
        loginButton.click();
        Log.debug("Clicked Login button");

        Log.info("Successfully logged in");
    }
}
