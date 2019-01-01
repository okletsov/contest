package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ExecuteQuery {

    private static final Logger Log = LogManager.getLogger(ExecuteQuery.class.getName());

    private Statement stmt;
    private int rowsAffected;
    private ResultSet selectResult;
    private boolean isSelect;

    public ExecuteQuery(Connection conn, String sql) {

        try{
            stmt = conn.createStatement();

            Log.trace("Executing sql statement...");
            isSelect = stmt.execute(sql);
            Log.trace("Statement executed successfully");

            if (isSelect){
                selectResult = stmt.getResultSet();
            } else {
                rowsAffected = stmt.getUpdateCount();
            }

        } catch (SQLException ex){
            Log.fatal("SQLException: " + ex.getMessage());
            Log.fatal("SQLState: " + ex.getSQLState());
            Log.fatal("VendorError: " + ex.getErrorCode());
            Log.fatal("Failing sql: " + sql);
            Log.trace("Stack trace: ", ex);
            System.exit(0);
        }
    }

    public int getRowsAffected() { return rowsAffected; }
    public ResultSet getSelectResult() { return selectResult; }

    public void cleanUp(){
        try {
            Log.trace("Closing statement...");
            if (stmt != null){
                stmt.close();
                Log.trace("Statement closed successfully...");
            }

            Log.trace("Closing selectResultSet...");
            if (isSelect){
                selectResult.close();
                Log.trace("selectResultSet closed successfully...");
            }

        } catch (SQLException ex){
            Log.error("SQL Exception: " + ex.getMessage());
        }
    }
}
