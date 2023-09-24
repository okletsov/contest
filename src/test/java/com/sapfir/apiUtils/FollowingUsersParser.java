package com.sapfir.apiUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class FollowingUsersParser {

    private final String json;
    private final String pathToUserDetails = "/d/info";
    private final JsonHelpers jsonHelpers = new JsonHelpers();

    public FollowingUsersParser(String followingUsersJson) {
        this.json = followingUsersJson;
    }

    public ArrayList<String> getUsernames(String jsonWithFollowingUsers) {

        ArrayList <String> usernames = new ArrayList<>();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonWithFollowingUsers);

            List<String> userIds = jsonHelpers.getParentFieldNames(jsonWithFollowingUsers, pathToUserDetails);

            for (String individualUserid: userIds) {
                JsonNode userDetails = rootNode.at(pathToUserDetails + "/" + individualUserid);
                String username = userDetails.get("Username").toString().replaceAll("\"", "");
                usernames.add(username);
            }

        }  catch (Exception e) {
            e.printStackTrace();
        }

        return usernames;
    }

    public String getUserIdByUsername(String jsonWithFollowingUsers, String usernameToSearchIdFor) {

        String foundUserId = "";

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonWithFollowingUsers);

            // Getting all userIds from the json
            List<String> userIds = jsonHelpers.getParentFieldNames(jsonWithFollowingUsers, pathToUserDetails);

            // Looping through userId objects to get usernames
            for (String individualUserid: userIds) {
                JsonNode userDetails = rootNode.at(pathToUserDetails + "/" + individualUserid);

                // Getting username for a given userId
                String username = userDetails.get("Username").toString().replaceAll("\"", "");

                // When username we are searching userId for is found, save userId to a variable and then return
                if (usernameToSearchIdFor.equals(username)) {
                    foundUserId = individualUserid;
                }
            }

        }  catch (Exception e) {
            e.printStackTrace();
        }

        return foundUserId;
    }

}
