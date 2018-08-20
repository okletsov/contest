package com.sapfir.tests;

import com.sapfir.helpers.ContestOperations;
import com.sapfir.helpers.DatabaseConnection;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Initialize_Contest_Test {

	private DatabaseConnection conn = new DatabaseConnection();
	private Connection connection;
	private Statement statement = null;
	private int resultSet = 0;

	@BeforeClass
	public void setUp() {
//		connection = conn.connectToDatabase();
	}

	@AfterClass
	public void tearDown() {
//		conn.closeConnection(connection);
	}

	@Test
	public void test() {
		ContestOperations co = new ContestOperations();
		co.addSeasonalContest("2018", "Winter");
	}
}