package com.sapfir.tests;

import com.sapfir.helpers.DatabaseConnection;
import com.sapfir.helpers.ReadProperties;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.SQLException;

public class Initialize_Contest_Test {

	private Connection connection;
	private DatabaseConnection c = new DatabaseConnection();

	@BeforeClass
	public void setUp() {
		ReadProperties properties = new ReadProperties();


		connection = c.getConnection(
						properties.getDatabaseURL(),
						properties.getDatabaseUsername(),
						properties.getDatabasePassword());
	}

	@AfterClass
	public void tearDown() {
		c.closeConnection(connection);
	}

	@Test
	public void test() {

	}
}
