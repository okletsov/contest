package com.sapfir.apiUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JsonHelpers {

    private final String pathToUserDetails = "/d/info";

    public String getFieldValueByPathAndName(String json, String pathToField, String fieldName) {

        String fieldValue = null;

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(json);

            fieldValue = rootNode.at(pathToField).get(fieldName).toString();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return fieldValue;
    }

    public List<String> getParentFieldNames(String json, String pathToFields) {

        List<String> userIds = new ArrayList<>();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(json);

            JsonNode pathToNodes = rootNode.at(pathToFields);

            Iterator<String> iterator = pathToNodes.fieldNames();
            iterator.forEachRemaining(userIds::add);

        }  catch (Exception e) {
            e.printStackTrace();
        }

        return userIds;
    }

    public ArrayList<String> getUsernames(String jsonWithFollowingUsers) {

        ArrayList <String> usernames = new ArrayList<>();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonWithFollowingUsers);

            List<String> userIds = getParentFieldNames(jsonWithFollowingUsers, pathToUserDetails);

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
            List<String> userIds = getParentFieldNames(jsonWithFollowingUsers, pathToUserDetails);

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