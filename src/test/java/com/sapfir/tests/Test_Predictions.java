package com.sapfir.tests;

import com.sapfir.apiUtils.*;
import com.sapfir.helpers.*;
import com.sapfir.pageClasses.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.DevTools;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

public class Test_Predictions {

    private final DatabaseOperations dbOp = new DatabaseOperations();
    private Connection conn = null;
    private ChromeDriver driver;
    private String followingJson;

    private ApiHelpers apiHelpers;

    @BeforeClass
    public void setUp() {

        conn = dbOp.connectToDatabase();

        // Setting up ChromeDriver
        BrowserDriver bd = new BrowserDriver();
        driver = bd.getDriver();
        driver.manage().window().maximize();

        Properties prop = new Properties();
        String baseUrl = prop.getSiteUrl();
        driver.get(baseUrl);

        // Getting necessary page classes
        HomePageBeforeLogin hpbl = new HomePageBeforeLogin(driver);
        LoginPage lp = new LoginPage(driver);
        CommonElements ce = new CommonElements(driver);
        ProfilePage pp = new ProfilePage(driver);
        JsonHelpers jsonHelpers = new JsonHelpers();

        // Getting access to devTools
        DevTools devTools = bd.getDevTools();

        // Setting up a listener to monitor and save json response with the list of participants
        DevToolsHelpers dtHelpers = new DevToolsHelpers();
        dtHelpers.captureResponseBody(devTools, "ajax-following");
        dtHelpers.captureRequestHeaders(devTools, "/ajax-communityFeed/profile/24836901");

        // Setting up a listener to monitor and save user-data json
        DevToolsHelpers dtHelpers2 = new DevToolsHelpers();
        dtHelpers2.captureResponseBody(devTools, "ajax-user-data");

        // Performing actions in UI
        // ce.clickRejectAllCookiesButton(); -- website behavior changed, this button no longer shown
        hpbl.clickLogin();
        lp.signIn();
        ce.openProfilePage();
        pp.viewParticipants();
        pp.clickFeedTab();

        // Capturing json response with the list of participants
        try {
            this.followingJson = decodeResponse(dtHelpers.getResponseBody());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Capturing request headers, usePremium and bookieHash to be used in subsequent API calls

        // Capturing request headers
        HashMap<String, String> requestHeaders = dtHelpers.getRequestHeaders();

        // Getting "bookieHash" and "usePremium" values (to be used in generating tournament results URL)
        String userDataJS = dtHelpers2.getResponseBody();
        String userDataJason = jsonHelpers.getJsonFromJsCode(userDataJS, "pageOutrightsVar");

        UserDataParser userDataParser = new UserDataParser(userDataJason);
        String usePremium = userDataParser.getUsePremium();
        String bookieHash = userDataParser.getBookieHash();
        assert !usePremium.isEmpty() && !bookieHash.isEmpty();

        // Creating an instance of ApiHelpers class
        this.apiHelpers = new ApiHelpers(usePremium, bookieHash, requestHeaders);
    }

     private byte[] decodeDynamicKey(String key) {
        // sample key = "5dec1dd50e1135ccc169be257859da4d";
        // expected value [93, 236, 29, 213, 14, 17, 53, 204, 193, 105, 190, 37, 120, 89, 218, 77]
        // Split the string into chunks of 2 characters
        List<String> hexPairs = new ArrayList<>();
        for (int i = 0; i < key.length(); i += 2) {
            hexPairs.add(key.substring(i, i + 2));
        }

        // Convert each hex pair to a byte
        byte[] byteArray = new byte[hexPairs.size()];
        for (int i = 0; i < hexPairs.size(); i++) {
            byteArray[i] = (byte) Integer.parseInt(hexPairs.get(i), 16);
        }
        return byteArray;
    }

    @Test private String decodeResponse(String rawResponse) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        // Base64 decode
        String decodedResponse = new String(Base64.getDecoder().decode(rawResponse));
        String[] responseElements = decodedResponse.split(":");
        // TODO: add validation

        // Original password and salt
        final String password = "%RtR8AB&nWsh=AQC+v!=pgAe@dSQG3kQ";
        final String salt = "orieC_jQQWRmhkPvR6u2kzXeTube6aYupiOddsPortal";
        byte[] iv = decodeDynamicKey(responseElements[1]);
        byte[] encryptedData = Base64.getDecoder().decode(responseElements[0]);

        // Key derivation using PBKDF2
        int iterations = 1000;
        int keyLength = 256;
        PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray(), salt.getBytes(StandardCharsets.UTF_8), iterations, keyLength);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = keyFactory.generateSecret(pbeKeySpec).getEncoded();

        // Convert PBKDF2 key to AES key
        SecretKeySpec aesKey = new SecretKeySpec(keyBytes, "AES");

        // AES decryption
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, aesKey, ivSpec);
        byte[] decryptedData = cipher.doFinal(encryptedData);

        // Output decrypted data
        return new String(decryptedData, StandardCharsets.UTF_8);
    }
    @AfterClass
    public void tearDown() {
        driver.quit();

//        Insert background job timestamp

        BackgroundJobs bj = new BackgroundJobs(conn);
        String jobName = Test_Predictions.class.getSimpleName();
        bj.addToBackgroundJobLog(jobName);

//        Close connection
        dbOp.closeConnection(conn);
        
    }

    @Test(dataProvider = "participants", dataProviderClass = Participants.class)
    public void testPredictions(String username) {

        // Getting user id from a json for a user from data provider
        FollowingUsersParser followingUsersParser = new FollowingUsersParser(followingJson);
        String jsonUserId = followingUsersParser.getUserIdByUsername(followingJson, username);

        int urlSuffix = 0;
        List<String> predictions;

        do {
            /*
                The logic in a do-while loop will:
                    - make a call to get first 20 predictions
                    - if the number of predictions equals to 20, API url will be changed to pull 20 more predictions
                    - it will keep making more calls until the number of returned predictions is less than 20
             */

            // Making a call to get a json with the list of predictions
            String requestUrl = apiHelpers.generatePredictionsRequestUrl(jsonUserId, urlSuffix);
            String predictionsJson = apiHelpers.makeApiRequest(requestUrl);

            // Getting the list of feed item ids from json
            JsonHelpers jsonHelpers = new JsonHelpers();
            predictions = jsonHelpers.getParentFieldNames(predictionsJson, "/d/feed");

            // Getting prediction metadata
            for (String predictionId: predictions) {

                // If statement has bad ids
                PredictionParser parser = new PredictionParser(predictionsJson, predictionId);
                if (!parser.isPredictionBroken()) {
                    PredictionOperations predOp = new PredictionOperations(conn, apiHelpers, predictionsJson, predictionId);
                    boolean predictionExist = predOp.checkIfExist(predictionId);

                    if (!predictionExist){
                        predOp.addPrediction(username);
                    } else {
                        boolean predictionFinalized = predOp.predictionFinalized(predictionId, username);

                        if (!predictionFinalized) {
                            predOp.updatePrediction();
                        }
                    }
                }
            }

            urlSuffix += 20;
        } while (predictions.size() == 20);

        // Individually inspect not settled predictions with dateScheduled in the past

        PredictionOperations predOp2 = new PredictionOperations(conn);
        predictions = predOp2.getInPlayPredictionsByUsername(username);

        for (String predictionId: predictions) {

            String requestUrl = apiHelpers.generateIndividualPredictionRequestUrl(predictionId);
            String predictionJson = apiHelpers.makeApiRequest(requestUrl);

            PredictionOperations predOp3 = new PredictionOperations(conn, apiHelpers, predictionJson, predictionId);
            predOp3.updatePrediction();
        }

    }
}