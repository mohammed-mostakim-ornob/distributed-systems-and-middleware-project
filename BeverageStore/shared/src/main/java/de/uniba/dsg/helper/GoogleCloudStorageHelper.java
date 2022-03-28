package de.uniba.dsg.helper;

import com.google.cloud.storage.*;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

public class GoogleCloudStorageHelper {
    private static final Storage storage = StorageOptions.getDefaultInstance()
            .getService();

    public static void createFile(String bucketName, String fileName, byte[] fileBytes, Map<String, String> metadata) {
        BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, fileName))
                .setMetadata(metadata)
                .build();

        storage.create(blobInfo, fileBytes);
    }

    public static File downloadFile(String bucketName, String fileName) {
        Path path = Path.of("/tmp/" + fileName);

        Blob blob = StorageOptions.getDefaultInstance()
                .getService()
                .get(bucketName)
                .get(fileName);

        blob.downloadTo(path);

        return new File(path.toUri());
    }
}
