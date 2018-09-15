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

    public List<String> getPredictions() {
        List<WebElement> predictionsList = driver.findElements(By.className("feed-item"));
        List<String> predictionIDs = new ArrayList<>();

        for (WebElement prediction : predictionsList) {
            predictionIDs.add(prediction.getAttribute("id"));
        }
        return predictionIDs;
    }


}
