package com.google.firebase.codelab.friendlychat.Utils;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;
import timber.log.Timber;

public class ManageFileUtils {

    private String directoryName;

    public ManageFileUtils() {
    }

    public void setDirectoryName(String directoryName) {
        this.directoryName = directoryName;
    }

    public boolean checkIfDirectoryExists() {
        if (!isExternalStorageWritable()
                || TextUtils.isEmpty(directoryName)) {
            return false;
        }

        File file = new File(Environment.getExternalStorageDirectory() + "/" + directoryName);
        if (!file.exists()) {
            return new File(file.getAbsolutePath()).mkdirs();
        }
        return file.exists();
    }

    public File getDirectoryRoot(String directoryName) {
        if (TextUtils.isEmpty(directoryName)) {
            return new File("");
        }
        return new File(Environment.getExternalStorageDirectory() + "/" + directoryName);
    }

    public File getFile(File rootFile, String fileName, String fileType) {
        if (TextUtils.isEmpty(fileName)
                || TextUtils.isEmpty(fileType)
                || !checkIfDirectoryExists()
                || !rootFile.exists()) {
            return new File("");
        }
        return new File(rootFile + "/" + fileName + fileType);
    }

    public boolean checkIfFileExists(File rootFile, String fileName, String fileType) {
        if (!rootFile.exists()
                || TextUtils.isEmpty(fileName)
                || TextUtils.isEmpty(fileType)) {
            return false;
        }
        return new File(rootFile + "/" + fileName + fileType).exists();
    }

    /* Checks if external storage is available for read and write */
    private boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /* Checks if external storage is available to at least read */
    private boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public File saveFile(File directoryRoot, String invoiceId, String fileTypePdf, ResponseBody body) {
        if (TextUtils.isEmpty(invoiceId)
                || TextUtils.isEmpty(fileTypePdf)
                || !checkIfDirectoryExists()
                || !directoryRoot.exists()
                || body == null
                || body.source() == null) {
            return new File("");
        }

        File destinationFile = getFile(directoryRoot, invoiceId, fileTypePdf);

        try {
            BufferedSink sink = Okio.buffer(Okio.sink(destinationFile));
            sink.writeAll(body.source());
            sink.close();
        } catch (IOException e) {
            Log.e("TAG", e.toString());
            return new File("");
        }
        return destinationFile;
    }

    public static File getFileFromAppExternalDirectory(String filename) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return null;
        }


        File root = null;

        try {
            root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString(), "FriendlyChat");
            root.mkdirs();
        } catch (Exception e) {
            Timber.d(e);
        }

        if (root == null) {
            return null;
        }

        return new File(root, filename);
    }
}
