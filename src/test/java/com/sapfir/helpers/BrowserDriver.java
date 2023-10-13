package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v92.network.Network;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BrowserDriver {

    private static final org.apache.logging.log4j.Logger Log = LogManager.getLogger(BrowserDriver.class.getName());

    private ChromeDriver driver;
    private DevTools devTools;

    public BrowserDriver() {
        Properties prop = new Properties();
        String headless = prop.getHeadless();

        String osName = System.getProperty("os.name");
        Log.info("Operating system: " + osName);

        if (osName.contains("Linux")) {
            System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver");
        }

        if (headless.equals("true")){
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            options.addArguments("--window-size=1920,1080");
            this.driver = new ChromeDriver(options);
            setDevTools(driver);
            limitLogs();
        } else if (headless.equals("false")){
            this.driver = new ChromeDriver();
            setDevTools(driver);
            limitLogs();
        }
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

    public void limitLogs() {
        System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");
        System.setProperty("webdriver.chrome.silentOutput", "true");
        Logger.getLogger("org.openqa.selenium").setLevel(Level.WARNING);
    }

    public ChromeDriver getDriver() {
        return driver;
    }

    public DevTools getDevTools() {
        return devTools;
    }
}
