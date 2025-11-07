package com.example.assignment.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FileUtils {

    public static String getRealPathFromURI(Context context, Uri uri) {
        String result = null;
        try {
            if (uri.getScheme().equals("content")) {
                String[] projection = { MediaStore.Images.Media.DATA };
                Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        result = cursor.getString(columnIndex);
                    }
                    cursor.close();
                }
            } else if (uri.getScheme().equals("file")) {
                result = uri.getPath();
            }
        } catch (Exception e) {
            // Fallback to copying file to temp location
            result = copyFileToTempLocation(context, uri);
        }
        return result;
    }

    private static String copyFileToTempLocation(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            // Create temp file
            File tempFile = new File(context.getCacheDir(), "temp_image_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            // Copy data
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();

            return tempFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
