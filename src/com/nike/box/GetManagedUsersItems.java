package com.nike.box;

import com.box.sdk.*;
import org.apache.log4j.Logger;
import java.io.*;


public class GetManagedUsersItems {

    private static Logger logger = Logger.getLogger(GetManagedUsersItems.class);

    private static final String CLIENT_ID = "";
    private static final String CLIENT_SECRET = "";
    private static final String ENTERPRISE_ID = "";
    private static final String PUBLIC_KEY_ID = "";
    private static final String PRIVATE_KEY_FILE = "";
    private static final String PRIVATE_KEY_PASSWORD = "";
    private static final int MAX_CACHE_ENTRIES = 100;


    public static void main(String[] args) throws Exception {
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

        BoxDeveloperEditionAPIConnection api = BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(
                ENTERPRISE_ID, CLIENT_ID, CLIENT_SECRET, encryptionPref, accessTokenCache);

        BoxUser.Info userInfo = BoxUser.getCurrentUser(api).getInfo();
        System.out.format("Welcome, %s!\n\n", userInfo.getName());

        Iterable<com.box.sdk.BoxUser.Info> managedUsers = BoxUser.getAllEnterpriseUsers(api);
        for (BoxUser.Info managedUser : managedUsers) {
            System.out.println(managedUser.getName() + " " + managedUser.getStatus());
            if (managedUser.getStatus().equals(BoxUser.Status.ACTIVE)) {

                // Used to get AppUser or ManagedUser
                BoxDeveloperEditionAPIConnection userApi = BoxDeveloperEditionAPIConnection.getAppUserConnection(managedUser.getID(), CLIENT_ID, CLIENT_SECRET, encryptionPref, accessTokenCache);

                BoxFolder boxFolder = BoxFolder.getRootFolder(userApi);
                Iterable<com.box.sdk.BoxItem.Info> items = boxFolder.getChildren("name");
                for (BoxItem.Info item : items) {
                    System.out.println("\t" + item.getName());
                }
                break;
                // 400 - they haven't accepted TOC
                // 403 - INACTIVE
            }
        }
    }
}