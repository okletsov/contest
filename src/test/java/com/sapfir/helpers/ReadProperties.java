package com.sapfir.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ReadProperties {

    private static final Logger Log = LogManager.getLogger(ReadProperties.class.getName());

    private String databaseURL;
    private String databaseUsername;
    private String databasePassword;

    public ReadProperties() {
        Properties prop = new Properties();

        try{
            Log.info("Reading properties file...");
            FileInputStream fileStream = new FileInputStream("config.properties");
            prop.load(fileStream);
            Log.info("Successfully read properties file...");
        } catch (IOException ex){
            Log.error(ex.getMessage());
            Log.trace("Stack trace: ", ex);
        }

        this.databaseURL = prop.getProperty("database_url");
        this.databaseUsername = prop.getProperty("database_username");
        this.databasePassword = prop.getProperty("database_password");
    }

    public String getDatabaseURL() {return databaseURL;}
    public String getDatabaseUsername() {return databaseUsername;}
    public String getDatabasePassword() {return databasePassword;}

}
