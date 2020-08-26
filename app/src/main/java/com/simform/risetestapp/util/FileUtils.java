package com.simform.risetestapp.util;

import android.os.Environment;

import java.io.File;
import java.util.Random;

public class FileUtils {

    private static final String TAG = FileUtils.class.getSimpleName();

    private static final Random mTempFileRandom = new Random();

    public static String createTempFile(String ext) {
        File path = new File(Environment.getExternalStorageDirectory(), "Launchcloud");
        if (!path.exists() && !path.mkdirs()) {
            path = Environment.getExternalStorageDirectory();
        }

        File result;
        do {
            int value = Math.abs(mTempFileRandom.nextInt());
            result = new File(path, "launchcloud-" + value + "-" + ext);
        }
        while (result.exists());

        return result.getAbsolutePath();
    }
}
