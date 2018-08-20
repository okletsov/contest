package com.sapfir.tests;

import com.sapfir.helpers.ContestOperations;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class Initialize_Contest_Test {

	@BeforeClass
	public void setUp() {

	}

	@AfterClass
	public void tearDown() {

	}

	@Test
	public void test() {
		ContestOperations co = new ContestOperations();
		co.addSeasonalContest("2019", "Winter");
	}
}