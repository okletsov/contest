package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v134.network.Network;

import java.util.Optional;

public class BrowserDriver {

    private static final Logger Log = LogManager.getLogger(BrowserDriver.class.getName());

    private ChromeDriver driver;
    private DevTools devTools;

    public BrowserDriver() {
        Properties prop = new Properties();
        String headless = prop.getHeadless();
        setChromedriverPath();
        System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");

        if (headless.equals("true")){
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            options.addArguments("--window-size=1920,1080");
            this.driver = new ChromeDriver(options);
        } else if (headless.equals("false")){
            this.driver = new ChromeDriver();
        }
        assert driver != null;
        setDevTools(driver);
    }

    private void setDevTools(ChromeDriver driver) {
        this.devTools = driver.getDevTools();
        devTools.createSession();
        devTools.send(Network.enable(
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        ));
    }

    private void setChromedriverPath() {
        String osArch = System.getProperty("os.arch");
        if (osArch.equals("aarch64")) {
            String chromedriverPath = "/usr/bin/chromedriver";
            System.setProperty("webdriver.chrome.driver", chromedriverPath);
            Log.info("found " + osArch + " architecture, manually setting chromedriver path to: " + chromedriverPath);
        } else {
            Log.info("Using system's default chromedriver path");
        }
    }

    public ChromeDriver getDriver() {
        return driver;
    }

    public DevTools getDevTools() {
        return devTools;
    }
}