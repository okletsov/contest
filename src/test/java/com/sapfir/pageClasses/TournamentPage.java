package com.sapfir.pageClasses;

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

    private boolean paginationExist() {
        SeleniumMethods sm = new SeleniumMethods(driver);
        return  sm.isElementPresent("id", "pagination");
    }

    private int getNumberOfPages() {
        WebElement lastPage = driver.findElement(By.cssSelector("#pagination a:last-child"));
        String pageLabel = lastPage.getAttribute("x-page");
        return Integer.parseInt(pageLabel);
    }

    private int getMatchingGameIndex (String winnerPredicted, List<WebElement> tournamentGames) {
        /*
            Variables:
                betTeam - the team/person user predicted to win a tournament (OUTRIGHTS market)
                compArray - array storing events of the tournament

            Goal:
                find index (first occurrence) of betTeam in compArray.
                The index will be used to determine the datetime event occurred

            How method works:
                Because team name in OUTRIGHTS and tournament RESULTS pages not always match, method will
                do the following:

                if betText consist of only one word the method will try to find that word in compaArray
                if betText consist of > 1 words:
                    1) it will try to find betText in compArray, if mno match found then
                    2) it will go word by word in betText and will:
                        - replace the word in betText with empty string --> try to find match in compAtrray
                        - replace the word in betText with its first letter --> try to find match in compAtrray
                        - replace the word in betText with its first two letters --> try to find match in compAtrray
                        - replace the word in betText with its first three letters --> try to find match in compAtrray
                        - replace the word in betText with its first four letters --> try to find match in compAtrray
                        - ...

             Can return wrong index if:
                - betText consists of > 1 words
                  AND one of the words shortened
                  AND there are two teams with same NOT shortened word playing in tournament

                  Example: betText can be Williams Serena or Williams Venus
                           compArray is shortened to Williams S. and Williams V.
                           method will return wrong result if user predicted Williams Serena to win, but Venus advanced
                           to later stages (her games were after last Serena's) comparing to Serena

            Think how to handle Pliskova example above
             - user bets on Karolina, but Kristina wins (method will return Kristina's result)
         */

        int gameIndex = -1;
        boolean matchFound = false;
        int i = 0;
        while (!matchFound && i < tournamentGames.size()) {
            String gameCompetitors = tournamentGames.get(i).getText().replace(".", "");

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

    public void clickResultsButton() {
        resultsButton.click();
    }
}
