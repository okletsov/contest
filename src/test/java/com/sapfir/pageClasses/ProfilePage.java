package com.sapfir.pageClasses;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

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

    @FindBy(css = "#profile-following .item")
    private List<WebElement> participants;

    public void clickFollowingTab() throws InterruptedException {
        Log.debug("Clicking Following tab...");
        followingTab.click();
        Thread.sleep(2000);
        Log.info("Clicked Following tab");
    }

    public void getParticipantUsername(){

        String username;
        int itemIndex;

        for (int i = 0; i < participants.size(); i++){
            itemIndex = i + 2;
            username = driver.findElement(By.cssSelector("#profile-following .item:nth-child(" + Integer.toString(itemIndex) +") .username")).getText();
            System.out.println(username);
        }
    }
}
