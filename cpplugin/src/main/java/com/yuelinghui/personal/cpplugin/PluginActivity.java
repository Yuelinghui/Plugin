package com.yuelinghui.personal.cpplugin;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Created by yuelinghui on 17/2/6.
 */

public class PluginActivity extends FragmentActivity {


    /**
     * 外部传入数据key
     */
    public static final String EXTRAL_PARAM = "extralParam";
    /**
     * 插件activity需要装载的context
     */
    public static final String EXTRA_PLUGINCONTEXT = "pluginClass";

    /**
     * 插件优化路径
     */
    private static final String PLUGIN_DEX_PATH = "plugin";
    /**
     * 插件apk全路径
     */
    private static final String PLUGIN_PATH = "pluginPath";
    /**
     * 插件包名
     */
    private static final String PLUGIN_PACKAGE = "pluginPackage";
    /**
     * 插件activity类名字
     */
    private static final String PLUGIN_CLASS = "pluginClass";
    /**
     * 插件函数提供者key
     */
    private static final String PLUGIN_FUNCTION_PROVIDER = "pluginFunctionProvider";
    /**
     * 插件函数提供者key
     */
    private static final String PLUGIN_CALLBACK = "pluginCallback";
    /**
     * native库所在的目录名
     */
    public static final String PLUGIN_NATIVE_LIB_PATH = "pluginNativeLib";

    /**
     * 插件回调
     */
    private String mPluginCallback = null;

    /**
     * 是否是result方式回调
     */
    private boolean isCallbackForResult = false;
    /**
     * 插件文件路径
     */
    private String mPluginPath;
    /**
     * 插件包名
     */
    private String mPluginPackage;
    /**
     * 插件activity对应的类名，如果为空，则对应plugin类
     */
    private String mPluginClass = "";
    /**
     * 插件的资源上下文
     */
    private Resources mPluginResources;
    /**
     * 插件的类装载器
     */
    private ClassLoader mPluginClassLoader;
    /**
     * 插件退出标识，和系统的mFinished区分
     */
    private boolean mPluginFinished = false;
    /**
     * 插件上下文
     */
    protected PluginContext mPluginContext = null;

    /**
     * 页面数据key
     */
    private static final String EXTRA_UIDATA = "com.wangyin.plugin.extra.UIDATA";
    /**
     * 业务数据
     */
    public UIData mUIData = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        PluginBundle activityBundle = (getIntent().getExtras() == null) ? null
                : new PluginBundle(getIntent().getExtras());
        PluginBundle savedBundle = (savedInstanceState == null) ? null
                : new PluginBundle(savedInstanceState);

        initPluginPath();
        Thread pluginThread = new Thread(new Runnable() {

            @Override
            public void run() {
                initPluginClassLoader();
            }
        });
        pluginThread.start();
        try {
            pluginThread.join();
            if (mPluginClassLoader != null) {
                Thread.currentThread()
                        .setContextClassLoader(mPluginClassLoader);
            } else {
                mPluginFinished = true;
            }
        } catch (InterruptedException e) {
            mPluginFinished = true;
            e.printStackTrace();
        }

//        // 确认恢复的数据和本地获取的数据是否一致
//        if (!mPluginFinished) {
//            if (activityBundle != null) {
//                try {
//                    activityBundle.injectHost();
//
//                    String destPluginClass = activityBundle
//                            .getString(EXTRA_PLUGINCONTEXT);
//                    if (!TextUtils.isEmpty(destPluginClass)
//                            && !destPluginClass.equals(mPluginClass)) {
//                        mPluginFinished = true;
//                    }
//                } finally {
//                    activityBundle.restoreHost();
//                }
//            }
//
//            if (savedBundle != null) {
//                try {
//                    savedBundle.injectHost();
//
//                    String destPluginPath = savedBundle.getString(PLUGIN_PATH);
//                    if (!TextUtils.isEmpty(destPluginPath)
//                            && !destPluginPath.equals(mPluginPath)) {
//                        mPluginFinished = true;
//                    }
//                } finally {
//                    savedBundle.restoreHost();
//                }
//            }
//        }

        initPluginContext();

        LibManager.getLibLoader().copyPluginSoLib(
                PluginActivity.this,
                mPluginPath,
                getDir(PLUGIN_NATIVE_LIB_PATH, Context.MODE_PRIVATE)
                        .getAbsolutePath());

        try {
            if (savedBundle != null) {
                savedBundle.injectHost();

                mUIData = (UIData) savedBundle.getSerializable(EXTRA_UIDATA);
                postRestoreUIData(savedInstanceState);
            } else {
                if (mPluginContext != null) {
                    mUIData = mPluginContext.createUIData();
                }
            }
        } catch (Exception e) {
            mPluginFinished = true;
        } finally {
            if (savedBundle != null) {
                savedBundle.restoreHost();
            }
        }

        super.onCreate(savedInstanceState);

        if (mPluginFinished) {
            new Handler().post(new Runnable() {

                @Override
                public void run() {
                    finish();
                }

            });
            return;
        }

        // 进入插件，维护bundle环境
        getIntent().setExtrasClassLoader(mPluginClassLoader);
        if (savedInstanceState != null) {
            savedInstanceState.setClassLoader(mPluginClassLoader);
        }

        if (mPluginContext != null && mPluginContext instanceof Activityable) {
            ((Activityable) mPluginContext).onCreate(PluginActivity.this,
                    savedInstanceState);
        }

        onPluginCreate(savedInstanceState);
    }

    /**
     * 返回插件自己的上下文
     *
     * @return
     */
    public PluginContext getPluginContext() {
        return mPluginContext;
    }

    /**
     * 初始化插件路径信息
     */
    private void initPluginPath() {
        if (TextUtils.isEmpty(mPluginPath)) {
            if (mPluginFinished) {
                return;
            }

            String pluginPath = ((PluginableApplication) getApplicationContext())
                    .getPlugin();
            if (TextUtils.isEmpty(pluginPath)) {
                mPluginFinished = true;
                return;
            }
            String pluginEntry = ((PluginableApplication) getApplicationContext())
                    .getPluginEntry();

            // 确认打开成功
            if (!((PluginableApplication) getApplicationContext()).openPlugin(
                    pluginPath, pluginEntry)) {
                mPluginFinished = true;
                return;
            }

            String pluginPackage;
            try {
                PackageInfo packageInfo = getPackageManager()
                        .getPackageArchiveInfo(pluginPath, 0);
                if (packageInfo == null) {
                    return;
                }
                pluginPackage = packageInfo.packageName;
            } catch (Exception e) {
                return;
            }

            mPluginPath = pluginPath;
            mPluginPackage = pluginPackage;
            mPluginClass = pluginEntry;
        }
    }

    /**
     * 初始化插件上下文
     */
    private void initPluginClassLoader() {
        if (mPluginClassLoader != null) {
            Thread.currentThread().setContextClassLoader(mPluginClassLoader);
            return;
        }

        if (TextUtils.isEmpty(mPluginPath)) {
            return;
        }

        ClassLoader localClassLoader = PluginActivity.class.getClassLoader();
        try {
            mPluginClassLoader = PluginClassLoader.loadPlugin(mPluginPath,
                    getFilesDir() + File.separator + PLUGIN_DEX_PATH, null,
                    localClassLoader);
            if (mPluginClassLoader != null) {
                Thread.currentThread()
                        .setContextClassLoader(mPluginClassLoader);
            }
        } catch (Exception e) {
        }
    }

    /**
     * 初始化插件上下文
     */
    private void initPluginContext() {
        if (mPluginContext != null) {
            return;
        }

        if (TextUtils.isEmpty(mPluginPackage) || mPluginClassLoader == null) {
            return;
        }

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null && TextUtils.isEmpty(mPluginClass)) {
            PluginBundle pluginBundle = new PluginBundle(bundle);
            try {
                pluginBundle.injectHost();
                mPluginClass = pluginBundle.getString(EXTRA_PLUGINCONTEXT);
            } finally {
                pluginBundle.restoreHost();
            }
        }

        String pluginContextClass = mPluginClass;
        if (TextUtils.isEmpty(mPluginClass)) {
            pluginContextClass = mPluginPackage + ".Plugin";
        }

        try {
            Class<?> pluginMain = null;
            pluginMain = mPluginClassLoader.loadClass(pluginContextClass);

            Constructor<?> pluginMainConstructor = pluginMain.getConstructor();
            mPluginContext = (PluginContext) pluginMainConstructor
                    .newInstance();
        } catch (Exception e) {
        }
    }

    /**
     * 插件界面创建
     *
     * @param savedInstanceState
     */
    protected void onPluginCreate(Bundle savedInstanceState) {
        if (mPluginContext != null) {
            mPluginContext.initUI(this, savedInstanceState);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mPluginContext != null
                && mPluginContext instanceof KeyEvent.Callback) {
            if (((KeyEvent.Callback) mPluginContext).onKeyDown(keyCode, event)) {
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStart() {
        // 确认插件初始化成功
        initPluginPath();

        if (mPluginContext != null && mPluginContext instanceof Activityable) {
            ((Activityable) mPluginContext).onStart();
        }
        super.onStart();
    }

    @Override
    protected void onResume() {
        // 切换插件资源
        ((PluginableApplication) getApplicationContext())
                .getPluginResources(mPluginPath);

        if (mPluginContext != null && mPluginContext instanceof Activityable) {
            ((Activityable) mPluginContext).onResume();
        }
        super.onResume();
    }

    /**
     * 缓存数据
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        PluginBundle pluginBundle = new PluginBundle(outState);
        pluginBundle.setClassLoader(getApplication().getClassLoader());

        super.onSaveInstanceState(pluginBundle.get());

        pluginBundle.putString(PLUGIN_PATH, mPluginPath);
        pluginBundle.putString(PLUGIN_PACKAGE, mPluginPackage);
        pluginBundle.putString(PLUGIN_CLASS, mPluginClass);
        pluginBundle.putSerializable(EXTRA_UIDATA, mUIData);
//        pluginBundle.putSerializable(PLUGIN_FUNCTION_PROVIDER,
//                mFunctionProvider);
        pluginBundle.putString(PLUGIN_CALLBACK, mPluginCallback);

        if (mPluginContext != null && mPluginContext instanceof Activityable) {
            ((Activityable) mPluginContext).onSaveInstanceState(pluginBundle
                    .get());
        }
    }

    /**
     * 还原数据
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        PluginBundle pluginBundle = new PluginBundle(savedInstanceState);

        try {
            pluginBundle.injectHost();

            super.onRestoreInstanceState(savedInstanceState);

            if (TextUtils.isEmpty(mPluginPath)) {
                mPluginPath = pluginBundle.getString(PLUGIN_PATH);
            }
            if (TextUtils.isEmpty(mPluginPackage)) {
                mPluginPackage = pluginBundle.getString(PLUGIN_PACKAGE);
            }
            if (TextUtils.isEmpty(mPluginClass)) {
                mPluginClass = pluginBundle.getString(PLUGIN_CLASS);
            }
            mUIData = (UIData) pluginBundle.getSerializable(EXTRA_UIDATA);
            postRestoreUIData(savedInstanceState);
//            mFunctionProvider = (FunctionProvider) pluginBundle
//                    .getSerializable(PLUGIN_FUNCTION_PROVIDER);
            mPluginCallback = pluginBundle.getString(PLUGIN_CALLBACK);
        } finally {
            pluginBundle.restoreHost();
        }

        savedInstanceState.setClassLoader(mPluginClassLoader);
        if (mPluginContext != null && mPluginContext instanceof Activityable) {
            ((Activityable) mPluginContext).onRestoreInstanceState(pluginBundle
                    .get());
        }
    }

    /**
     * uidata恢复之后，通常处理全局用户数据
     */
    protected void postRestoreUIData(Bundle savedInstanceState) {
        // 子类实现
    }

    @Override
    protected void onPause() {
        if (mPluginContext != null && mPluginContext instanceof Activityable) {
            ((Activityable) mPluginContext).onPause();
        }
        super.onPause();

        // 释放插件资源
        ((PluginableApplication) getApplicationContext())
                .releasePluginResources(mPluginPath);
    }

    @Override
    protected void onStop() {
        if (mPluginContext != null && mPluginContext instanceof Activityable) {
            ((Activityable) mPluginContext).onStop();
        }
        super.onStop();

        // 释放插件相关数据
        ((PluginableApplication) getApplicationContext()).releasePlugin(
                mPluginPath, mPluginClass);
    }

    @Override
    protected void onDestroy() {
        if (mPluginContext != null && mPluginContext instanceof Activityable) {
            ((Activityable) mPluginContext).onDestroy();
        }
        super.onDestroy();

        // 释放插件相关数据
        ((PluginableApplication) getApplicationContext()).releasePlugin(
                mPluginPath, mPluginClass);
    }

    @Override
    public void finish() {
        if (!TextUtils.isEmpty(mPluginPath)) {
            // 主动关闭插件
            ((PluginableApplication) getApplicationContext()).closePlugin(
                    mPluginPath, mPluginClass);
        }
        super.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mPluginContext != null && mPluginContext instanceof Activityable) {
            ((Activityable) mPluginContext).onActivityResult(requestCode,
                    resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = super.onTouchEvent(event);
        if (mPluginContext != null && mPluginContext instanceof Activityable) {
            return ((Activityable) mPluginContext).onTouchEvent(event);
        }
        return result;
    }

    @Override
    public Resources getResources() {
        if (mPluginResources == null) {
            initPluginPath();

            mPluginResources = ((PluginableApplication) getApplicationContext())
                    .getPluginResources(mPluginPath);
        }

        Resources res = mPluginResources != null ? mPluginResources : super
                .getResources();
        /*
         * 优化字体设置 20150825 判断字体改变后，才需要更新默认字体
		 */
        Configuration config = res.getConfiguration();
        if (1 != config.fontScale) {
            // 恢复默认字体
            config.fontScale = 1f;
            res.updateConfiguration(config, null);
        }
        return res;
    }

    @Override
    public ClassLoader getClassLoader() {
        if (mPluginClassLoader != null) {
            return mPluginClassLoader;
        }

        initPluginClassLoader();

        return (mPluginClassLoader != null) ? mPluginClassLoader : super
                .getClassLoader();
    }

    @Override
    public Context getApplicationContext() {
        Context appContext = super.getApplicationContext();
        if (!(appContext instanceof PluginableApplication)) {
            throw new IllegalArgumentException(
                    "application must inherit from PluginableApplication.");
        }

        return appContext;
    }

    /**
     * 加载Activity的第一个Fragment，被加载的fragment不入栈
     *
     * @param containerID fragment 父容器ID
     * @param fragment
     */
    public void startFirstFragment(int containerID, PluginFragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.replace(containerID, fragment);
        ft.commitAllowingStateLoss();
    }

    /**
     * 加载Fragment，被加载的fragment入栈
     *
     * @param containerID fragment 父容器ID
     * @param fragment
     */
    public void startFragment(int containerID, PluginFragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        // ft.setCustomAnimations(R.anim.push_right_in, R.anim.push_left_out,
        // R.anim.push_left_in, R.anim.push_right_out);
        // ft.setCustomAnimations(android.R.anim.slide_out_right,
        // android.R.anim.slide_in_left);

        // ft.setCustomAnimations(android.R.anim.slide_in_left,
        // android.R.anim.slide_out_right, arg2, arg3)
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.replace(containerID, fragment, fragment.getClass().getName());
        ft.addToBackStack(fragment.getClass().getName());
        ft.commitAllowingStateLoss();
    }

    /**
     * 退回到指定的fragment
     *
     * @param containerID   fragment 父容器ID
     * @param fragmentClass fragment class
     */
    public void backToFragment(int containerID,
                               Class<? extends Fragment> fragmentClass) {
        String fragmentName = fragmentClass.getName();

        FragmentManager manager = getSupportFragmentManager();
        if (manager.findFragmentByTag(fragmentName) != null) {
            manager.popBackStack(fragmentName, 0);
        } else {
            // pop all fragments in stack and new a fragment
            try {
                boolean pop = true;
                while (pop) {
                    pop = manager.popBackStackImmediate();
                }

                Fragment newFragment = null;
                List<Fragment> fs = manager.getFragments();
                if (fs != null && fs.size() > 0) {
                    for (Fragment f : fs) {
                        if (f != null
                                && f.getClass().getName()
                                .compareTo(fragmentName) == 0) {
                            newFragment = f;
                            break;
                        }
                    }
                }
                if (newFragment == null) {
                    newFragment = fragmentClass.newInstance();
                }

                FragmentTransaction ft = manager.beginTransaction();
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.replace(containerID, newFragment);
                ft.commitAllowingStateLoss();
            } catch (Exception e) {
            }
        }
    }

    /**
     * 判断当前显示的是不是此Fragment
     *
     * @param fragment
     * @return
     */
    public boolean isCurrentFragment(Fragment fragment) {
        return (fragment != null && getLastFragmentName().equals(
                fragment.getClass().getName()));

    }

    /**
     * 判断当前显示的是不是此Fragment
     *
     * @param fragmentName
     * @return
     */
    public boolean isCurrentFragment(String fragmentName) {
        if (TextUtils.isEmpty(fragmentName)) {
            return false;
        }
        return getLastFragmentName().equals(fragmentName);
    }

    /**
     * 获取栈中最后一个Fragment
     *
     * @return
     */
    protected String getLastFragmentName() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            return "";
        }
        FragmentManager.BackStackEntry entry = getSupportFragmentManager().getBackStackEntryAt(
                count - 1);
        return entry.getName();
    }

    /**
     * 释放回调函数
     *
     * @author wyqiuchunlong
     */
    public void releaseCallback() {
        this.mPluginCallback = null;
        this.isCallbackForResult = false;
    }

    /**
     * 设置回调函数 startActivityForResult
     *
     * @param callback
     * @author wyqiuchunlong
     */
    public void setCallbackForResult(String callback) {
        this.mPluginCallback = callback;
        this.isCallbackForResult = true;
    }

    /**
     * 设置回调函数 startActivity
     *
     * @param callback
     * @author wyqiuchunlong
     */
    public void setCallback(String callback) {
        this.mPluginCallback = callback;
        this.isCallbackForResult = false;
    }

    /**
     * 获取回调键
     *
     * @return
     */
    public String getCallback() {
        return this.mPluginCallback;
    }

    /**
     * 判断是否为forResult方式回调函数
     *
     * @return
     * @author wyqiuchunlong
     */
    public boolean isCallbackForResult() {
        return this.isCallbackForResult;
    }

    /**
     * 启动插件上下文
     *
     * @param intent
     */
    public void startPluginContext(Intent intent) {
        startPluginContext(intent, -1);
    }

    /**
     * 启动插件上下文
     *
     * @param intent
     */
    public void startPluginContext(Intent intent, int requestCode) {
        if (TextUtils.isEmpty(mPluginPath)) {
            return;
        }

        try {
            ComponentName component = intent.getComponent();
            String targetClass = component.getClassName();
            Class<?> pluginContextClass = mPluginClassLoader
                    .loadClass(targetClass);
            if (!PluginContext.class.isAssignableFrom(pluginContextClass)) {
                throw new IllegalArgumentException(
                        "target class must inherit from PluginContext.");
            }
            intent.putExtra(EXTRA_PLUGINCONTEXT, targetClass);
            intent.setClassName(this, this.getClass().getCanonicalName());

            if (((PluginableApplication) getApplication()).openPlugin(
                    mPluginPath, targetClass)) {
                startActivityForResult(intent, requestCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        View view = PluginLayoutInflater.from(this).inflate(layoutResID, null);
        setContentView(view);
    }

}
