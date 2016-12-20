package com.nike.box;

import com.box.sdk.*;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 *   Note -- Run UploadFileAsAppUser.java first.  Put that userId in USER_ID below.
 */
public class GetAppUsersItems {

    private static final String CLIENT_ID = "";
    private static final String CLIENT_SECRET = "";
    private static final String ENTERPRISE_ID = "";
    private static final String PUBLIC_KEY_ID = "";
    private static final String PRIVATE_KEY_FILE = "";
    private static final String PRIVATE_KEY_PASSWORD = "";
    private static final int MAX_CACHE_ENTRIES = 100;
    private static final String USER_ID = "";    // <--- put the appUserId here

    public static void main(String[] args) throws IOException {
        {
            File file = new File(PRIVATE_KEY_FILE);
            byte[] fileData = new byte[(int) file.length()];
            DataInputStream dis = new DataInputStream(new FileInputStream(file));
            dis.readFully(fileData);
            dis.close();

            String privateKey = new String(fileData);

            JWTEncryptionPreferences encryptionPref = new JWTEncryptionPreferences();
            encryptionPref.setPublicKeyID(PUBLIC_KEY_ID);
            encryptionPref.setPrivateKey(privateKey);
            encryptionPref.setPrivateKeyPassword(PRIVATE_KEY_PASSWORD);
            encryptionPref.setEncryptionAlgorithm(EncryptionAlgorithm.RSA_SHA_256);

            IAccessTokenCache accessTokenCache = new InMemoryLRUAccessTokenCache(MAX_CACHE_ENTRIES);

            BoxDeveloperEditionAPIConnection userApi = BoxDeveloperEditionAPIConnection.getAppUserConnection(USER_ID, CLIENT_ID, CLIENT_SECRET, encryptionPref, accessTokenCache);

            BoxFolder boxFolder = BoxFolder.getRootFolder(userApi);
            Iterable<com.box.sdk.BoxItem.Info> items = boxFolder.getChildren("name");
            for (BoxItem.Info item : items) {
                System.out.println("\t" + item.getName());
            }
        }
    }
}
