package com.sapfir.apiUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class ResponseDecoder {

    private static final Logger Log = LogManager.getLogger(ResponseDecoder.class.getName());

    public String decodeResponse(String rawResponse) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        // Base64 decode
        String decodedResponse = new String(Base64.getDecoder().decode(rawResponse));
        String[] responseElements = decodedResponse.split(":");
        // TODO: add validation

        // Original password and salt
        final String password = "J*8sQ!p$7aD_fR2yW@gHn*3bVp#sAdLd_k";
        final String salt = "5b9a8f2c3e6d1a4b7c8e9d0f1a2b3c4d";
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
}
