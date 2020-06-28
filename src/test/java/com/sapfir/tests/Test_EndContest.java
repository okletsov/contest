package com.sapfir.tests;

import com.sapfir.helpers.ContestOperations;
import com.sapfir.helpers.DatabaseOperations;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Test_EndContest {

    private static final Logger Log = LogManager.getLogger(Test_EndContest.class.getName());

    private final DatabaseOperations dbOp = new DatabaseOperations();
    private Connection conn = null;

    @BeforeClass
    public void setUp() {
        conn = dbOp.connectToDatabase();
    }

    @AfterClass
    public void tearDown() {
        dbOp.closeConnection(conn);
    }

    @Test
    public void endContest() {

//        Step 1: Specify contest id to end

        String contestId = "2deb734e-ce85-11e8-8022-74852a015562";

//        Step 2: Writing general contest results

        ContestOperations contOp = new ContestOperations(conn);
        List<HashMap<String,Object>> results = contOp.getGeneralContestResults(contestId);
        contOp.writeGeneralContestResults(results);

//        Step 3: Writing winning strick
//        Step 4: Writing biggest odds
//        Step 5: Writing Finance data
    }
}
