package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidityStatuses {

    private static final Logger Log = LogManager.getLogger(ValidityStatuses.class.getName());

    private final Map<Integer, String> validityStatuses = new HashMap<>();

    public ValidityStatuses(Connection conn) {
//        this.conn = conn; -- uncomment if needs to be used outside of constructor

        Log.debug("Getting validity statuses from db...");
        DatabaseOperations dbOp = new DatabaseOperations();

        String sql = "select status, description from validity_statuses;";
        List<HashMap<String, Object>> list = dbOp.getListOfHashMaps(conn, sql);

        int key;
        String value;

        for (HashMap<String, Object> stringObjectHashMap : list) {
            key = (int) stringObjectHashMap.get("status");
            value = (String) stringObjectHashMap.get("description");
            validityStatuses.put(key, value);
        }
        Log.debug("Successfully got validity statuses from db");
    }

    public String getDescription(int validityStatus) {
        return validityStatuses.get(validityStatus);
    }
}
