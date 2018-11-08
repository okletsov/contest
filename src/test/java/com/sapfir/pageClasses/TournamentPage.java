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

    private boolean paginationExist() {
        SeleniumMethods sm = new SeleniumMethods(driver);
        return  sm.isElementPresent("id", "pagination");
    }

    private int getNumberOfPages() {
        WebElement lastPage = driver.findElement(By.cssSelector("#pagination a:last-child"));
        String pageLabel = lastPage.getAttribute("x-page");
        return Integer.parseInt(pageLabel);
    }

    private int getMatchingGameIndex (String winnerPredicted, List<WebElement> competitorsElements) {
        /*
            Variables:
                winnerPredicted - the team/person user predicted to win a tournament (OUTRIGHTS market)
                competitorsElements - list storing web elements with competitors for each tournament event

            Goal:
                find index (first occurrence) of winnerPredicted in competitorsElements
                The index will be used to determine the datetime event occurred

            How method works:
                Because team name in OUTRIGHTS and tournament RESULTS pages not always match, method will
                do the following:

                if winnerPredicted consist of 1 word:

                    1) it will try to find winnerPredicted in competitorsElements, if mno match found then
                    2) it will go word by word in winnerPredicted and will:
                        - replace the word in winnerPredicted with the word without its last letter
                        - replace the word in winnerPredicted with the word without its last two letters
                        - replace the word in winnerPredicted with the word without its last three letters
                        - ...
                        - replace the word in winnerPredicted with empty string
                   After every replacement it will try to find winnerPredicted in competitorsElements and will
                   stop execution as soon as it finds a match

                if winnerPredicted consist of 2 words:
                    1) it will try to find winnerPredicted in competitorsElements
         */

        int gameIndex = -1;
        boolean matchFound = false;
        String gameCompetitors;

        String[] words = winnerPredicted.split(" ");
        if (words.length > 1) { // If winner predicted consist of > 1 word
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
        } else { //If winnerPredicted consist of 1 word
            int t = 0;
            while (!matchFound && t < competitorsElements.size()) {
                gameCompetitors = competitorsElements.get(t).getText().replace(".", "");
                matchFound = gameCompetitors.contains(winnerPredicted);
                if (matchFound) {
                    gameIndex = t;
                }
                t++;
            }
        }
        return gameIndex;
    }

    private void clickResultsButton() {
        resultsButton.click();
        SeleniumMethods sm = new SeleniumMethods(driver);
        sm.waitForElement(driver.findElement(By.id("tournamentTable")), 10);
    }

    private void clickNextPage(int currentPage) {
        //Clicking on the next page
        int nextPageIndex = currentPage + 1;
        WebElement nextPageElement =
                driver.findElement(By.cssSelector("#pagination [x-page=\"" + nextPageIndex + "\"]"));
        nextPageElement.click();

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

    public String getWinnerDateScheduled (String winnerPredicted) {
        String winnerDateScheduled = null;

        clickResultsButton();
        int matchingGameIndex = getMatchingGameIndex(winnerPredicted, competitorsElements);

        if (paginationExist()) {
            int currentPage = 1;
            int numberOfPages = getNumberOfPages();
            while (matchingGameIndex == -1 && currentPage < numberOfPages) {
                clickNextPage(currentPage);
                matchingGameIndex = getMatchingGameIndex(winnerPredicted, competitorsElements);
                currentPage++;
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
