package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
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

    public void openNewTab(String link) {
        JavascriptExecutor js = (JavascriptExecutor)driver;
        js.executeScript("window.open('" + link + "')");

        ArrayList<String> tabs = new ArrayList<> (driver.getWindowHandles());
        driver.switchTo().window(tabs.get(1));
    }

    public void closeTab() {
        driver.close();
        ArrayList<String> tabs = new ArrayList<> (driver.getWindowHandles());
        driver.switchTo().window(tabs.get(0));
    }

    public String getNextTextNode(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor)driver;
        String jsOutput = (String)js.executeScript(
                "var x;" +
                        "if(arguments[0].nextSibling != null) {" +
                        "x = arguments[0].nextSibling.textContent;" +
                        "} else {" +
                        "x = \"unknown\";" +
                        "}" +
                        "return x;", element);

        if(jsOutput.equals("unknown")) { return null; }
        else { return jsOutput; }
    }

    public String getPreviousTextNode(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor)driver;
        return (String)js.executeScript("return arguments[0].previousSibling.textContent", element);
    }
}
