package com.sapfir.pageClasses;

import com.sapfir.helpers.DateTimeOperations;
import com.sapfir.helpers.SeleniumMethods;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.lang.reflect.Array;
import java.util.List;

public class TournamentPage {
    private static final Logger Log = LogManager.getLogger(TournamentPage.class.getName());

    private WebDriver driver;

    public TournamentPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    @FindBy(css = ".main-filter a[href*=results]")
    private WebElement resultsButton;

    @FindBy(css = "#tournamentTable  [xeid] .name.table-participant")
    private List<WebElement> competitorsElements;

    @FindBy(css = "#tournamentTable  [xeid] [class*=\"table-time datet t\"]")
    private List<WebElement> eventDatesElements;

    @FindBy(css = "#pagination .active-page")
    private WebElement activePage;

    private void clickResultsButton() {
        resultsButton.click();
        SeleniumMethods sm = new SeleniumMethods(driver);
        sm.waitForElement(driver.findElement(By.id("tournamentTable")), 10);
        PageFactory.initElements(driver, this);
    }

    private boolean paginationExist() {
        SeleniumMethods sm = new SeleniumMethods(driver);
        return  sm.isElementPresent("id", "pagination");
    }

    private int getNumberOfPages() {
        WebElement lastPage = driver.findElement(By.cssSelector("#pagination a:last-child"));
        String pageLabel = lastPage.getAttribute("x-page");
        return Integer.parseInt(pageLabel);
    }

    private void clickNextPage(int currentPage) {
        //Clicking on the next page
        int nextPageIndex = currentPage + 1;
        WebElement nextPageElement =
                driver.findElement(By.cssSelector("#pagination [x-page=\"" + nextPageIndex + "\"]"));
        nextPageElement.click();

        //trying to deal with stale reference exception. Remove the line below if it is not helping
        PageFactory.initElements(driver, this);

        //Waiting for page to load
        SeleniumMethods sm = new SeleniumMethods(driver);
        sm.waitForElement(activePage, 60);
        String activePageLabel = activePage.getText();
        int activePageIndex = Integer.parseInt(activePageLabel);
        while (activePageIndex != nextPageIndex) {
            sm.waitForElement(activePage, 60);
            activePageLabel = activePage.getText();
            activePageIndex = Integer.parseInt(activePageLabel);
        }
    }

    private int searchForDirectMatch(String winnerPredicted) {

        boolean matchFound = false;
        int gameIndex = -1;

        int i = 0;
        while (!matchFound && i < competitorsElements.size()) {
            String gameCompetitors = competitorsElements.get(i).getText();
            matchFound = gameCompetitors.contains(winnerPredicted);
            if (matchFound) {
                gameIndex = i;
            }
            i++;
        }
        return gameIndex;
    }

    private int searchForShortenedMatch(String winnerPredicted) {

        int gameIndex = -1;
        boolean matchFound = false;
        String gameCompetitors;
        winnerPredicted = winnerPredicted.replace(".", "");

        String[] words = winnerPredicted.split(" ");
        int j = 0;
        while (!matchFound && j < words.length) {
            char[] letters = words[j].toCharArray();
            int k = letters.length - 1;
            while (!matchFound && k >= 0) {
                String newWord = words[j].substring(0, k);
                int i = 0;
                while (!matchFound && i < competitorsElements.size()) {
                    gameCompetitors = competitorsElements.get(i).getText().replace(".", "");
                    matchFound = gameCompetitors.contains(winnerPredicted.replace(words[j], newWord));
                    if (matchFound) {
                        gameIndex = i;
                    }
                    i++;
                }
                k--;
            }
            j++;
        }
        return gameIndex;
    }

    public String getWinnerDateScheduled (String winnerPredicted) {
        String winnerDateScheduled = null;
        int matchingGameIndex = -1;
        int currentPage;

        clickResultsButton();
        int numberOfPages = getNumberOfPages();

        if (paginationExist()) {

            currentPage = 1;
            matchingGameIndex = searchForDirectMatch(winnerPredicted);
            while (matchingGameIndex == -1 && currentPage < numberOfPages) { //Searching for direct match on every page
                clickNextPage(currentPage);
                matchingGameIndex = searchForDirectMatch(winnerPredicted);
                currentPage++;
            }

            if (matchingGameIndex == -1) { clickResultsButton(); } //Go back to results if match not found

            currentPage = 1;
            matchingGameIndex = searchForShortenedMatch(winnerPredicted);
            while (matchingGameIndex == -1 && currentPage < numberOfPages) { //Searching for shortened match on every page
                clickNextPage(currentPage);
                matchingGameIndex = searchForShortenedMatch(winnerPredicted);
                currentPage++;
            }
        } else { //If there is no pagination search for direct match
            matchingGameIndex = searchForDirectMatch(winnerPredicted);
            if (matchingGameIndex == -1) { //If match not found search for shortened match
                searchForShortenedMatch(winnerPredicted);
            }
        }

        if (matchingGameIndex != -1) {
            String className = eventDatesElements.get(matchingGameIndex).getAttribute("class");

            //Getting unix timestamp from class name
            int startIndex = className.indexOf(" t") + 2;
            int endIndex = className.indexOf("-", startIndex);
            String unixDate = className.substring(startIndex, endIndex);

            //Getting a string from unix timestamp
            DateTimeOperations dop = new DateTimeOperations();
            winnerDateScheduled = dop.convertFromUnix(unixDate);
        }
        return winnerDateScheduled;
    }
}
