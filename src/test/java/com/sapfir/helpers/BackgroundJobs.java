package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BackgroundJobs {

    private static final Logger Log = LogManager.getLogger(BackgroundJobs.class.getName());

    private final Connection conn;
    private final DatabaseOperations dbOp = new DatabaseOperations();
    private final DateTimeOperations dtOp = new DateTimeOperations();

    public BackgroundJobs(Connection conn) {
        this.conn = conn;
    }

    private String getBackgroundJobId(String className) {
        String sql = "select id from background_job where name = '" + className + "';";
        return dbOp.getSingleValue(conn, "id", sql);
    }

    public void addToBackgroundJobLog(String className) {

        PreparedStatement sql = null;
        String jobId = getBackgroundJobId(className);
        String finishTimestamp = dtOp.getTimestamp();

        try {
            sql = conn.prepareStatement(
                    "INSERT INTO `main`.`background_job_log` (`id`, `background_job_id`, `finish_timestamp`) " +
                            "VALUES (uuid(), ?, ?);"
            );

            sql.setString(1, jobId);
            sql.setString(2, finishTimestamp);

            sql.executeUpdate();
            sql.close();

        } catch (SQLException ex) {
            Log.error("SQLException: " + ex.getMessage());
            Log.error("SQLState: " + ex.getSQLState());
            Log.error("VendorError: " + ex.getErrorCode());
            Log.trace("Stack trace: ", ex);
            Log.error("Failing sql statement: " + sql);
        }

    }

}
