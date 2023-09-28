package com.sapfir.apiUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JsonHelpers {

    public String getFieldValueByPathAndName(String json, String pathToField, String fieldName) {

        String fieldValue = "null";

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(json);

            // Check if the field exists (e.g. Result field is not there for Winner bets)
            if (!rootNode.at(pathToField).has(fieldName)) { return "null"; }

            // Grab field value
            fieldValue = rootNode.at(pathToField).get(fieldName).toString();

            // Removing quotation marks and HTML entities (e.g. "Yastremska&nbsp;D.&nbsp;ret.")
            fieldValue = fieldValue
                    .replaceAll("\"", "")
                    .replaceAll("&[a-zA-Z]+;", " ");

            if (fieldValue.isEmpty()) { return "null"; }

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

    public String getJsonFromJsCode(String jsCode, String jsVariable) {

        String json = null;

        try {
            
            // Getting java object from provided JS code and JS variable
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("graal.js");
            engine.eval(jsCode);
            Object jsonData = engine.get(jsVariable);

            // Converting java object into a json string
            ObjectMapper objectMapper = new ObjectMapper();
            json = objectMapper.writeValueAsString(jsonData);
        } catch (ScriptException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return json;
    }
}