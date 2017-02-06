package com.yuelinghui.personal.host;

import java.io.File;

/**
 * Created by yuelinghui on 17/2/6.
 */

public class App extends PluginableApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        RunningEnvironment.init(this);
    }

    @Override
    public boolean openPlugin(String apkPath) {
        String NEWASSETFILE = "test.apk";
        final String folderPath = FileProvider
                .getModulePath("TEST");
        final String fileName = "test.apk";
        File file = new File(folderPath + fileName);
        if (!file.exists()) {
            file.deleteOnExit();
            try {
                FileProvider.copyAssestFile(RunningEnvironment.sAppContext,
                        NEWASSETFILE, folderPath, fileName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        apkPath = file.getAbsolutePath();
        return super.openPlugin(apkPath);
    }

//    private static boolean testPluginFirstCreate = true;

}
