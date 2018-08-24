package com.sapfir.pageClasses;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class HomePage {

    private static final Logger Log = LogManager.getLogger(HomePage.class.getName());

    private WebDriver driver;

    public HomePage(WebDriver driver){
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    @FindBy(name = "login-submit")
    private WebElement loginButton;

    public void clickLogin(){
        Log.debug("Clicking Home Page Login button...");
        loginButton.click();
        Log.info("Home Page Login clicked");
    }
}
