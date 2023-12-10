package com.sapfir.apiUtils;

import com.sapfir.helpers.DateTimeOperations;
import com.sapfir.helpers.Properties;
import com.sapfir.helpers.TournamentResultsHelpers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PredictionParser {

    private static final Logger Log = LogManager.getLogger(PredictionParser.class.getName());

    private final JsonHelpers jsonHelpers = new JsonHelpers();
    private final Properties props = new Properties();

    private final String json;
    private final String feedItemId;
    private final String predictionResultId;
    private final String market;
    private final int userPickIndex;

    private final String feedFieldsPath;
    private final String infoFieldsPath;
    private List<String> rawOutcomeNames = new ArrayList<>();

    private final ApiHelpers apiHelpers;

    public PredictionParser(String jsonWithPredictions, String feedItemId, ApiHelpers apiHelpers) {
        this.apiHelpers = apiHelpers;

        this.json = jsonWithPredictions;
        this.feedItemId = feedItemId;
        this.feedFieldsPath = "/d/feed/" + this.feedItemId;

        String predictionInfoId = getPredictionInfoId();
        this.infoFieldsPath = "/d/info/" + predictionInfoId;

        this.market = getMarket();

        this.rawOutcomeNames = getRawOutcomeNames();
        Collections.sort(this.rawOutcomeNames);

        this.userPickIndex = getUserPickIndex();
        this.predictionResultId = getPredictionResultId();
    }

    public List<String> getRawOutcomeNames() {
        return jsonHelpers.getParentFieldNames(json, infoFieldsPath + "/outcomes");
    }

    public String getPredictionInfoId() {
        return jsonHelpers.getFieldValueByPathAndName(json, feedFieldsPath, "SubjectUID");
    }

    public String getEventIdForDatabase() {
        String eventIdFromJson = jsonHelpers.getFieldValueByPathAndName(json, infoFieldsPath, "EventID");

        // Winner bets don't have a value for EventId, but have it for TournamentID field
        if (eventIdFromJson == null) {
            return jsonHelpers.getFieldValueByPathAndName(json, infoFieldsPath, "TournamentID") + "-winner";
        }
        return eventIdFromJson;
    }

    public String getSport() {
        return jsonHelpers.getFieldValueByPathAndName(json, infoFieldsPath, "sport-name");
    }

    public String getRegion() {
        return jsonHelpers.getFieldValueByPathAndName(json, infoFieldsPath, "country-name");
    }

    public String getTournamentName() {
        return jsonHelpers.getFieldValueByPathAndName(json, infoFieldsPath, "tournament-name");
    }

    public String getMainScore() {
        /*
            Note: it is possible for main score:
                - to not be present (Winner bets)
                - to equal to null
                - or to be an empty string
         */
        return jsonHelpers.getFieldValueByPathAndName(json, infoFieldsPath, "Result");
    }

    public String getDetailedScore() {
        return jsonHelpers.getFieldValueByPathAndName(json, infoFieldsPath, "partialresult");
    }

    public String getPredictionResultId() {

        String path = infoFieldsPath + "/outcomes/" + rawOutcomeNames.get(userPickIndex);
        return jsonHelpers.getFieldValueByPathAndName(json, path, "OutcomeResultID");
    }

    private String getSportId() {
        return jsonHelpers.getFieldValueByPathAndName(json, infoFieldsPath, "sport-id");
    }

    private String getEncodeTournamentId() {
        return jsonHelpers.getFieldValueByPathAndName(json, infoFieldsPath, "encodeTournamentID");
    }

    public String getCompetitors() {
        return jsonHelpers.getFieldValueByPathAndName(json, infoFieldsPath, "event-name");
    }

    public String getDateScheduled() {
        // Getting raw unix value from json
        String unixValue = jsonHelpers.getFieldValueByPathAndName(json, infoFieldsPath, "Time");
        DateTimeOperations dateTimeOperations = new DateTimeOperations();

        // e.g. if dateScheduled is known
        if (unixValue != null) {
            return dateTimeOperations.convertFromUnix(unixValue);

        // if dateScheduled is unknown but prediction outcome is known assuming the bet is for Winner market
        } else if (predictionResultId != null) {

            Log.info("Attempting to find dateScheduled for a Winner bet");

            // Getting sport id and event id for making tournament results API call
            String sportId = getSportId();
            String encodeTournamentId = getEncodeTournamentId();
            TournamentResultsHelpers tournamentResults = new TournamentResultsHelpers(apiHelpers, sportId, encodeTournamentId);

            // Getting competitors and searching for date scheduled in tournament results
            String competitors = getCompetitors();
            unixValue = tournamentResults.getDateScheduledByCompetitors(competitors);
            return dateTimeOperations.convertFromUnix(unixValue);
        } else {
            /*
                if both dateScheduled and prediction outcome are unknown,
                then assuming the bet is for Winner market but result is still unknown
             */
            return null;
        }
    }

    public String getMarket() {
        return jsonHelpers.getFieldValueByPathAndName(json, infoFieldsPath, "Bettype");
    }

    public String getDatePredicted() {
        String unixValue = jsonHelpers.getFieldValueByPathAndName(json, feedFieldsPath, "InsertTime");
        DateTimeOperations dateTimeOperations = new DateTimeOperations();
        return dateTimeOperations.convertFromUnix(unixValue);
    }

    public String getMarketUrl() {

        String siteUrl = props.getSiteUrl();
        String eventUrl = jsonHelpers.getFieldValueByPathAndName(json, infoFieldsPath, "event-url");
        String betTypeUrl = jsonHelpers.getFieldValueByPathAndName(json, infoFieldsPath, "BettypeUrl");
        return siteUrl + eventUrl + betTypeUrl;
    }

    public String getFeedUrl() {
        String siteUrl = props.getSiteUrl();
        return siteUrl + "community/feed/item/" + feedItemId;
    }

    private List<String> getEditedOptionNames() {

        // This method gets modifies option names for markets which have unique behavior

        List<String> names = new ArrayList<>();
        
        if (market.contains("Winner")) {
            names.add(0,"Winner");
        } else if (market.equals("DC")) {
            /*
                The following order is intentional: X2 = away, 12 = draw, 1X = home. That's how rawOutcomes are sorted
                because rawOutcomeNames and editedOutcomeNames should always be sorted in the same way
             */
            names.add(0, "X2");
            names.add(1, "12");
            names.add(2, "1X");
        } else if (market.contains("CS") || market.contains("HT/FT")) {
            String path = infoFieldsPath + "/outcomes/" + rawOutcomeNames.get(0);
            String nameToAdd = jsonHelpers.getFieldValueByPathAndName(json, path, "MixedParameterName");
            names.add(0, nameToAdd);
        }
        else {
            names = rawOutcomeNames;
        }

        return names;
    }

    public String getOptionName(int index) {

        List<String> names = getEditedOptionNames();

        String optionName;
        if (index == 1) { optionName = names.get(0); }
        else if (index == 2) {
            if (names.size() >= 2) { optionName = names.get(1); }
            else { optionName = null; }
        } else if (index == 3) {
            if (names.size() == 3) { optionName = names.get(2); }
            else { optionName = null; }
        } else {
            optionName = null;
        }
        return optionName;
    }

    private List<String> getOptionValues() {

        List<String> optionValues = new ArrayList<>();

        for (String outcomeName: rawOutcomeNames) {
            String path = infoFieldsPath + "/outcomes/" + outcomeName;
            String value = jsonHelpers.getFieldValueByPathAndName(json, path, "MaxOdds");
            optionValues.add(value);
        }

        return optionValues;
    }

    public BigDecimal getOptionValue(int index) {

        List<String> values = getOptionValues();

        BigDecimal optionValue = null;
        try {
            if (index == 1) { optionValue = new BigDecimal(values.get(0)); }
            else if (index == 2) {
                if (values.size() >= 2) { optionValue = new BigDecimal(values.get(1)); }
            } else if (index == 3) {
                if (values.size() == 3) { optionValue = new BigDecimal(values.get(2)); }
            }
        } catch(NumberFormatException ex) {
            ex.printStackTrace();
        }
        return optionValue;
    }

    private int getUserPickIndex() {

        // The outcome user picked will always have null for ParentPredictionID field

        for (int i=0; i<=rawOutcomeNames.size() - 1; i++) {
            String path = infoFieldsPath + "/outcomes/" + rawOutcomeNames.get(i);
            String parentPredictionId = jsonHelpers.getFieldValueByPathAndName(json, path, "ParentPredictionID");

            if (parentPredictionId == null) { return i; }
        }

        return -1;
    }

    public String getUserPickName() {
        return getEditedOptionNames().get(userPickIndex);
    }

    public BigDecimal getUserPickValue() {
        return new BigDecimal(getOptionValues().get(userPickIndex));
    }

    public String getResult() {

        if (predictionResultId != null) {
            switch (predictionResultId) {
                case "1": return "won";
                case "2": return "lost";
                case "3": return "void";
                case "4": return "void-won";
                case "5": return "void-lost";
            }
        }

        return "not-played";
    }

    public BigDecimal getUnitOutcome() {
        Log.debug("Getting unit outcome...");

        BigDecimal unitOutcome = new BigDecimal("0");
        String result = getResult();

        if (!result.equals("not-played")) {
            BigDecimal userPickValue = getUserPickValue();
            unitOutcome = calculateUnitOutcome(userPickValue, result);
        }
        Log.debug("Successfully got unit outcome: " + unitOutcome);
        return unitOutcome;
    }

    private BigDecimal calculateUnitOutcome(BigDecimal userPickValue, String result) {
        BigDecimal unitOutcome = new BigDecimal("0");
        BigDecimal betUnits = new BigDecimal("1");
        BigDecimal betUnitsQuarterGoal = new BigDecimal("0.5");

        switch (result) {
            case "won":
                unitOutcome = userPickValue.subtract(betUnits);
                break;
            case "lost":
                unitOutcome = unitOutcome.subtract(betUnits);
                break;
            case "void-won":
                unitOutcome = betUnitsQuarterGoal.multiply(userPickValue).add(betUnitsQuarterGoal).subtract(betUnits);
                break;
            case "void-lost":
                unitOutcome = unitOutcome.subtract(betUnitsQuarterGoal);
                break;
            case "void":
                break;
            default:
                Log.error("Result not supported");
                return null;
        }
        return unitOutcome;
    }
}
