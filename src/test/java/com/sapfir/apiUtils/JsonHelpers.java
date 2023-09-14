package com.sapfir.apiUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JsonHelpers {

    public ArrayList<String> getUsernames(String jsonWithFollowingUsers) {

        ArrayList <String> usernames = new ArrayList<>();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonWithFollowingUsers);

            List<String> userIds = getUserIds(jsonWithFollowingUsers);

            for (String individualUserid: userIds) {
                JsonNode userDetails = rootNode.at("/d/info/" + individualUserid);
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
            List<String> userIds = getUserIds(jsonWithFollowingUsers);

            // Looping through userId objects to get usernames
            for (String individualUserid: userIds) {
                JsonNode userDetails = rootNode.at("/d/info/" + individualUserid);

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

    private List<String> getUserIds(String jsonWithFollowingUsers) {

        List<String> userIds = new ArrayList<>();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonWithFollowingUsers);

            JsonNode usersInfo = rootNode.at("/d/info");

            Iterator<String> iterator = usersInfo.fieldNames();
            iterator.forEachRemaining(userIds::add);

        }  catch (Exception e) {
            e.printStackTrace();
        }

        return userIds;
    }
}