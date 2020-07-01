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

        ContestOperations contOp = new ContestOperations(conn);

//        Step 1: Specify contest id to end

        String contestId = "8d570ed0-de73-11e9-92ec-74852a015562";

//        Step 2: Writing general contest results

        List<HashMap<String,Object>> results = contOp.getGeneralContestResults(contestId);
        contOp.writeGeneralContestResults(results);

//        Step 3: Writing winning strick

        List<HashMap<String,Object>> strickPerUser = contOp.getContestResultsWinningStrick(contestId);
        contOp.writeContestResultsWinningStrick(strickPerUser);

//        Step 4: Writing biggest odds

        List<HashMap<String,Object>> biggestOddsPerUser = contOp.getContestResultsBiggestOdds(contestId);
        contOp.writeContestResultsBiggestOdds(biggestOddsPerUser);

//        Step 5: Writing Finance data
//        Step 6: Deactivate contest if it is active
    }
}
