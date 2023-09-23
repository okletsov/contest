package com.sapfir.apiUtils;

public class UserDataParser {

    private final JsonHelpers jsonHelpers = new JsonHelpers();

    private final String json;

    public UserDataParser(String userDataJson) {
        this.json = userDataJson;
    }

    public String getUsePremium() {
        return jsonHelpers.getFieldValueByPathAndName(json, "", "usePremium");
    }

    public String getBookieHash() {
        return jsonHelpers.getFieldValueByPathAndName(json, "", "bookiehash");
    }

}
