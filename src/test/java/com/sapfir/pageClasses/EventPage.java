package com.sapfir.pageClasses;

import com.sapfir.helpers.SeleniumMethods;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.List;

public class EventPage {
    private static final Logger Log = LogManager.getLogger(EventPage.class.getName());

    private WebDriver driver;

    public EventPage(WebDriver driver){
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    @FindBy(css = "#event-status sup")
    private List<WebElement> supElements;

    private String mainScoreLocator = "#event-status strong";

    private WebElement getMainScoreElement() {
        return driver.findElement(By.cssSelector(mainScoreLocator));
    }

    String getDetailedScoreHelper() {

        SeleniumMethods sm = new SeleniumMethods(driver);

        String detailedScore = "";

        if (sm.isElementPresent("css", mainScoreLocator)){

            if (supElements.size() > 0) {

                for (int i = 0; i < supElements.size(); i++) {
                    String textBeforeSup = sm.getPreviousTextNode(supElements.get(i));
                    String supText = supElements.get(i).getText();
                    detailedScore = detailedScore + textBeforeSup + "[" + supText + "]";

                    if (i == supElements.size() - 1) {
                        String textAfterSup = sm.getNextTextNode(supElements.get(i));
                        detailedScore = detailedScore + textAfterSup;
                    }
                }
            } else {
                try {
                    detailedScore = sm.getNextTextNode(getMainScoreElement()).trim();
                } catch (Exception ex) {
                    Log.warn("Unable to locate detailed score. Check the event");
                    Log.warn("Message: " + ex.getMessage());
                    return null;
                }
            }
        } else {
            detailedScore = null;
        }
        return detailedScore;
    }
}
