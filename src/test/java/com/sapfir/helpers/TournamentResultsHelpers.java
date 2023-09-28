package com.sapfir.helpers;

import com.sapfir.apiUtils.ApiHelpers;
import com.sapfir.apiUtils.TournamentResultsParser;

public class TournamentResultsHelpers {

    private final ApiHelpers apiHelpers;
    private final String sportId;
    private final String encodeTournamentID;
    private final String usePremium;
    private final String bookieHash;

    public TournamentResultsHelpers(ApiHelpers apiHelpers, String sportId, String encodeTournamentID) {
        this.apiHelpers = apiHelpers;
        this.sportId = sportId;
        this.encodeTournamentID = encodeTournamentID;
        this.usePremium = this.apiHelpers.getUsePremium();
        this.bookieHash = this.apiHelpers.getBookieHash();
    }

    private String generateTournamentResultsUrl(int page) {

        Properties properties = new Properties();
        String siteUrl = properties.getSiteUrl();
        String path = "ajax-sport-country-tournament-archive_/";

        return siteUrl +
                path +
                sportId + "/" +
                encodeTournamentID + "/" +
                bookieHash + "/" +
                usePremium + "/" +
                "0/" +
                "page/" +
                page + "/";
    }

    public String getTournamentResultsJson(int page) {
        String url = generateTournamentResultsUrl(page);
        return apiHelpers.makeApiRequest(url);
    }

    public String getDateScheduledByCompetitors(String competitors) {

        String dateScheduled = null;

        // Making API call to get tournament results for the first page
        String json = getTournamentResultsJson(1);

        // Getting the total number of pages
        TournamentResultsParser pagination = new TournamentResultsParser(json);
        int pageCount = pagination.getPageCount();

        // Parsing through tournament results pages
        for (int page = 1; page <= pageCount; page++) {

            // Making an API call to get tournament results for a given page
            json = getTournamentResultsJson(page);

            // Parsing through tournament results json to find date scheduled of the latest game
            TournamentResultsParser resultsParser = new TournamentResultsParser(json);
            dateScheduled = resultsParser.getUnixDateScheduledFromJson(competitors);

            if (dateScheduled != null) {
                return dateScheduled;
            }

            System.out.println("Match not found on page " + page + ", searching " + page);
        }

        return dateScheduled;

    }

}
