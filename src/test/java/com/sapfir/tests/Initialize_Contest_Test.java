package com.sapfir.tests;

import com.sapfir.helpers.ContestOperations;
import com.sapfir.helpers.DatabaseOperations;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.Connection;

public class Initialize_Contest_Test {

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
	public void test() {
		ContestOperations co = new ContestOperations();
		co.addSeasonalContest(conn,"2019", "Winter");
	}
}