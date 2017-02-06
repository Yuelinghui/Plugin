package com.yuelinghui.personal.plugin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by yuelinghui on 17/2/6.
 */

public class LibManager {


    private static final String TAG = "plugin";
    public static final String CPU_ARMEABI = "armeabi";
    public static final String CPU_X86 = "x86";
    public static final String CPU_MIPS = "mips";
    public static final String PREFERENCE_NAME = "plugin_pref";

    /**
     * So File executor
     */
    private ExecutorService mSoExecutor = Executors.newCachedThreadPool();
    /**
     * single instance of the SoLoader
     */
    private static LibManager sInstance = new LibManager();
    /**
     * app's lib dir
     */
    private static String sNativeLibDir = "";
    /**
     * 复制线程数
     */
    private int mCopyTaskNum = 0;

    private final int CopyTaskNumAdd1 = 0;
    private final int CopyTaskNumMinus1 = 1;
    private final int CopyTaskNumGet = 2;
    /**
     * 遍历结束标识
     */
    private boolean mIsTraversalDone;

    private LibManager() {
    }

    /**
     * @return
     */
    public static LibManager getLibLoader() {
        return sInstance;
    }

    /**
     * get cpu name, according cpu type parse relevant so lib
     *
     * @return ARM、ARMV7、X86、MIPS
     */
    private String getCpuName() {
        try {
            FileReader fr = new FileReader("/proc/cpuinfo");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            br.close();
            String[] array = text.split(":\\s+", 2);
            if (array.length >= 2) {
                return array[1];
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @SuppressLint("DefaultLocale")
    private String getCpuArch(String cpuName) {
        String cpuArchitect = CPU_ARMEABI;
        if (cpuName.toLowerCase().contains("arm")) {
            cpuArchitect = CPU_ARMEABI;
        } else if (cpuName.toLowerCase().contains("x86")) {
            cpuArchitect = CPU_X86;
        } else if (cpuName.toLowerCase().contains("mips")) {
            cpuArchitect = CPU_MIPS;
        }

        return cpuArchitect;
    }

    /**
     * copy so lib to specify directory(/data/data/host_pack_name/pluginlib)
     *
     * @param dexPath
     *            plugin path
     * @param nativeLibDir
     *            nativeLibDir
     */
    public void copyPluginSoLib(Context context, String dexPath,
                                String nativeLibDir) {
        if (!TextUtils.isEmpty(dexPath)) {
            mIsTraversalDone = false;
            String cpuName = getCpuName();
            String cpuArchitect = getCpuArch(cpuName);

            sNativeLibDir = nativeLibDir;
            Log.d(TAG, "cpuArchitect: " + cpuArchitect);
            try {
                ZipFile zipFile = new ZipFile(dexPath);
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry zipEntry = (ZipEntry) entries.nextElement();
                    if (zipEntry.isDirectory()) {
                        continue;
                    }
                    String zipEntryName = zipEntry.getName();
                    if (zipEntryName.endsWith(".so")
                            && zipEntryName.contains(cpuArchitect)) {
                        final long lastModify = zipEntry.getTime();
                        if (lastModify == getSoLastModifiedTime(context,
                                zipEntryName)) {
                            // exist and no change
                            Log.d(TAG,
                                    "skip copying, the so lib is exist and not change: "
                                            + zipEntryName);
                            continue;
                        }
                        operateTaskNum(CopyTaskNumAdd1);
                        mSoExecutor.execute(new CopySoTask(context, zipFile,
                                zipEntry, lastModify));
                    }
                }
                mIsTraversalDone = true;
                if (operateTaskNum(CopyTaskNumGet) == 0) {
                    zipFile.close();
                }
            } catch (IOException e) {
                mIsTraversalDone = true;
                e.printStackTrace();
            }
        }
    }

    /**
     * @author mrsimple
     */
    private class CopySoTask implements Runnable {

        private String mSoFileName;
        private ZipFile mZipFile;
        private ZipEntry mZipEntry;
        private Context mContext;
        private long mLastModityTime;

        CopySoTask(Context context, ZipFile zipFile, ZipEntry zipEntry,
                   long lastModify) {
            mZipFile = zipFile;
            mContext = context;
            mZipEntry = zipEntry;
            mSoFileName = parseSoFileName(zipEntry.getName());
            mLastModityTime = lastModify;
        }

        private final String parseSoFileName(String zipEntryName) {
            return zipEntryName.substring(zipEntryName.lastIndexOf("/") + 1);
        }

        private void writeSoFile2LibDir() throws IOException {
            InputStream is = null;
            FileOutputStream fos = null;
            is = mZipFile.getInputStream(mZipEntry);
            fos = new FileOutputStream(new File(sNativeLibDir, mSoFileName));
            copy(is, fos);
            operateTaskNum(CopyTaskNumMinus1);
            if (operateTaskNum(CopyTaskNumGet) == 0 && mIsTraversalDone) {
                try {
                    mZipFile.close();
                } catch (Exception e) {
                }
            }
        }

        /**
         * 输入输出流拷贝
         *
         * @param is
         * @param os
         */
        public void copy(InputStream is, OutputStream os) throws IOException {
            if (is == null || os == null)
                return;
            BufferedInputStream bis = new BufferedInputStream(is);
            BufferedOutputStream bos = new BufferedOutputStream(os);
            int size = getAvailableSize(bis);
            byte[] buf = new byte[size];
            int i = 0;
            while ((i = bis.read(buf, 0, size)) != -1) {
                bos.write(buf, 0, i);
            }
            bos.flush();
            bos.close();
            bis.close();
        }

        private int getAvailableSize(InputStream is) throws IOException {
            if (is == null)
                return 0;
            int available = is.available();
            return available <= 0 ? 1024 : available;
        }

        @Override
        public void run() {
            try {
                writeSoFile2LibDir();
                setSoLastModifiedTime(mContext, mZipEntry.getName(),
                        mLastModityTime);
                Log.d(TAG, "copy so lib success: " + mZipEntry.getName());
            } catch (IOException e) {
                Log.e(TAG, "copy so lib failed: " + e.toString());
                e.printStackTrace();
            }

        }

    }

    @SuppressLint("InlinedApi")
    public void setSoLastModifiedTime(Context cxt, String soName, long time) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            SharedPreferences prefs = cxt.getSharedPreferences(PREFERENCE_NAME,
                    Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
            prefs.edit().putLong(soName, time).apply();
        } else {
            SharedPreferences prefs = cxt.getSharedPreferences(PREFERENCE_NAME,
                    Context.MODE_PRIVATE);
            prefs.edit().putLong(soName, time).commit();
        }
    }

    @SuppressLint("InlinedApi")
    public long getSoLastModifiedTime(Context cxt, String soName) {
        SharedPreferences prefs = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            prefs = cxt.getSharedPreferences(PREFERENCE_NAME,
                    Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
        } else {
            prefs = cxt.getSharedPreferences(PREFERENCE_NAME,
                    Context.MODE_PRIVATE);
        }
        return prefs.getLong(soName, 0);
    }

    /**
     * 操作mCopyTaskNum
     *
     * @param function
     * @return
     */
    private synchronized int operateTaskNum(int function) {
        switch (function) {
            case CopyTaskNumAdd1:
                mCopyTaskNum++;
                return -1;
            case CopyTaskNumMinus1:
                mCopyTaskNum--;
                return -1;
            case CopyTaskNumGet:
                return mCopyTaskNum;
            default:
                return -1;
        }
    }
}
