package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;

public class Properties {

    private static final Logger Log = LogManager.getLogger(Properties.class.getName());

    private final String databaseURL;
    private final String databaseUsername;
    private final String databasePassword;

    private final String siteUrl;
    private final String siteUsername;
    private final String sitePassword;

    private final String sandboxSiteUrl;
    private final String sandboxUsername;
    private final String sandboxPassword;

    private final String headless;
    private final String contestId;

    public Properties() {
        java.util.Properties properties = new java.util.Properties();

        try{
            Log.trace("Reading properties file...");
            FileInputStream fileStream = new FileInputStream("config.properties");
            properties.load(fileStream);
            Log.trace("Successfully read properties file...");
        } catch (IOException ex){
            Log.error(ex.getMessage());
            Log.trace("Stack trace: ", ex);
            System.exit(0);
        }

        this.databaseURL = properties.getProperty("database_url");
        this.databaseUsername = properties.getProperty("database_username");
        this.databasePassword = properties.getProperty("database_password");

        this.siteUrl = properties.getProperty("site_url");
        this.siteUsername = properties.getProperty("site_username");
        this.sitePassword = properties.getProperty("site_password");

        this.sandboxSiteUrl = properties.getProperty("sandbox_url");
        this.sandboxUsername = properties.getProperty("sandbox_username");
        this.sandboxPassword = properties.getProperty("sandbox_password");

        this.headless = properties.getProperty("headless");
        this.contestId = properties.getProperty("contest_id");
    }

    public String getDatabaseURL() {return databaseURL;}
    public String getDatabaseUsername() {return databaseUsername;}
    public String getDatabasePassword() {return databasePassword;}

    public String getSiteUrl() {return siteUrl;}
    public String getSiteUsername() {return siteUsername;}
    public String getSitePassword() {return sitePassword;}

    public String getSandboxUrl() {return sandboxSiteUrl;}
    public String getSandboxUsername() {return sandboxUsername;}
    public String getSandboxPassword() {return sandboxPassword;}

    public String getHeadless() {return headless;}
    public String getContestId() {return contestId;}
}
