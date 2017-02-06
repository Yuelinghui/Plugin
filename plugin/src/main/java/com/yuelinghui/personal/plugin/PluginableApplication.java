package com.yuelinghui.personal.plugin;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by yuelinghui on 17/2/6.
 */

public class PluginableApplication extends Application {

    /**
     * 管理插件对应的resources
     */
    private final Map<String, Resources> mPluginResources = new WeakHashMap<String, Resources>(
            5);
    /**
     * 当前插件环境的资源对象
     */
    private PluginableResources mAppResources;
    /**
     * 管理插件打开的文件名/入口列表，第一次从文件取，之后内存和文件同步
     */
    private ArrayList<PluginInfo> mPluginInfos = new ArrayList<PluginInfo>();
    /**
     * 当前插件路径
     */
    private String mPluginPath = null;
    /**
     * 当前插件对应入口标识，可以有多个入口
     */
    private String mPluginEntry = null;

    /**
     * 文件存储插件路径 @see AppConfig
     */
    private SharedPreferences mDiskStorage;
    /**
     * 插件路径的存储key
     */
    private static final String KEY_CURR_INFO = "entries";

    /**
     * 获取插件对应的资源，为兼容旧版本设计
     *
     * @param apkPath
     *
     */
    public boolean openPlugin(String apkPath) {
        return openPlugin(apkPath, null);
    }

    /**
     * 获取插件对应的资源
     *
     * @param apkPath
     * @param apkEntry
     *            插件入口类，空代表原始类
     *
     */
    public boolean openPlugin(String apkPath, String apkEntry) {

        if (TextUtils.isEmpty(apkPath)) {
            return false;
        }

        File file = new File(apkPath);
        if (!file.exists()) {
            return false;
        }

        initPluginValue();

        if (apkPath == null) {
            apkPath = "";
        }
        if (apkEntry == null) {
            apkEntry = "";
        }

        mPluginPath = apkPath;
        mPluginEntry = apkEntry;

        setPluginValue();

        clearLayoutInflaterMap();

        return true;
    }

    /**
     * 存储当前打开的插件的信息
     */
    private void setPluginValue() {
        // 维护对应的插件信息在堆栈中的位置
        if (mPluginInfos.isEmpty()) {
            mPluginInfos.add(new PluginInfo(mPluginPath, mPluginEntry));
            persistPluginInfos();
        } else {
            PluginInfo plugin = mPluginInfos.get(mPluginInfos.size() - 1);
            if (!mPluginPath.equals(plugin.path)
                    || !mPluginEntry.equals(plugin.entry)) {
                mPluginInfos.add(new PluginInfo(mPluginPath, mPluginEntry));
                persistPluginInfos();
            } else if (!plugin.active) {
                plugin.active = true;
                persistPluginInfos();
            }
        }
    }

    /**
     * 存储到本地
     */
    private void persistPluginInfos() {
        try {
            if (mDiskStorage == null) {
                mDiskStorage = RunningEnvironment.sAppContext
                        .getSharedPreferences("modules", 0);
            }
            SharedPreferences.Editor editor = mDiskStorage.edit();
            editor.putString(KEY_CURR_INFO, new Gson().toJson(mPluginInfos));
            editor.commit();
        } catch (Exception e) {
        }
    }

    /**
     * 获取自动登录账户
     *
     * @return
     */
    private void initPluginValue() {
        if (mPluginInfos.isEmpty()) {
            try {
                if (mDiskStorage == null) {
                    mDiskStorage = RunningEnvironment.sAppContext
                            .getSharedPreferences("modules", 0);
                }
                String infos = mDiskStorage.getString(KEY_CURR_INFO, "");
                mPluginInfos = new Gson().fromJson(infos,
                        new TypeToken<ArrayList<PluginInfo>>() {
                        }.getType());
                if (mPluginInfos == null) {
                    mPluginInfos = new ArrayList<PluginInfo>();
                } else {
                    // 删除激活状态（异常关闭）的插件信息
                    PluginInfo plugin = null;
                    boolean persist = false;
                    for (int i = mPluginInfos.size() - 1; i >= 0; i--) {
                        plugin = mPluginInfos.get(i);
                        if (plugin.active) {
                            mPluginInfos.remove(i);
                            persist = true;
                        }
                    }
                    if (persist) {
                        persistPluginInfos();
                    }
                }
            } catch (Exception e) {
            }
        }

        if (!mPluginInfos.isEmpty()) {
            PluginInfo plugin = mPluginInfos.get(mPluginInfos.size() - 1);
            mPluginPath = plugin.path;
            mPluginEntry = plugin.entry;
        } else {
            mPluginPath = "";
            mPluginEntry = "";
        }
    }

    /**
     * 获取最新打开的插件信息，在closePlugin后拿到的是上一个插件的信息
     *
     * @return
     */
    public String getPlugin() {
        initPluginValue();
        return mPluginPath;
    }

    /**
     * 获取插件入口，在getPlugin后使用，在closePlugin后拿到的是上一个插件的信息
     *
     * @return
     */
    public String getPluginEntry() {
        if (TextUtils.isEmpty(mPluginPath)) {
            return null;
        }
        return mPluginEntry;
    }

    /**
     * 关闭插件，和openPlugin对应，维护插件列表
     *
     * @param apkPath
     * @param apkEntry
     *
     */
    public void closePlugin(String apkPath, String apkEntry) {
        // 维护对应的插件列表，通常是删除最后一个
        if (!mPluginInfos.isEmpty()) {
            if (apkPath == null) {
                apkPath = "";
            }
            if (apkEntry == null) {
                apkEntry = "";
            }
            PluginInfo plugin = null;
            for (int i = mPluginInfos.size() - 1; i >= 0; i--) {
                plugin = mPluginInfos.get(i);
                if (plugin.path.equals(apkPath)
                        && plugin.entry.equals(apkEntry)) {
                    mPluginInfos.remove(i);
                    persistPluginInfos();
                    break;
                }
            }
        }
    }

    /**
     * 查询插件是否已经装载过，plugin在使用期间不能重新装载
     *
     * @param apkPath
     *            插件文件路径
     * @return
     */
    public boolean hasPluginLoaded(String apkPath) {
        return PluginClassLoader.hasPluginLoaded(apkPath);
    }

    /**
     * 只保证在插件启动后的使用全局resource，可能有插件退出时被释放（@see releasePluginResource()），建议存储后使用。
     */
    @Override
    public Resources getResources() {
        if (mAppResources != null) {
            Resources res = mAppResources;
            if (res != null) {
                return res;
            }
        }

        return super.getResources();
    }

    public Resources getAppResources() {
        return super.getResources();
    }

    @Override
    public ClassLoader getClassLoader() {
        ClassLoader loader = null;
        if (!TextUtils.isEmpty(mPluginPath)) {
            loader = PluginClassLoader.getClassLoader(mPluginPath);
        }

        return (loader != null) ? loader : super.getClassLoader();
    }

    /**
     * 获取插件对应的资源
     *
     * @param apkPath
     *
     */
    public Resources getPluginResources(String apkPath) {
        if (TextUtils.isEmpty(apkPath)) {
            return super.getResources();
        }

        Resources res = null;
        res = mPluginResources.get(apkPath);

        if (res == null) {
            try {
                Resources mainRes = super.getResources();

                Class<?> assetManagerClass = Class
                        .forName("android.content.res.AssetManager");
                Object assetMag = assetManagerClass.newInstance();
                Method addAssetPathMethod = assetManagerClass
                        .getDeclaredMethod("addAssetPath", String.class);
                addAssetPathMethod.invoke(assetMag, apkPath);

                Constructor<?> resourcesConstructor = Resources.class
                        .getConstructor(assetManagerClass, mainRes
                                .getDisplayMetrics().getClass(), mainRes
                                .getConfiguration().getClass());
                res = (Resources) resourcesConstructor
                        .newInstance(assetMag, mainRes.getDisplayMetrics(),
                                mainRes.getConfiguration());
                if (res != null) {
                    mPluginResources.put(apkPath, res);
                }
            } catch (Exception e) {
            }

            addWebViewAssetPath(res);
        }

        if (res != null) {
            PluginableResources pluginRes = mAppResources != null ? mAppResources
                    : null;
            if (pluginRes == null || pluginRes.getPluginResources() != res) {
                mAppResources = new PluginableResources(res,
                        super.getResources());
            }
        }

        return res;
    }

    /**
     * 释放插件对应的资源
     *
     * @param apkPath
     *
     */
    public void releasePluginResources(String apkPath) {
        if (TextUtils.isEmpty(apkPath)) {
            return;
        }

        if (TextUtils.isEmpty(mPluginPath)) {
            if (mAppResources != null) {
                mAppResources = null;
            }
            return;
        }

        if (mPluginPath.equals(apkPath)) {
            if (mAppResources != null) {
                mAppResources = null;
            }
        }
    }

    /**
     * 释放插件相关资源，classloader等
     *
     * @param apkPath
     * @param apkEntry
     *
     */
    public void releasePlugin(String apkPath, String apkEntry) {
        if (TextUtils.isEmpty(apkPath)) {
            return;
        }

        // 释放插件资源
        releasePluginResources(apkPath);

        // 释放classloader
        boolean releaseClassLoader = true;

        if (!mPluginInfos.isEmpty()) {
            if (apkPath == null) {
                apkPath = "";
            }
            if (apkEntry == null) {
                apkEntry = "";
            }
            // 当前插件自动调用释放，需要维护插件状态，由于releasePlugin调用时机不确认，所以需要遍历更新
            // 插件列表中如果有多个相同的path+entry，可能有错误，但是插件已经自己有维护环境，还可以正常使用
            PluginInfo plugin = null;
            for (int i = mPluginInfos.size() - 1; i >= 0; i--) {
                plugin = mPluginInfos.get(i);
                if (plugin.path.equals(apkPath)
                        && plugin.entry.equals(apkEntry)) {
                    if (plugin.active) {
                        plugin.active = false;
                        persistPluginInfos();
                    }
                    break;
                }
            }

            for (int i = mPluginInfos.size() - 1; i >= 0; i--) {
                plugin = mPluginInfos.get(i);
                if (plugin.active) {
                    releaseClassLoader = false;
                    break;
                }
            }
        }

        if (releaseClassLoader) {
            // 恢复主程序环境
            Thread.currentThread().setContextClassLoader(
                    this.getClass().getClassLoader());
        }
    }

    /**
     * 清理LayoutInflater中缓存的CPFrame工程中的类
     */
    @SuppressWarnings("unchecked")
    private void clearLayoutInflaterMap() {
        Field field = null;
        try {
            field = LayoutInflater.class.getDeclaredField("sConstructorMap");
            if (field == null) {
                return;
            }
            field.setAccessible(true);
            HashMap<String, Constructor<? extends View>> map;
            map = (HashMap<String, Constructor<? extends View>>) field
                    .get(null);
            if (map == null || map.keySet() == null
                    || map.keySet().iterator() == null) {
                return;
            }
            Iterator<String> iter = map.keySet().iterator();
            while (iter.hasNext()) {
                String key = iter.next();
                if (!TextUtils.isEmpty(key)
                        && (key.startsWith("com.cpframe.")
                        || key.startsWith("com.fortysevendeg.")
                        || key.startsWith("com.jfeinstein.")
                        || key.startsWith("com.tortysevendeg.")
                        || key.startsWith("com.nineoldandroids.")
                        || key.startsWith("com.jd.plugin.scancodepay.")
                        || key.startsWith("com.jd.plugin.mealtickets.") || key
                        .startsWith("com.jd.plugin.receipt."))) {
                    iter.remove();
                }
            }
        } catch (Exception e) {
        } finally {
            try {
                if (field != null) {
                    field.setAccessible(false);
                }
            } catch (Exception ce) {
            }
        }
    }

    /**
     * 添加webview的资源路径
     *
     * @param res
     *            插件Resources对象
     */
    private void addWebViewAssetPath(Resources res) {
        if (res == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 21) {
            Method addAssetPathMethod = null;
            try {
                Class<?> assetManagerClass = Class
                        .forName("android.content.res.AssetManager");
                addAssetPathMethod = assetManagerClass.getDeclaredMethod(
                        "addAssetPath", String.class);
            } catch (Exception e) {
            }
            if (addAssetPathMethod != null) {
                try {
                    // 默认的webview的包名是com.google.android.webview（源码中写错成com.android.webview）
                    PackageInfo packageInfo1 = getPackageManager()
                            .getPackageInfo("com.android.webview", 0);
                    addAssetPathMethod.invoke(res.getAssets(),
                            packageInfo1.applicationInfo.sourceDir);
                    PackageInfo packageInfo2 = getPackageManager()
                            .getPackageInfo("com.google.android.webview", 0);
                    addAssetPathMethod.invoke(res.getAssets(),
                            packageInfo2.applicationInfo.sourceDir);
                } catch (Exception e) {
                    // 没有找到com.google.android.webview的包名，搜索所有报名找到含有webview字样，全部既加入资源路径
                    List<PackageInfo> packageInfoList = getPackageManager()
                            .getInstalledPackages(0);
                    for (PackageInfo packageInfo : packageInfoList) {
                        if (packageInfo.packageName.toLowerCase(Locale.ENGLISH)
                                .contains("webview")) {
                            try {
                                addAssetPathMethod.invoke(res.getAssets(),
                                        packageInfo.applicationInfo.sourceDir);
                            } catch (Exception e2) {
                            }
                        }

                    }
                }
            }

        }
    }

}
