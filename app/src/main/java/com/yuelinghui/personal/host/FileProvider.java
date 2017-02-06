package com.yuelinghui.personal.host;

import android.content.Context;
import android.text.TextUtils;

import com.yuelinghui.personal.plugin.RunningEnvironment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by yuelinghui on 17/2/6.
 */

public class FileProvider {

    /**
     * 获取保存module的路径
     *
     * @return /data/data/wallet/files/moduleFolder
     */
    public static String getModulePath(String moduleFolder) {

        try {
            String modulePath = RunningEnvironment.sAppContext.getExternalCacheDir()
                    .getPath() + File.separator + moduleFolder + File.separator;
            if (!createFolder(modulePath)) {
                return "";
            }
            return modulePath;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 创建文件夹
     *
     * @param folderPath
     *            文件夹路径
     */
    public static boolean createFolder(String folderPath) {
        if (TextUtils.isEmpty(folderPath)) {
            return false;
        }

        File folder = new File(folderPath);
        if (folder.exists()) {
            if (isFolderReadable(folderPath)) {
                // 路径存在并可以读取，不用重新创建
                return true;
            }
            // 路径存在但是无法读取，删除路径
            folder.delete();
        }
        return folder.mkdirs();
    }

    public static String copyAssestFile(Context context, String assetFile,
                                        String destPath, String destFile) {
        if (context == null || TextUtils.isEmpty(assetFile)
                || TextUtils.isEmpty(destPath) || TextUtils.isEmpty(destFile)) {
            return null;
        }
        boolean result = false;
        InputStream asset = null;
        File dest = null;
        OutputStream output = null;
        try {
            if (isExist(destFile)) {
                result = true;
            } else {
                asset = context.getAssets().open(assetFile);
                if (asset != null) {
                    dest = createFile(destPath, destFile);
                    if (dest != null) {
                        output = new FileOutputStream(dest);
                        if (output != null) {
                            int readLen = 0;
                            byte[] buf = new byte[1024];
                            while ((readLen = asset.read(buf)) != -1) {
                                output.write(buf, 0, readLen);
                            }
                            result = true;
                        }
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
            } catch (Exception e) {
            }
            try {
                if (asset != null) {
                    asset.close();
                }
            } catch (Exception e) {
            }
            if (!result && dest != null) {
                dest.delete();
            }
        }
        return (result && dest != null) ? dest.getPath() : null;
    }

    /**
     * 在指定路径下创建文件 若路径不存在，先创建路径
     *
     * @param path
     *            文件路径
     * @param fileName
     *            文件名
     * @return 文件
     */
    public static File createFile(String path, String fileName) {
        if (TextUtils.isEmpty(path) || TextUtils.isEmpty(fileName)) {
            return null;
        }
        if (!createFolder(path)) {
            return null;
        }
        File file = new File(path + File.separator + fileName);
        try {
            file.createNewFile();
            return file;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 文件或文件夹是否存在
     *
     * @return
     */
    public static boolean isExist(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        File file = new File(path);
        return file.exists();
    }

    /**
     * 文件夹是否可以读取
     *
     * @return
     */
    private static boolean isFolderReadable(String folderPath) {
        File tempFile = new File(folderPath + "temp.txt");
        FileOutputStream tempStream = null;
        try {
            tempStream = new FileOutputStream(tempFile);
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                tempStream.close();
            } catch (Exception e) {
            }
            try {
                tempFile.delete();
            } catch (Exception e) {
            }
        }
    }
}
