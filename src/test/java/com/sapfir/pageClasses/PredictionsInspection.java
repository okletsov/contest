package com.sapfir.pageClasses;

import com.sapfir.helpers.DateTimeOperations;
import com.sapfir.helpers.SeleniumMethods;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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

    public String getResult(String predictionID) {
        Log.debug("Getting prediction result...");
        String locator = "#" + predictionID + " .odd [class*=\"status-text-\"]";
        SeleniumMethods sm = new SeleniumMethods(driver);
        boolean resultKnown = sm.isElementPresent("css", locator);

        String result = "Not played";
        if (resultKnown) {
            String siteText = driver.findElement(By.cssSelector(locator)).getText().trim();
            switch (siteText) {
                case "W":
                    result = "Won";
                    break;
                case "L":
                    result = "Lost";
                    break;
                case "V":
                    result = "Voided";
                    break;
                default:
                    Log.error("Unexpected text returned");
            }
        }
        Log.debug("Successfully got prediction result: " + result);
        return result;
    }

    public String getDateScheduled(String predictionID) {
        Log.debug("Getting date scheduled...");
        String dateScheduled;

        //Check if event date exist
        String locator = "#" + predictionID + " [id*=\"status-\"]";
        SeleniumMethods sm = new SeleniumMethods(driver);
        boolean dateExist = sm.isElementPresent("css", locator);

        if (dateExist) {
            //Getting element's class name
            WebElement element = driver.findElement(By.cssSelector(locator));
            String className = element.getAttribute("class");

            //Getting unix timestamp from class name
            int startIndex = className.indexOf(" t") + 2;
            int endIndex = className.indexOf("-", startIndex);
            String unixDate = className.substring(startIndex, endIndex);

            DateTimeOperations dop = new DateTimeOperations();
            dateScheduled = dop.convertFromUnix(unixDate);
            Log.debug("Successfully got date scheduled");

        } else {
            Log.debug("Event date unknown: null returned");
            dateScheduled = null;
        }
        return dateScheduled;
    }

    public String getCompetitorsText(String predictionID) {
        Log.debug("Getting competitors...");
        WebElement element = getCompetitorsElement(predictionID);
        String competitors = element.getText().trim();
        Log.debug("Successfully got competitors");
        return competitors;
    }

    public String getMarket(String predictionID) {
        Log.debug("Getting market...");
        String locator = "#" + predictionID + " .odd span a";
        WebElement element = driver.findElement(By.cssSelector(locator));
        String market = element.getText().trim();
        Log.debug("Successfully got market");
        return market;
    }

    private WebElement getCompetitorsElement(String predictionID) {
        String locator = "#" + predictionID + " .odd a.bold";
        return driver.findElement(By.cssSelector(locator));
    }

    public String getMainScore(String predictionID) {

        /*
            This method checks if the score is known and grabs main score from feed page
         */

        Log.debug("Getting main score score...");
        String mainScoreText;
        String locator = "#" + predictionID + " [class=\"center bold table-odds\"]";

        SeleniumMethods sm = new SeleniumMethods(driver);
        boolean scoreKnown = sm.isElementPresent("css", locator);

        if (scoreKnown){
            WebElement mainScoreElement = driver.findElement(By.cssSelector(locator));
            mainScoreText = mainScoreElement.getText().trim();
            Log.debug("Successfully got main score");
        } else {
            Log.debug("Main score unknown");
            mainScoreText = null;
        }
        return mainScoreText;
    }

    public String getDetailedScore(String predictionID) {

        /*
            This method checks if main score exist and:
                - opens event page in new tab
                - grabs detailed score
                - closes tab
         */

        Log.debug("Getting detailed score...");
        String detailedScore;
        String mainScore = getMainScore(predictionID);

        if (mainScore != null) {
            SeleniumMethods sm = new SeleniumMethods(driver);

            //Open new tab
            WebElement competitors = getCompetitorsElement(predictionID);
            sm.openNewTab(competitors.getAttribute("href"));
            EventPage ep = new EventPage(driver);

            //Grab detailed score and close the tab
            detailedScore = ep.getDetailedScoreHelper();
            sm.closeTab();
            Log.debug("Successfully got detailed score");
        } else {
            detailedScore = null;
            Log.debug("Detailed score unknown");
        }
        return detailedScore;
    }
}
