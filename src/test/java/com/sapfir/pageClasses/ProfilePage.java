package com.sapfir.pageClasses;

import com.sapfir.helpers.SeleniumMethods;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ProfilePage {

    private static final Logger Log = LogManager.getLogger(ProfilePage.class.getName());

    private WebDriver driver;

    public ProfilePage(WebDriver driver){
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    @FindBy(css = ".bg-black-main ul li:nth-child(4)")
    public WebElement followingTab;

    @FindBy(css = ".mt-3 .ml-auto")
    private WebElement firstUnfollowButton;

    @FindBy(css = ".active-item-feed + li")
    private WebElement feedTab;

    @FindBy(className = "view-more")
    private WebElement viewMoreButton;

    @FindBy(css = ".mt-3.items-center > .bg-gray-light")
    private List<WebElement> participants;

    public void viewParticipants() {

        SeleniumMethods sm = new SeleniumMethods(driver);

        Log.debug("Clicking Following tab...");
        followingTab.click();
        //Waiting for the first Unfollow button to appear
        sm.waitForElement(firstUnfollowButton, Duration.ofSeconds(30));

        Log.debug("Viewing Participants");
    }

    public ArrayList getParticipantUsernames() {

        String username;
        int childIndex;
        ArrayList<String> usernames = new ArrayList<>();

        for (int i = 0; i < participants.size(); i++){
            //Getting the child index of each user to generate unique css
            childIndex = i + 2;

            // .mt-3.items-center > :nth-child(3) .underline
            username = driver.findElement(By.cssSelector(
                    ".mt-3.items-center > :nth-child(" + Integer.toString(childIndex) +") .underline")).getText();
            usernames.add(username.trim());
        }
        return usernames;
    }

    public void clickParticipantUsername(String username){
        String locator = "[href=\"/profile/" + username + "/\"]";

        SeleniumMethods sm = new SeleniumMethods(driver);
        boolean usernameExist = sm.isElementPresent("css", locator);

        Assert.assertTrue(usernameExist,
                "Participant " + username + " exist in database, but is not present in Following tab");
        driver.findElement(By.cssSelector(locator)).click();
        sm.waitForElement(feedTab, Duration.ofSeconds(10));
    }

    public void viewPredictions(String username) {
        /*
            This method will click on the Feed tab and wait for one of the following conditions to be true:
                - at least one prediction appears on page
                - the text saying there are no predictions appears

            If there are predictions the method will use clickViewMoreButton method to load all predictions
         */
        Log.debug(username + ": starting to load predictions");
        feedTab.click();

        SeleniumMethods sm = new SeleniumMethods(driver);
        boolean predictionsPresent;
        boolean noPredictionsTextPresent;

        do{
            predictionsPresent = sm.isElementPresent("css", ".feed-item");
            noPredictionsTextPresent = sm.isElementPresent("css", ".message-info.feed-end[style=\"display: block;\"]");
        } while (!predictionsPresent && !noPredictionsTextPresent);

        if (predictionsPresent) {
            Log.debug("Feed loaded");
            clickViewMoreButton();
            Log.info(username + ": all predictions loaded");
        }
        if (noPredictionsTextPresent) {Log.info("User does not have any predictions");}
    }

    private void clickViewMoreButton() {
        /*
            This method will keep clicking View More button until all predictions are loaded

            How it works:
                The method will keep clicking View More button until it is unavailable (while loop)
                Once view More button is clicked - method will wait until loading of new predictions
                is complete (do-while loop)
         */
        PredictionsInspection pi = new PredictionsInspection(driver);

        int visiblePredictions = pi.getPredictions().size();
        int predictionsAfterViewMore;
        boolean viewMoreButtonPresent = viewMoreButton.isDisplayed();

        while (viewMoreButtonPresent){
            viewMoreButton.click();
            do {
                predictionsAfterViewMore = pi.getPredictions().size();
            } while (visiblePredictions == predictionsAfterViewMore);
            visiblePredictions = pi.getPredictions().size();
            viewMoreButtonPresent = viewMoreButton.isDisplayed();
        }
    }
}
