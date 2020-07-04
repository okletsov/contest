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

//        Step 1: Specify contest id to end

        String contestId = "2deb734e-ce85-11e8-8022-74852a015562";

//        Step 2: Writing general contest results

        List<HashMap<String,Object>> results = contRes.getGeneralContestResultsToWrite(contestId);
//        contResOp.writeGeneralContestResults(results);

//        Step 3: Writing winning strick

        List<HashMap<String,Object>> strickPerUser = contRes.getContestResultsWinningStrickToWrite(contestId);
//        contResOp.writeContestResultsWinningStrick(strickPerUser);

//        Step 4: Writing biggest odds

        List<HashMap<String,Object>> biggestOddsPerUser = contRes.getContestResultsBiggestOddsToWrite(contestId);
//        contResOp.writeContestResultsBiggestOdds(biggestOddsPerUser);

//        Step 5: Writing Finance data
        ContestFinance cf = new ContestFinance(conn, contestId);
        Contest c = new Contest(conn, contestId);

        BigDecimal seasEntranceFees = cf.getSumEntranceFees();
        BigDecimal seasPrize = cf.seasPrize();

        BigDecimal seasPlacesPrize = cf.getSeasPlacesPrize();

        BigDecimal seasFirstPlace = cf.getSeasFirstPlaceAward();
        BigDecimal seasSecondPlace = cf.getSeasSecondPlaceAward();
        BigDecimal seasThirdPlace = cf.getSeasThirdPlaceAward();

        BigDecimal winningStrick = cf.getWinningStrickAward();
        BigDecimal biggestOdds = cf.getBiggestOddsAward();

        BigDecimal monFirstPlace = cf.getMonFirstPlaceAward(3);
        BigDecimal monSecondPlace = cf.getMonSecondPlaceAward(3);
        BigDecimal monThirdPlace = cf.getMonThirdPlaceAward(3);

        System.out.println("success");
//        Step 6: Deactivate contest if it is active
    }
}
