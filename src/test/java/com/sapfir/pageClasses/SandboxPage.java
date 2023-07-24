package com.sapfir.pageClasses;

import com.sapfir.helpers.Properties;
import com.sapfir.helpers.SeleniumMethods;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;

import java.time.Duration;

public class SandboxPage {

    private static final Logger Log = LogManager.getLogger(SandboxPage.class.getName());

    private WebDriver driver;

    public SandboxPage(WebDriver driver){
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    @FindBy(id = "username")
    private WebElement usernameField;

    @FindBy(id = "password")
    private WebElement passwordField;

    @FindBy(css = "[class=\"splButton-primary btn btn-primary\"]")
    private WebElement loginButton;

    @FindBy(css = "[class=\"hoverable-area product-tours\"]")
    private WebElement elementToWaitFor;

    public void signIn(){
        Properties prop = new Properties();
        SeleniumMethods sm = new SeleniumMethods(driver);

        String sandboxUsername = prop.getSandboxUsername();
        String sandboxPassword = prop.getSandboxPassword();

        Log.debug("Filling username...");
        usernameField.sendKeys(sandboxUsername);
        Log.debug("Filled username");

        Log.debug("Filling password...");
        passwordField.sendKeys(sandboxPassword);
        Log.debug("Filled password");

        Log.debug("Clicking Login button...");
        loginButton.click();
        Log.debug("Clicked Login button");

        sm.waitForElement(elementToWaitFor, Duration.ofSeconds(10));
        Log.info("Successfully logged in");
    }
}
