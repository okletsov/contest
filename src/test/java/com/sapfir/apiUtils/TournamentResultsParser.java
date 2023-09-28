package com.sapfir.apiUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class TournamentResultsParser {

    private final String json;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MultiValuedMap<String, String> gamesAndGameTimes = new ArrayListValuedHashMap<>();

    public TournamentResultsParser(String resultsJson) {
        this.json = resultsJson;
        saveGamesAndGameTimes();
    }

    private void saveGamesAndGameTimes() {

        try {
            JsonNode rootNode = objectMapper.readTree(json);
            JsonNode games = rootNode.path("d").path("rows");

            // Looping through games
            for(int i=0; i<= games.size()-1; i++) {

                // Saving items to multimap using dateScheduled as a key and competitors as a value
                String dateScheduled = games.get(i).get("date-start-timestamp").asText();
                String competitors = games.get(i).get("name").asText();
                this.gamesAndGameTimes.put(dateScheduled, competitors);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private ArrayList<String> sortedDateScheduledList() {
        ArrayList<String> listOfDateScheduled = new ArrayList<>(gamesAndGameTimes.keySet());
        listOfDateScheduled.sort(Collections.reverseOrder());
        return listOfDateScheduled;
    }

    public int getPageCount() {

        int pages = 1;

        try {
            String paginationPath = ("/d/pagination");
            JsonHelpers jsonHelpers = new JsonHelpers();
            pages = Integer.parseInt(jsonHelpers.getFieldValueByPathAndName(json, paginationPath, "pageCount"));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return pages;
    }

    public String getUnixDateScheduledFromJson(String winnerPredicted) {

        boolean matchFound;

        // Iterating over gamesAndTimes
        for(String dateScheduled: sortedDateScheduledList()) {

            // Getting all games played on a specific dateScheduled
            Collection<String> gamesPlayedOnDateScheduled = gamesAndGameTimes.get(dateScheduled);

            // Iterating over competitors for played games to find matching name
            for(String competitors: gamesPlayedOnDateScheduled) {
                matchFound = directMatchFound(winnerPredicted, competitors);
                if (matchFound) {
                    return dateScheduled;
                }
            }
        }

        // If not found, iterate over gamesAndTimes to find shortened match
        for(String dateScheduled: sortedDateScheduledList()) {

            // Getting all games played on a specific dateScheduled
            Collection<String> gamesPlayedOnDateScheduled = gamesAndGameTimes.get(dateScheduled);

            // Iterating over competitors for played games to find matching name
            for(String competitors: gamesPlayedOnDateScheduled) {
                matchFound = shortenedMatchFound(winnerPredicted, competitors);
                if (matchFound) {
                    return dateScheduled;
                }
            }
        }

        return null;
    }

    private boolean directMatchFound(String teamToSearch, String game) {
        return game.contains(teamToSearch);
    }

    private boolean shortenedMatchFound(String teamToSearch, String game) {

        boolean matchFound = false;
        String competitors = game.replace(".", "");
        String team = teamToSearch.replace(".", "");

        String[] words = team.split(" ");
        int j = 0;
        while (!matchFound && j < words.length) {
            char[] letters = words[j].toCharArray();
            int k = letters.length - 1;
            while (!matchFound && k >= 0) {
                String newWord = words[j].substring(0, k);
                matchFound = competitors.contains(team.replace(words[j], newWord));
                k--;
            }
            j++;
        }
        return matchFound;
    }
}
