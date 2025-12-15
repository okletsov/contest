package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class SqlLoader {

    private static final Logger Log = LogManager.getLogger(SqlLoader.class.getName());
    private String sql;
    private final List<String> paramOrder = new ArrayList<>();

    public SqlLoader(String filePath) {
        readSqlFromFile(filePath);
        setParamsOrder();
    }

    private void readSqlFromFile(String filePath) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(filePath)), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }

        } catch (Exception ex) {
            Log.error(ex);
        }
        sql = sb.toString().trim();
    }

    private void setParamsOrder() {
        Matcher matcher = java.util.regex.Pattern.compile(":(\\w+)").matcher(sql);
        while (matcher.find()) {
            paramOrder.add(matcher.group(1));
        }
    }

    public String getSql(HashMap<String, Object> paramValues) {
        sql = sql.replaceAll(":(\\w+)", "?");
        for (String paramName : paramOrder) {
            Object rawVal = paramValues.get(paramName);
            String value;

            if (rawVal == null) {
                value = "NULL";
            } else if (rawVal instanceof String || rawVal instanceof java.sql.Date || rawVal instanceof java.time.LocalDateTime) {
                value = "'" + rawVal.toString().replace("'", "''") + "'";
            } else if (rawVal instanceof List<?> ) {
                List<?> list = (List<?>) rawVal;
                if (list.isEmpty()) {
                    value = "NULL";
                } else {
                    value = list.stream()
                            .map(v -> v == null
                                    ? "NULL"
                                    : "'" + v.toString().replace("'", "''") + "'")
                            .collect(Collectors.joining(", ", "(", ")"));
                }
                
            } else {
                value = rawVal.toString();
            }

            sql = sql.replaceFirst("\\?", value);
        }
        return sql;
    }

    public String getSql() {
        return getSql(null);
    }
}
