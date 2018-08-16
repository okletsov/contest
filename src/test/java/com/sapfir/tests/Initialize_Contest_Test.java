package com.sapfir.tests;

import com.sapfir.helpers.ConnectToDatabase;
import com.sapfir.helpers.ReadProperties;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class Initialize_Contest_Test {

	@BeforeClass
	public void setUp() {

	}

	@Test
	public void test() {
		ReadProperties rp = new ReadProperties();
		System.out.println(rp.getDatabaseURL());

		ConnectToDatabase c = new ConnectToDatabase(rp.getDatabaseURL(), rp.getDatabaseUsername(), rp.getDatabasePassword());
	}
}
