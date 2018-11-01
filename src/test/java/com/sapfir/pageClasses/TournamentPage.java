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

                if winnerPredicted consist of only one word the method will try to find that word in competitorsElements
                if winnerPredicted consist of > 1 words:
                    1) it will try to find betText in competitorsElements, if mno match found then
                    2) it will go word by word in winnerPredicted and will:
                        - replace the word in winnerPredicted with empty string --> try to find match in competitorsElements
                        - replace the word in winnerPredicted with its first letter --> try to find match in competitorsElements
                        - replace the word in winnerPredicted with its first two letters --> try to find match in competitorsElements
                        - replace the word in winnerPredicted with its first three letters --> try to find match in competitorsElements
                        - replace the word in winnerPredicted with its first four letters --> try to find match in competitorsElements
                        - ...

             Can return wrong index if:
                - winnerPredicted consists of > 1 words
                  AND one of the words shortened
                  AND there are two teams with same NOT shortened word playing in tournament

                  Example: winnerPredicted can be Williams Serena or Williams Venus
                           competitorsElements is shortened to Williams S. and Williams V.
                           method will return wrong result if user predicted Williams Serena to win, but Venus advanced
                           to later stages (her games were after last Serena's) comparing to Serena
         */

        int gameIndex = -1;
        boolean matchFound = false;
        int i = 0;
        while (!matchFound && i < competitorsElements.size()) {
            String gameCompetitors = competitorsElements.get(i).getText().replace(".", "");

            matchFound = gameCompetitors.contains(winnerPredicted);
            String[] words = winnerPredicted.split(" ");
            if (!matchFound && words.length > 1) {
                int j = 0;
                while (!matchFound && j < words.length) {
                    char[] letters = words[j].toCharArray();
                    int k = 0;
                    while (!matchFound && k < letters.length) {
                        String newWord = words[j].substring(0, k);
                        matchFound = gameCompetitors.contains(winnerPredicted.replace(words[j], newWord));
                        k++;
                    }
                    j++;
                }
            }
            i++;
        }
        if (matchFound) {
            gameIndex = i - 1;
        }
        return gameIndex;
    }

    private void clickResultsButton() {
        resultsButton.click();
        SeleniumMethods sm = new SeleniumMethods(driver);
        sm.waitForElement(driver.findElement(By.id("tournamentTable")), 10);
    }

    public String getWinnerDateScheduled (String winnerPredicted) {
        clickResultsButton();
        int matchingGameIndex = getMatchingGameIndex(winnerPredicted, competitorsElements);
        System.out.println(matchingGameIndex);
        String className = eventDatesElements.get(matchingGameIndex).getAttribute("class");

        //Getting unix timestamp from class name
        int startIndex = className.indexOf(" t") + 2;
        int endIndex = className.indexOf("-", startIndex);
        String unixDate = className.substring(startIndex, endIndex);

        DateTimeOperations dop = new DateTimeOperations();
        return dop.convertFromUnix(unixDate);
    }
}
