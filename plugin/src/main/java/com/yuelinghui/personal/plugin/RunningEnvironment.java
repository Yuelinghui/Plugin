package com.yuelinghui.personal.plugin;

import android.app.Application;
import android.content.Context;

import java.lang.reflect.Field;
import java.util.concurrent.Callable;

/**
 * Created by yuelinghui on 17/2/6.
 */

public class RunningEnvironment {


    /**
     * 全局静态context
     */
    public static Context sAppContext = null;

    /**
     * 初始化方法
     *
     * @param app
     */
    public static void init(Application app) {
        if (sAppContext == null) {
            sAppContext = app.getApplicationContext();
        }
    }

    /**
     * app环境下执行装载类（改进systemClassLoader装载和appClassLoader一致）
     * 部分装载系统内部使用了systemClassLoader（例如，Serializable的装载）
     *
     * 只适用于无关联的类定义，有层次关系的定义慎用
     *
     * @param classCaller
     * @return 装载的类
     * @throws Exception
     */
    public static <T> T callClass(Callable<T> classCaller) throws Exception {

        if (classCaller == null) {
            throw new IllegalArgumentException("classCaller must not be null.");
        }

        ClassLoader sysClassLoader = RunningEnvironment.class.getClassLoader();
        ClassLoader appClassLoader = sAppContext.getClassLoader();
        if (sysClassLoader == appClassLoader) {
            return classCaller.call();
        }

        // systemClassLoader模拟appClassLoader，调用装载类后，撤回现有分类器

        T classObj = null;
        // sysClassLoader = system + boot
        // appClassLoader = app + system + boot
        synchronized (sysClassLoader) {
            Field parentField = null;
            ClassLoader bootClassLoader = sysClassLoader.getParent();
            ClassLoader diffClassLoader = null;
            try {
                // 修改systemclassloader链式关系
                parentField = ClassLoader.class.getDeclaredField("parent");
                parentField.setAccessible(true);
                if (parentField.getType() != ClassLoader.class) {
                    parentField = null;
                } else {
                    // 查找共同的parent
                    diffClassLoader = appClassLoader;
                    while (diffClassLoader != null
                            && !sysClassLoader.equals(diffClassLoader
                            .getParent())) {
                        diffClassLoader = diffClassLoader.getParent();
                    }
                    if (diffClassLoader != null) {
                        // appClassLoader = app + boot
                        // sysClassLoader = system + appClassLoader
                        parentField.set(diffClassLoader, bootClassLoader);
                        parentField.set(sysClassLoader, appClassLoader);
                    }
                }
            } catch (Exception e) {
                // e.printStackTrace();
                parentField = null;
            }

            try {
                classObj = classCaller.call();
            } finally {
                if (parentField != null) {
                    // 还原systemclassloader链式关系
                    try {
                        if (diffClassLoader != null) {
                            parentField.set(sysClassLoader, bootClassLoader);
                            parentField.set(diffClassLoader, sysClassLoader);
                        }

                        parentField.setAccessible(false);
                    } catch (Exception e) {
                        // e.printStackTrace();
                    }
                }
            }
        }

        return classObj;
    }

    /**
     * 为指定的classloader分配APP的ClassLoader
     *
     * @param src
     * @return 可能返回自身或者appclassloader
     */
    public static ClassLoader allocateAppClassLoader(ClassLoader src) {
        ClassLoader appClassLoader = sAppContext.getClassLoader();

        if (src != null) {
            // 遍历查找是否包含appclassloader
            ClassLoader cl = src;
            while (cl != null) {
                if (cl.equals(appClassLoader)) {
                    return src;
                }
                cl = cl.getParent();
            }
        }

        return appClassLoader;
    }
}
