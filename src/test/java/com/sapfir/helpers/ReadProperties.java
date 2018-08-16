package com.sapfir.helpers;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ReadProperties {

    private String databaseURL;
    private String databaseUsername;
    private String databasePassword;

    public ReadProperties() {
        Properties prop = new Properties();

        try{
            FileInputStream fileStream = new FileInputStream("config.properties");
            prop.load(fileStream);
        } catch (IOException e){
            System.out.println("Error message: " + e.getMessage());
        }

        this.databaseURL = prop.getProperty("database_url");
        this.databaseUsername = prop.getProperty("database_username");
        this.databasePassword = prop.getProperty("database_password");
    }

    public String getDatabaseURL() {return databaseURL;}
    public String getDatabaseUsername() {return databaseUsername;}
    public String getDatabasePassword() {return databasePassword;}

}
