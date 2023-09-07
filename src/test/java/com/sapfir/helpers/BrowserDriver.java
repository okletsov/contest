package com.sapfir.helpers;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v115.network.Network;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BrowserDriver {

    private ChromeDriver driver;
    private DevTools devTools;

    public BrowserDriver() {
        Properties prop = new Properties();
        String headless = prop.getHeadless();

        if (headless.equals("true")){
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            this.driver = new ChromeDriver(options);
            setDevTools(driver);
            limitChromeDriverLogs();
        } else if (headless.equals("false")){
            this.driver = new ChromeDriver();
            setDevTools(driver);
            limitChromeDriverLogs();
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

    public void limitChromeDriverLogs() {
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
