package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;

public class Properties {

    private static final Logger Log = LogManager.getLogger(Properties.class.getName());

    private String databaseURL;
    private String databaseUsername;
    private String databasePassword;

    private String siteUrl;
    private String siteUsername;
    private String sitePassword;

    private String headless;

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

        this.headless = properties.getProperty("headless");
    }

    public String getDatabaseURL() {return databaseURL;}
    public String getDatabaseUsername() {return databaseUsername;}
    public String getDatabasePassword() {return databasePassword;}

    public String getSiteUrl() {return siteUrl;}
    public String getSiteUsername() {return siteUsername;}
    public String getSitePassword() {return sitePassword;}

    public String getHeadless() {return headless;}
}
