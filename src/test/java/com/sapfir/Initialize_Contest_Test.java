package com.sapfir;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Initialize_Contest_Test {

	private Properties prop;

	@BeforeClass
	public void setUp() {
		prop = new Properties();

		// Loading configuration properties
		try{
			FileInputStream fileStream = new FileInputStream("config.properties");
			prop.load(fileStream);
		} catch (IOException e){
			System.out.println("Error message: " + e.getMessage());
		}
	}

	@Test
	public void test() {
		System.out.println(prop.get("database_url"));
	}
}
