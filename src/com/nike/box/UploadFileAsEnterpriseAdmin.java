package com.nike.box;

import com.box.sdk.*;
import org.apache.log4j.Logger;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by kdomen on 12/6/16.
 */
public class UploadFileAsEnterpriseAdmin {
    private static Logger logger = Logger.getLogger(GetManagedUsersItems.class);

    private static final String CLIENT_ID = "";
    private static final String CLIENT_SECRET = "";
    private static final String ENTERPRISE_ID = "";
    private static final String PUBLIC_KEY_ID = "";
    private static final String PRIVATE_KEY_FILE = "private_key.pem";
    private static final String PRIVATE_KEY_PASSWORD = "!";
    private static final String APP_USER_NAME = "NikeService";
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

        BoxFolder rootFolder = BoxFolder.getRootFolder(api);

        String fileId = uploadFile("/Users/kdomen/Downloads/ken.jpg", api, rootFolder);

        BoxFile boxFile = new BoxFile(api, fileId);
        String ownerLogin = boxFile.getInfo().getOwnedBy().getLogin();

        System.out.println("ownerLogin: " + ownerLogin);
    }

    private static String uploadFile(String pathFileName, BoxAPIConnection api, BoxFolder folder) {
        boolean fileExists = false;
        String fileId = null;

        try {
            String fileName = pathFileName.substring(pathFileName.lastIndexOf("/")+1, pathFileName.length());
            logger.info("fileName: " + fileName);

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
                logger.info("uploading new file: " + fileName);
                FileInputStream stream = new FileInputStream(pathFileName);
                BoxFile.Info boxInfo = folder.uploadFile(stream, pathFileName);
                fileId = boxInfo.getID();
                stream.close();
            }
            else {
                logger.info("uploading new version of file: " + fileName);
                BoxFile file = new BoxFile(api, fileId);
                FileInputStream stream = new FileInputStream(pathFileName);
                file.uploadVersion(stream);
                stream.close();
            }
        }
        catch (IOException e) {
            logger.fatal("IOException", e);
        }
        return fileId;
    }
}
