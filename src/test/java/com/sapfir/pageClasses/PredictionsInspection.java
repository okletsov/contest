package com.sapfir.pageClasses;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.ArrayList;
import java.util.List;

public class PredictionsInspection {

    private static final Logger Log = LogManager.getLogger(PredictionsInspection.class.getName());

    private WebDriver driver;

    public PredictionsInspection(WebDriver driver){
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public String getItemNumber(String predictionID) {
        return predictionID.replaceAll("\\D+","");
    }

    public List<String> getPredictions() {
        Log.trace("Getting list of prediction IDs...");
        List<WebElement> predictionsList = driver.findElements(By.className("feed-item"));
        List<String> predictionIDs = new ArrayList<>();

        for (WebElement prediction : predictionsList) {
            predictionIDs.add(prediction.getAttribute("id"));
        }
        Log.trace("Successfully got list of prediction IDs");
        return predictionIDs;
    }

    public boolean checkIfRemoved(String predicitonID) {
        String itemNumber = getItemNumber(predicitonID);
        WebElement element = driver.findElement(By.cssSelector("#"+predicitonID+" #reportResult_" + itemNumber));
        String text = element.getText().trim();

        boolean isRemoved = false;
        if (text.equals("Prediction was removed.")) {
            isRemoved = true;
        }
        System.out.println("Text:" + text);
        System.out.println("Is prediction removed: " + isRemoved);
        return isRemoved;
    }

    public String getSport(String predictionID) {
        checkIfRemoved(predictionID);
        return driver.findElement(By.cssSelector("#" + predictionID + "  .first a:nth-of-type(1)")).getText();
    }
}
