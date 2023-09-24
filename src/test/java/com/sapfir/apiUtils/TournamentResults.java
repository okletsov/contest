package com.sapfir.apiUtils;

import com.sapfir.helpers.Properties;

public class TournamentResults {

    private final ApiHelpers apiHelpers;
    private final String sportId;
    private final String encodeTournamentID;
    private final String usePremium;
    private final String bookieHash;

    public TournamentResults(ApiHelpers apiHelpers, String sportId, String encodeTournamentID) {
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
                page +
                "/";
    }

    public String getTournamentResultsJson(int page) {
        String url = generateTournamentResultsUrl(page);
        return apiHelpers.makeApiRequest(url);
    }

    public String getDateScheduledByTeam(String team) {
        // Get json for page 1, then check for team name match
        // Continue until json indicate there are not more results to parse through

        int page = 1;
        String json = getTournamentResultsJson(page);

        return "WIP";
    }

}
