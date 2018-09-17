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
      /*
            This method will try to search if the following text is present for prediction:
            "Prediction was removed."
            It will return true if prediction was removed (false otherwise)
       */

      Log.trace("Checking if prediction was removed...");
      WebElement element = driver.findElement(By.cssSelector("#" + predicitonID + " .feed-item-content.hover-togle"));
      String text = element.getText();

        boolean isRemoved = false;
        if (text.contains("Prediction was removed.")) {
            isRemoved = true;
        }
        Log.trace("Check complete. Prediction removed = " + isRemoved);
        return isRemoved;
    }

    public String getSport(String predictionID) {
        Log.debug("Getting sport of prediction...");
        WebElement element = driver.findElement(By.cssSelector("#" + predictionID + "  .first a:nth-of-type(1)"));
        String sport = element.getText().trim();
        Log.debug("Successfully got sport: " + sport);
        return sport;
    }

    public String getRegion(String predictionID) {
        Log.debug("Getting region of prediction...");
        WebElement element = driver.findElement(By.cssSelector("#" + predictionID + "  .first a:nth-of-type(2)"));
        String region = element.getText().trim();
        Log.debug("Successfully got region: " + region);
        return region;
    }

    public String getTournament(String predictionID) {
        Log.debug("Getting tournament of prediction...");
        WebElement element = driver.findElement(By.cssSelector("#" + predictionID + "  .first a:nth-of-type(3)"));
        String region = element.getText().trim();
        Log.debug("Successfully got tournament: " + region);
        return region;
    }

    public ArrayList<String> getOptionNames(String predictionID) {
        Log.debug("Getting option names...");
        ArrayList<String> optionNames = new ArrayList<>();
        List<WebElement> columns = driver.findElements(By.cssSelector("#" + predictionID + " .dark .center"));

        for (int i = 0; i < columns.size(); i++) {
            if (i == 0) {
                String option1 = columns.get(i).getText();
                optionNames.add(option1);
            } else if (i == 1) {
                String option2 = columns.get(i).getText();
                optionNames.add(option2);
            } else if (i == 2) {
                String option3 = columns.get(i).getText();
                optionNames.add(option3);
            }
        }
        Log.debug("Successfully got option names");
        return optionNames;
    }

    public ArrayList<String> getOptionValues(String predictionID) {
        Log.debug("Getting option values...");
        ArrayList<String> values = new ArrayList<>();
        List<WebElement> columns =  driver.findElements(By.cssSelector("#" + predictionID + " .odds-nowrp"));

        for (int i = 0; i < columns.size(); i++) {
            if (i == 0) {
                String value1 = columns.get(i).getAttribute("xodd");
                values.add(value1);
            } else if (i == 1) {
                String value2 = columns.get(i).getAttribute("xodd");
                values.add(value2);
            } else if (i == 2) {
                String value3 = columns.get(i).getAttribute("xodd");
                values.add(value3);
            }
        }
        Log.debug("Successfully got optoin values");
        return values;
    }
}
