package com.sapfir.tests;

import com.sapfir.helpers.BackgroundJobs;
import com.sapfir.helpers.ContestOperations;
import com.sapfir.helpers.DatabaseOperations;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.Connection;

public class Test_ActivateMonth2Contest {

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
	public void test() {
		ContestOperations co = new ContestOperations(conn);

		co.activateMonth2contest();

//			Insert background job timestamp
		BackgroundJobs bj = new BackgroundJobs(conn);
		String jobName = Test_ActivateMonth2Contest.class.getSimpleName();
		bj.addToBackgroundJobLog(jobName);
	}
}