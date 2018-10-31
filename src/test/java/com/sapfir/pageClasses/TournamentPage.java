package com.sapfir.pageClasses;

import com.sapfir.helpers.SeleniumMethods;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class TournamentPage {
    private static final Logger Log = LogManager.getLogger(TournamentPage.class.getName());

    private WebDriver driver;

    public TournamentPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    @FindBy(css = ".main-filter a[href*=results]")
    private WebElement resultsButton;

    private boolean paginationExist() {
        SeleniumMethods sm = new SeleniumMethods(driver);
        return  sm.isElementPresent("id", "pagination");
    }

    private int getNumberOfPages() {
        WebElement lastPage = driver.findElement(By.cssSelector("#pagination a:last-child"));
        String pageLabel = lastPage.getAttribute("x-page");
        return Integer.parseInt(pageLabel);
    }

    public void clickResultsButton() {
        resultsButton.click();
    }
}
