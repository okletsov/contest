package com.sapfir.tests;

import com.sapfir.helpers.DatabaseConnection;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.Connection;

public class Initialize_Contest_Test {

	private DatabaseConnection conn = new DatabaseConnection();
	private Connection connection;

	@BeforeClass
	public void setUp() {
		connection = conn.connectToDatabase();
	}

	@AfterClass
	public void tearDown() {
		conn.closeConnection(connection);
	}

	@Test
	public void test() {

	}
}
