package com.sapfir.tests;

import com.sapfir.helpers.BackgroundJobs;
import com.sapfir.helpers.ContestOperations;
import com.sapfir.helpers.DatabaseOperations;
import com.sapfir.helpers.Properties;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
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
	public void addSeasonalContest() {
		ContestOperations co = new ContestOperations(conn);
		Properties prop = new Properties();

		int year = Integer.parseInt(prop.getYear());
		String season =prop.getSeason();

		co.addContest(year, season);

//			Insert background job timestamp
		BackgroundJobs bj = new BackgroundJobs(conn);
		String jobName = Test_AddSeasonalContest.class.getSimpleName();
		bj.addToBackgroundJobLog(jobName);
	}
}