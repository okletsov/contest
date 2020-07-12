package com.sapfir.tests;

import com.sapfir.helpers.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
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

        ContestResults contRes = new ContestResults(conn);
        ContestResultsOperations contResOp = new ContestResultsOperations(conn);
        ContestFinanceOperations contFinOp = new ContestFinanceOperations(conn);

//        Step 1: Specify contest id to end

        String contestId = "2deb734e-ce85-11e8-8022-74852a015562";

//        Step 2: Writing general contest results

//        List<HashMap<String,Object>> generalResultsToWrite = contRes.getGeneralContestResultsToWrite(contestId);
//        contResOp.writeGeneralContestResults(generalResultsToWrite);

//        Step 3: Writing winning strick

//        List<HashMap<String,Object>> strickPerUser = contRes.getContestResultsWinningStrickToWrite(contestId);
//        contResOp.writeContestResultsWinningStrick(strickPerUser);

//        Step 4: Writing biggest odds

        List<HashMap<String,Object>> biggestOddsPerUser = contRes.getContestResultsBiggestOddsToWrite(contestId);
        contResOp.writeContestResultsBiggestOdds(biggestOddsPerUser);

//        Step 5: Writing Finance data

//            Step 5.1 Writing contest placement award
//        List<HashMap<String,Object>> writtenContestResults = contRes.getFirstThreePlaces(contestId);
//        contFinOp.writeContestPlacementAwards(writtenContestResults);

//            Step 5.2 Writing biggest odds awards
        List<HashMap<String,Object>> writtenBiggestOdds = contRes.getContestResultsWrittenBiggestOdds(contestId);
        contFinOp.writeContestBiggestOddsAwards(writtenBiggestOdds);

//            Step 5.3 Writing winning strick awards

        System.out.println("success");
//        Step 6: Deactivate contest if it is active
    }
}
