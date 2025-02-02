package com.sapfir.tests;

import com.sapfir.helpers.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

public class Test_EndContest {

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
        Properties prop = new Properties();

//        Step 1: Specify contest id to end and find its type (seasonal vs monthly)

        String contestId = prop.getContestId();

        Contest c = new Contest(conn, contestId);
        String contestType = c.getContestType();

//        Step 2: Writing general contest results

        List<HashMap<String,Object>> generalResultsToWrite = contRes.getGeneralContestResultsToWrite(contestId);
        contResOp.writeGeneralContestResults(generalResultsToWrite);

        if (contestType.equals("seasonal")) {

//            Step 3: Writing winning strick

            List<HashMap<String,Object>> strickPerUser = contRes.getContestResultsWinningStrickToWrite(contestId);
            contResOp.writeContestResultsWinningStrick(strickPerUser);

//            Step 4: Writing biggest odds

            List<HashMap<String,Object>> biggestOddsPerUser = contRes.getContestResultsBiggestOddsToWrite(contestId);
            contResOp.writeContestResultsBiggestOdds(biggestOddsPerUser);
        }

//        Step 5: Writing Finance data

//            Step 5.1 Writing contest placement awards
        List<HashMap<String,Object>> writtenContestResults = contRes.getFirstThreePlaces(contestId);
        contFinOp.writeContestPlacementAwards(writtenContestResults);

        if (contestType.equals("seasonal")) {

//            Step 5.2 Writing biggest odds awards
            List<HashMap<String,Object>> writtenBiggestOddsResults = contRes.getContestResultsWrittenBiggestOdds(contestId);
            contFinOp.writeContestBiggestOddsAwards(writtenBiggestOddsResults);

//            Step 5.3 Writing winning strick awards
            List<HashMap<String,Object>> writtenWinningStrickResults = contRes.getContestResultsWrittenWinningStrick(contestId);
            contFinOp.writeContestWinningStrickAwards(writtenWinningStrickResults);
        }

//        Step 6: Deactivating contest
        ContestOperations contOp = new ContestOperations(conn);
        contOp.deactivateContest(contestId);

//        Step 7: insert background job timestamp
        BackgroundJobs bj = new BackgroundJobs(conn);
        String jobName = Test_EndContest.class.getSimpleName();
        bj.addToBackgroundJobLog(jobName);

    }
}
