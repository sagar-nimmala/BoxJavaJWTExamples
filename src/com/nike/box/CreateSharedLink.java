package com.nike.box;

import com.box.sdk.*;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

/**
 * Created by kdomen on 12/26/16.
 */
public class CreateSharedLink {

    private static final String CLIENT_ID = "";
    private static final String CLIENT_SECRET = "";
    private static final String ENTERPRISE_ID = "";
    private static final String PUBLIC_KEY_ID = "";
    private static final String PRIVATE_KEY_FILE = "";
    private static final String PRIVATE_KEY_PASSWORD = "";
    private static final int MAX_CACHE_ENTRIES = 100;
    private static final String FILE = "";
    private static final String USER_ID = "";    // <--- put the appUserId here
    private static final String FILE = ""



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


        BoxFolder kentestFolder = getChildFolder(rootFolder);
        kentestFolder.collaborate("p.kdomen@nike.com", BoxCollaboration.Role.EDITOR);

        String fileId = uploadFile(FILE, api, kentestFolder);

        BoxFile boxFile = new BoxFile(api, fileId);

        createSharedLink(boxFile);


    }

    public static BoxSharedLink createSharedLink(BoxFile boxFile) {
        BoxSharedLink.Permissions permissions = new BoxSharedLink.Permissions();
        permissions.setCanDownload(true);
        permissions.setCanPreview(true);

        Date unshareAt = null;

        BoxSharedLink sharedLink = boxFile.createSharedLink(BoxSharedLink.Access.OPEN, unshareAt, permissions);
        System.out.println("Download link: " + sharedLink.getDownloadURL());
        return sharedLink;
    }

    private static BoxFolder getChildFolder(BoxFolder rootFolder) {
        BoxFolder kentestFolder = null;
        Iterable<BoxItem.Info> items = rootFolder.getChildren();
        for (BoxItem.Info item : items) {
            if (item.getName().equals("kentest")) {
                kentestFolder = (BoxFolder)item.getResource();
            }
        }

        if (null == kentestFolder) {
            BoxFolder.Info info = rootFolder.createFolder("kentest");
            kentestFolder = info.getResource();
        }
        return kentestFolder;
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
                BoxFile.Info boxInfo = folder.uploadFile(stream, pathFileName);
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
