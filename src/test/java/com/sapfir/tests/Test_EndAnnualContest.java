package com.sapfir.tests;

import com.sapfir.helpers.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

public class Test_EndAnnualContest {

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
    public void endAnnualContest() {

        ContestResults contRes = new ContestResults(conn);
        ContestResultsOperations contResOp = new ContestResultsOperations(conn);
        ContestFinanceOperations contFinOp = new ContestFinanceOperations(conn);
        ContestOperations contOp = new ContestOperations(conn);


//        Step 1: Specify id of an annual contest
        String contestId = "d2a41eba-0e33-11ec-ab01-288316e63e84";

//        Step 2: Getting results of annual contest
        List<HashMap<String,Object>> annContestResults = contRes.getGeneralContestResultsToWrite(contestId);

//        Step 3: Writing annual contest results
        contResOp.writeAnnContestResults(annContestResults);

//        Step 4: Getting annual contest winners
        List<HashMap<String,Object>> firstThreeAnnPlaces = contRes.getFirstThreePlaces(contestId);

//        Step 5: Writing annual contest placement awards
        contFinOp.writeContestPlacementAwards(firstThreeAnnPlaces);

//        Step 6: Deactivate annual contest
        contOp.deactivateContest(contestId);

//        Step 7: insert background job timestamp
        BackgroundJobs bj = new BackgroundJobs(conn);
        String jobName = Test_EndAnnualContest.class.getSimpleName();
        bj.addToBackgroundJobLog(jobName);

    }
}
