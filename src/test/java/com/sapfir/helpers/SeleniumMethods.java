package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public class SeleniumMethods {

    private static final Logger Log = LogManager.getLogger(SeleniumMethods.class.getName());

    private WebDriver driver;

    public SeleniumMethods(WebDriver driver){
        this.driver = driver;
    }

    public void waitForElement (WebElement element, int timeout){
        Log.trace("Waiting for element...");
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        wait.until(ExpectedConditions.visibilityOf(element));
        Log.trace("Element is visible");
    }

    public boolean isElementPresent(String type, String locator) {

        /*
            This method will return true if element/elements is present in the DOM
            Otherwise it will return false

            Just pass locator type (id, css, xpath) and locator itself as parameters
         */

        Log.trace("Trying to locate elements with " + type + ": " + locator);
        List<WebElement> elements = new ArrayList<>();
        switch (type){
            case "id":
                elements = driver.findElements(By.id(locator));
                break;
            case "css":
                elements = driver.findElements(By.cssSelector(locator));
                break;
            case "class":
                elements = driver.findElements(By.className(locator));
                break;
            case "xpath":
                elements = driver.findElements(By.xpath(locator));
                break;
             default:
                 Log.error("Locator type not supported: " + type);
        }

        boolean isPresent;
        if (elements.size() > 0){
            isPresent = true;
            Log.trace("Element found with " + type + ": " + locator);
        } else {
            isPresent = false;
            Log.debug("Element not found with " + type + ": " + locator);
        }
        return isPresent;
    }
}
