package com.nike.box;

import com.box.sdk.*;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 *   Note -- Run CreateAppUser.java and create an AppUser and get the userId.  Put that userId in USER_ID below.
 */
public class UploadFileAsAppUser {

    private static final String CLIENT_ID = "";
    private static final String CLIENT_SECRET = "";
    private static final String ENTERPRISE_ID = "";
    private static final String PUBLIC_KEY_ID = "";
    private static final String PRIVATE_KEY_FILE = "";
    private static final String PRIVATE_KEY_PASSWORD = "";
    private static final int MAX_CACHE_ENTRIES = 100;
    private static final String FILE = "";
    private static final String USER_ID = "";    // <--- put the appUserId here


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

        BoxDeveloperEditionAPIConnection api = BoxDeveloperEditionAPIConnection.getAppUserConnection(USER_ID, CLIENT_ID,
                CLIENT_SECRET, encryptionPref, accessTokenCache);

        BoxFolder rootFolder = BoxFolder.getRootFolder(api);
        System.out.println("folder owner: " + rootFolder.getInfo().getOwnedBy().getName());

        String fileId = uploadFile(FILE, api, rootFolder);

        BoxFile boxFile = new BoxFile(api, fileId);
        String ownerLogin = boxFile.getInfo().getOwnedBy().getName();

        System.out.println("file owner name: " + ownerLogin);
    }

    private static String uploadFile(String pathFileName, BoxAPIConnection api, BoxFolder folder) {
        boolean fileExists = false;
        String fileId = null;

        try {
            String fileName = pathFileName.substring(pathFileName.lastIndexOf("/")+1, pathFileName.length());

            for (BoxItem.Info itemInfo : folder) {
                if (itemInfo instanceof BoxFile.Info) {
                    BoxFile.Info fileInfo = (BoxFile.Info) itemInfo;
                    if (fileName.equals(fileInfo.getName())) {
                        fileExists = true;
                        fileId = fileInfo.getID();
                    }
                }
            }

            if (!fileExists) {
                System.out.println("uploading new file: " + fileName);
                FileInputStream stream = new FileInputStream(pathFileName);
                BoxFile.Info boxInfo = folder.uploadFile(stream, fileName);
                fileId = boxInfo.getID();
                stream.close();
            }
            else {
                System.out.println("uploading new version of file: " + fileName);
                BoxFile file = new BoxFile(api, fileId);
                FileInputStream stream = new FileInputStream(pathFileName);
                file.uploadVersion(stream);
                stream.close();
            }
        }
        catch (IOException e) {
            System.out.println(e);
        }
        return fileId;
    }
}
