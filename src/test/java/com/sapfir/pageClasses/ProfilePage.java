package com.sapfir.pageClasses;

import com.sapfir.helpers.SeleniumMethods;
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

    @FindBy(css = ".view-more[style=\"display: block;\"]")
    private WebElement viewMoreButton;

    @FindBy(css = ".feed-loader")
    private WebElement feedLoafingSpinner;

    @FindBy(css = "#profile-following .item")
    private List<WebElement> participants;

    public void viewParticipants() {

        SeleniumMethods sm = new SeleniumMethods(driver);

        Log.debug("Clicking Following tab...");
        followingTab.click();
        //Waiting for Save Changes button to know the tab finished loading
        sm.waitForElement(saveChangesButton, 10);

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

        List<WebElement> text;
        List<WebElement> predictions;
        int size;
        int size2;
        do {
            text = driver.findElements(By.cssSelector(".message-info.feed-end[style=\"display: block;\"]"));
            size2 = text.size();
            predictions = driver.findElements(By.cssSelector(".feed-item"));
            size = predictions.size();
            System.out.println("Size: " + size);
            System.out.println("Size2: " + size2);

        } while (size + size2 == 0);
        
        if (size > 0) {
            System.out.println("predictions loaded");
        } else if (size2 > 0) {
            System.out.println("user does not have any predictions");
        }
    }
}
