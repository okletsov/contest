package com.sapfir.tests;

import com.sapfir.helpers.ConnectToDatabase;
import com.sapfir.helpers.ReadProperties;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class Initialize_Contest_Test {

	@BeforeClass
	public void setUp() {
		ReadProperties properies = new ReadProperties();
		ConnectToDatabase connection =
				new ConnectToDatabase(properies.getDatabaseURL(), properies.getDatabaseUsername(), properies.getDatabasePassword());
	}

	@Test
	public void test() {

	}
}
