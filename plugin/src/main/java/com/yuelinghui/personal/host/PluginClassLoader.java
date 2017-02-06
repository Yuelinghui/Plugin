package com.yuelinghui.personal.host;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

import dalvik.system.DexClassLoader;

/**
 * Created by yuelinghui on 17/2/6.
 */

public class PluginClassLoader extends DexClassLoader {


    /**
     * 管理插件对应的classloader
     */
    private static final Map<String, WeakReference<ClassLoader>> pluginLoaders = new WeakHashMap<String, WeakReference<ClassLoader>>(
            5);

    /**
     * The child ClassLoader.
     */
    private ClassLoader child;
    /**
     * loadClass的嵌套调用次数，不为0时说明是内部调用，直接返回
     */
    private int nestedLoadLevels;

    private PluginClassLoader(String apkPath, String optimizedDirectory,
                              String libraryPath, ClassLoader parent) {
        super(apkPath, optimizedDirectory, libraryPath, parent);
    }

    /**
     * 装载对应插件
     *
     * @param apkPath
     * @param apkOutputDir
     * @param libPath
     * @param parent
     * @return
     */
    public static ClassLoader loadPlugin(String apkPath, String apkOutputDir,
                                         String libPath, ClassLoader parent) {

        ClassLoader loader = null;

        WeakReference<ClassLoader> l = pluginLoaders.get(apkPath);
        if (l != null) {
            loader = l.get();
        }

        if (loader == null) {
            try {
                File dir = new File(apkOutputDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                ClassLoader pluginLoader = new PluginClassLoader(apkPath,
                        apkOutputDir, libPath, parent);
                if (pluginLoader != null) {
                    loader = pluginLoader;
                    pluginLoaders.put(apkPath, new WeakReference<ClassLoader>(
                            loader));
                }
            } catch (Exception e) {
                // e.printStackTrace();
            }
        }

        return loader;
    }

    /**
     * 查询是否有插件已经装载过，plugin在使用期间不能重新装载 通常可以用于判断是否需要更新插件信息
     *
     * @return
     */
    public static boolean hasPluginLoaded() {
        for (WeakHashMap.Entry<String, WeakReference<ClassLoader>> entry : pluginLoaders
                .entrySet()) {
            WeakReference<ClassLoader> l = entry.getValue();
            if (l != null && l.get() != null) {
                return true;
            }
        }

        return false;
    }

    /**
     * 查询插件是否已经装载过，plugin在使用期间不能重新装载
     *
     * @param apkPath
     *            插件文件路径
     * @return
     */
    public static boolean hasPluginLoaded(String apkPath) {
        WeakReference<ClassLoader> l = pluginLoaders.get(apkPath);
        return (l != null && l.get() != null);
    }

    /**
     * 返回plugin对应的ClassLoader
     *
     * @param apkPath
     *            插件文件路径
     * @return
     */
    public static ClassLoader getClassLoader(String apkPath) {
        WeakReference<ClassLoader> l = pluginLoaders.get(apkPath);
        if (l == null) {
            return null;
        }

        return l.get();
    }

    /**
     * 设置子classloader，用于循环调用
     *
     * @param loader
     */
    public synchronized void setChild(ClassLoader loader) {
        child = loader;
        nestedLoadLevels = 0;
    }

    /**
     * Overridden by subclasses, throws a {@code ClassNotFoundException} by
     * default. This method is called by {@code loadClass} after the parent
     * {@code ClassLoader} has failed to find a loaded class of the same name.
     *
     * @param className
     *            the name of the class to look for.
     * @return the {@code Class} object that is found.
     * @throws ClassNotFoundException
     *             if the class cannot be found.
     */
    protected Class<?> findClass(String className)
            throws ClassNotFoundException {
        if (child == null) {
            nestedLoadLevels = 0;
            return super.findClass(className);
        }

        try {
            return super.findClass(className);
        } catch (ClassNotFoundException e) {
            // 尝试使用child装载
            nestedLoadLevels++;
            return child.loadClass(className);
        } finally {
            nestedLoadLevels = 0;
        }
    }

    /**
     * Loads the class with the specified name, optionally linking it after
     * loading. The following steps are performed:
     * <ol>
     * <li>Call {@link #findLoadedClass(String)} to determine if the requested
     * class has already been loaded.</li>
     * <li>If the class has not yet been loaded: Invoke this method on the
     * parent class loader.</li>
     * <li>If the class has still not been loaded: Call
     * {@link #findClass(String)} to find the class.</li>
     * </ol>
     * <p>
     * <strong>Note:</strong> In the Android reference implementation, the
     * {@code resolve} parameter is ignored; classes are never linked.
     * </p>
     *
     * @return the {@code Class} object.
     * @param className
     *            the name of the class to look for.
     * @param resolve
     *            Indicates if the class should be resolved after loading. This
     *            parameter is ignored on the Android reference implementation;
     *            classes are not resolved.
     * @throws ClassNotFoundException
     *             if the class can not be found.
     */
    protected Class<?> loadClass(String className, boolean resolve)
            throws ClassNotFoundException {
        if (child != null && nestedLoadLevels > 0) {
            nestedLoadLevels = 0;
            // 再次尝试父类装载器，模拟抛出异常等操作
            return getParent().loadClass(className);
        }

        Class<?> clazz = findLoadedClass(className);

        if (clazz == null) {
            try {
                clazz = getParent().loadClass(className);
            } catch (ClassNotFoundException e) {
                // Don't want to see this.
            }

            if (clazz == null) {
                clazz = findClass(className);
            }
        }

        return clazz;
    }

}
