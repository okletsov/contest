package com.sapfir.tests;

import com.sapfir.helpers.DatabaseOperations;
import com.sapfir.helpers.PredictionValidation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.util.ArrayList;

public class Test_ValidatePredictions {

    private static final Logger Log = LogManager.getLogger(Test_ValidatePredictions.class.getName());

    private DatabaseOperations dbOp = new DatabaseOperations();
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
    public void testPredictions() {

        PredictionValidation predVal = new PredictionValidation(conn);
        boolean newInvalidPredictionsFound = predVal.validatePredictions();

        while (newInvalidPredictionsFound) {
            newInvalidPredictionsFound = predVal.validatePredictions();
        }
    }

}
