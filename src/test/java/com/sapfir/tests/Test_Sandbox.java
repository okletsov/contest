package com.sapfir.tests;

import com.sapfir.helpers.BrowserDriver;
import com.sapfir.helpers.Properties;
import com.sapfir.pageClasses.SandboxPage;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class Test_Sandbox {
	private WebDriver driver;
	private String sandboxBaseUrl;

	@BeforeClass
	public void setUp() {

		BrowserDriver bd = new BrowserDriver();
		driver = bd.getDriver();
		driver.manage().window().maximize();

		Properties prop = new Properties();
		sandboxBaseUrl = prop.getSandboxUrl();
	}

	@AfterClass
	public void tearDown() {
		driver.quit();
	}

	@Test
	public void testSimpleLogin() {
		driver.get(sandboxBaseUrl);

		SandboxPage sandbox = new SandboxPage(driver);
		sandbox.signIn();

	}
}
