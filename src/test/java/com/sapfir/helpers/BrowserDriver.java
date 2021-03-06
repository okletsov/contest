package com.sapfir.helpers;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class BrowserDriver {

    private WebDriver driver;

    public BrowserDriver() {
        Properties prop = new Properties();
        String headless = prop.getHeadless();

        if (headless.equals("true")){
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            this.driver = new ChromeDriver(options);
        } else if (headless.equals("false")){
            this.driver = new ChromeDriver();
        }
    }

    public WebDriver getDriver() {
        return driver;
    }
}
