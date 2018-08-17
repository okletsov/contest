package com.sapfir.tests;

import com.sapfir.helpers.ConnectToDatabase;
import com.sapfir.helpers.ReadProperties;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class Initialize_Contest_Test {

	private ConnectToDatabase connection;

	@BeforeClass
	public void setUp() {
		ReadProperties properties = new ReadProperties();
		connection = new ConnectToDatabase(
						properties.getDatabaseURL(),
						properties.getDatabaseUsername(),
						properties.getDatabasePassword());
	}

	@Test
	public void test() {

	}
}
