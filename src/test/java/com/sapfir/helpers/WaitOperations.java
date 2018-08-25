package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WaitOperations {

    private static final Logger Log = LogManager.getLogger(WaitOperations.class.getName());

    private WebDriver driver;

    public WaitOperations(WebDriver driver){
        this.driver = driver;
    }

    public void waitForElement (WebElement element, int timeout){
        Log.info("Waiting for element...");
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        wait.until(ExpectedConditions.visibilityOf(element));
        Log.info("Element found");
    }
}
