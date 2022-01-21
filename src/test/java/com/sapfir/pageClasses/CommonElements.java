package com.sapfir.pageClasses;

import com.sapfir.helpers.SeleniumMethods;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class CommonElements {

    private static final Logger Log = LogManager.getLogger(CommonElements.class.getName());

    private WebDriver driver;

    public CommonElements(WebDriver driver){
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    @FindBy(css = "#user-header-r2 li:nth-child(5) a")
    public WebElement username;

    @FindBy(id = "onetrust-reject-all-handler")
    public WebElement rejectAllCookiesButton;

    @FindBy(id = "onetrust-banner-sdk")
    public WebElement cookiesBanner;

    public void openProfilePage(){

        SeleniumMethods sm = new SeleniumMethods(driver);
        ProfilePage pp = new ProfilePage(driver);

        Log.debug("Clicking Username...");
        username.click();

        //Waiting for the Following Tab button to appear to know the page finished loading
        sm.waitForElement(pp.followingTab, 10);
        Log.info("Navigated to Profile page");
    }

    public void clickRejectAllCookiesButton(){

        SeleniumMethods sm = new SeleniumMethods(driver);
        sm.waitForElement(rejectAllCookiesButton, 10);
        rejectAllCookiesButton.click();
    }
}
