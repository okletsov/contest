package com.sapfir.helpers;

import com.sapfir.apiUtils.ApiHelpers;
import com.sapfir.apiUtils.TournamentResultsParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TournamentResultsHelpers {

    private static final Logger Log = LogManager.getLogger(TournamentResultsHelpers.class.getName());

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

        String dateScheduled;
        int page = 1;
        int pageCount = 1;

        // Parsing through tournament results pages until new page index is greater than total number of pages
        do {
            // Making API call to get tournament results for a given page
            String json = getTournamentResultsJson(page);

            // Getting the total number of pages
            if (page == 1) {
                TournamentResultsParser pagination = new TournamentResultsParser(json);
                pageCount = pagination.getPageCount();
            }

            // Parsing through tournament results json to find date scheduled of the latest game
            TournamentResultsParser resultsParser = new TournamentResultsParser(json);
            dateScheduled = resultsParser.getUnixDateScheduledFromJson(competitors);

            if (dateScheduled != null) {
                return dateScheduled;
            }

            Log.info("Match not found on page " + page);
            page++;
        } while (page < pageCount);

        return dateScheduled;

    }

}
