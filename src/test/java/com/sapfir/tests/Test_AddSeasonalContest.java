package com.sapfir.tests;

import com.sapfir.helpers.ContestOperations;
import com.sapfir.helpers.DatabaseOperations;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.Connection;

public class Test_AddSeasonalContest {

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

		String year = System.getProperty("year");
		String season = System.getProperty("season");

		co.deactivateContest(conn, "seasonal");
		co.addContest(conn, year, season);
	}
}