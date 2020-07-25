package com.sapfir.tests;

import com.sapfir.helpers.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

public class Test_EndAnnualContest {

    private static final Logger Log = LogManager.getLogger(Test_EndAnnualContest.class.getName());

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
        ContestFinanceOperations contFinOp = new ContestFinanceOperations(conn);


//        Step 1: Specify id of a seasonal contest which indicates start of the annual contest
//        (Autumn seasonal contest id of the previous year)
        String contestId = "2deb734e-ce85-11e8-8022-74852a015562";

//        Step 2: Getting winners of annual contest
        List<HashMap<String,Object>> annContestWinners = contRes.getFirstThreeAnnPlaces(contestId);

//        Step 3: Writing annual contest placement awards
//        contFinOp.writeAnnContestPlacementAwards(annContestWinners);

//        Step 4: Deactivate annual contest

    }
}
