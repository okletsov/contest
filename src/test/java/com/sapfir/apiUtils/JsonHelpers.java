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
                usernames.add(userDetails.get("Username").toString().replaceAll("\"", ""));
            }

        }  catch (Exception e) {
            e.printStackTrace();
        }

        return usernames;
    }

    public List<String> getUserIds(String jsonWithFollowingUsers) {

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