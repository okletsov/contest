package com.sapfir.tests;

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
		connection = conn.connectToDatabase();
	}

	@AfterClass
	public void tearDown() {
		conn.closeConnection(connection);
	}

	@Test
	public void test() {
		try {
			statement = connection.createStatement();
			resultSet = statement.executeUpdate(
					"INSERT INTO" +
							" contest (id, type, year, season, start_date, end_date, is_active)" +
							" VALUES (UUID(), 'seasonal', '2018', 'Autumn', '2018-09-01 00:00:00', '2018-11-30 23:59:59', 1);"
			);
			System.out.println("Inserted rows: " + statement.getUpdateCount());

		} catch (SQLException ex) {
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		} finally {

			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException sqlEx) {
				} // ignore

				statement = null;
			}
		}
	}
}