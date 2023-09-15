package com.sapfir.pageClasses;

import com.sapfir.helpers.SeleniumMethods;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.time.Duration;

public class HomePageBeforeLogin {

    private static final Logger Log = LogManager.getLogger(HomePageBeforeLogin.class.getName());

    private WebDriver driver;

    public HomePageBeforeLogin(WebDriver driver){
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    @FindBy(className = "loginModalBtn")
    private WebElement loginButton;

    @FindBy(name = "login-submit")
    private WebElement loginButtonInsideModal;

    public void clickLogin(){

        SeleniumMethods sm = new SeleniumMethods(driver);

        Log.debug("Clicking Login button on Home Page...");
        loginButton.click();
        sm.waitForElement(loginButtonInsideModal, Duration.ofSeconds(10));
        Log.info("Starting to login...");
    }
}
