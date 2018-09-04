package com.sapfir.pageClasses;

import com.sapfir.helpers.WaitOperations;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.ArrayList;
import java.util.List;

public class ProfilePage {

    private static final Logger Log = LogManager.getLogger(CommonElements.class.getName());

    private WebDriver driver;

    public ProfilePage(WebDriver driver){
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    @FindBy(id = "feed_menu_following")
    private WebElement followingTab;

    @FindBy(css = "#profile-following [type=submit]")
    private WebElement saveChangesButton;

    @FindBy(id = "feed_menu_feeds")
    private WebElement feedTab;

    @FindBy(id = "feed")
    private WebElement feed;

    @FindBy(css = ".view-more[style=\"display: block;\"]")
    private WebElement viewMoreButton;

    @FindBy(css = "#profile-following .item")
    private List<WebElement> participants;

    public void viewParticipants() {

        WaitOperations wo = new WaitOperations(driver);

        Log.debug("Clicking Following tab...");
        followingTab.click();

        //Waiting for Save Changes button to know the tab finished loading
        wo.waitForElement(saveChangesButton, 10);

        Log.debug("Viewing Participants");
    }

    public ArrayList getParticipantUsernames() {

        String username;
        int childIndex;
        ArrayList<String> usernames = new ArrayList<>();

        for (int i = 0; i < participants.size(); i++){
            //Getting the child index of each user to generate unique css
            childIndex = i + 2;
            username = driver.findElement(By.cssSelector(
                    "#profile-following .item:nth-child(" + Integer.toString(childIndex) +") .username")).getText();
            usernames.add(username.trim());
        }
        return usernames;
    }

    public void viewPredictions() {
        Log.debug("Viewing predictions...");
        feedTab.click();
        WaitOperations wo = new WaitOperations(driver);
        wo.waitForElement(feed, 30);
    }
}
