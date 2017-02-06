package com.yuelinghui.personal.cpplugin;

import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;


import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Created by yuelinghui on 17/2/6.
 */

public class PluginBundle {


    /**
     * 代理的bundle
     */
    private Bundle mBaseBundle;

    /**
     * host相关classloader
     */
    private ClassLoader mSysClassLoader;
    private ClassLoader mAppClassLoader;
    private Field mParentField;
    private ClassLoader mBootClassLoader;
    private ClassLoader mDiffClassLoader;

    public PluginBundle(Bundle bundle) {
        if (bundle == null) {
            throw new IllegalArgumentException("bundle must not be null");
        }
        // 初始化环境相关classloader
        try {
            RunningEnvironment.callClass(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    return null;
                }
            });
        } catch (Exception e) {
        }
        mBaseBundle = bundle;
    }

    /**
     * 返回内置的Bundle
     *
     * @return
     */
    public Bundle get() {
        return mBaseBundle;
    }

    /**
     * 注入host环境
     */
    public void injectHost() {
        mSysClassLoader = RunningEnvironment.class.getClassLoader();
        mAppClassLoader = RunningEnvironment.sAppContext.getClassLoader();
        if (mSysClassLoader == mAppClassLoader) {
            if (mAppClassLoader instanceof PluginClassLoader) {
                ((PluginClassLoader) mAppClassLoader).setChild(null);
            }
            mParentField = null;
            mBootClassLoader = null;
            mDiffClassLoader = null;
        } else {
            // sysClassLoader = system + boot
            // appClassLoader = app + system + boot
            mParentField = null;
            mBootClassLoader = mSysClassLoader.getParent();
            mDiffClassLoader = null;
            try {
                // 修改systemclassloader链式关系
                mParentField = ClassLoader.class.getDeclaredField("parent");
                mParentField.setAccessible(true);
                if (mParentField.getType() != ClassLoader.class) {
                    mParentField = null;
                } else {
                    // 查找共同的parent
                    mDiffClassLoader = mAppClassLoader;
                    while (mDiffClassLoader != null
                            && !mSysClassLoader.equals(mDiffClassLoader
                            .getParent())) {
                        mDiffClassLoader = mDiffClassLoader.getParent();
                    }
                    if (mDiffClassLoader != null) {
                        // appClassLoader = app + boot
                        // sysClassLoader = system + appClassLoader
                        mParentField.set(mDiffClassLoader, mBootClassLoader);
                        mParentField.set(mSysClassLoader, mAppClassLoader);

                        if (mAppClassLoader instanceof PluginClassLoader) {
                            ((PluginClassLoader) mAppClassLoader)
                                    .setChild(mSysClassLoader);
                        }
                    }
                }
            } catch (Exception e) {
                // e.printStackTrace();
                mParentField = null;

                if (mAppClassLoader instanceof PluginClassLoader) {
                    ((PluginClassLoader) mAppClassLoader).setChild(null);
                }
            }
        }

        setClassLoader(mSysClassLoader);
    }

    /**
     * 恢复host环境
     */
    public void restoreHost() {
        if (mParentField != null) {
            // 还原systemclassloader链式关系
            try {
                if (mDiffClassLoader != null) {
                    mParentField.set(mSysClassLoader, mBootClassLoader);
                    mParentField.set(mDiffClassLoader, mSysClassLoader);

                    if (mAppClassLoader instanceof PluginClassLoader) {
                        ((PluginClassLoader) mAppClassLoader).setChild(null);
                    }
                }

                mParentField.setAccessible(false);
            } catch (Exception e) {
                // e.printStackTrace();
            }

            mParentField = null;
            mBootClassLoader = null;
            mDiffClassLoader = null;
        }
    }

    /**
     * Changes the ClassLoader this Bundle uses when instantiating objects.
     *
     * @param loader
     *            An explicit ClassLoader to use when instantiating objects
     *            inside of the Bundle.
     */
    public void setClassLoader(ClassLoader loader) {
        mBaseBundle.setClassLoader(loader);
    }

    /**
     * Return the ClassLoader currently associated with this Bundle.
     */
    public ClassLoader getClassLoader() {
        return mBaseBundle.getClassLoader();
    }

    /**
     * Returns the number of mappings contained in this Bundle.
     *
     * @return the number of mappings as an int.
     */
    public int size() {
        try {
            return RunningEnvironment.callClass(new Callable<Integer>() {

                @Override
                public Integer call() throws Exception {
                    return mBaseBundle.size();
                }
            });
        } catch (Exception e) {
            // e.printStackTrace();
            return 0;
        }
    }

    /**
     * Returns true if the mapping of this Bundle is empty, false otherwise.
     */
    public boolean isEmpty() {
        try {
            return RunningEnvironment.callClass(new Callable<Boolean>() {

                @Override
                public Boolean call() throws Exception {
                    return mBaseBundle.isEmpty();
                }
            });
        } catch (Exception e) {
            // e.printStackTrace();
            return true;
        }
    }

    /**
     * Removes all elements from the mapping of this Bundle.
     */
    public void clear() {
        // 使用根据当前应用类装载器调整的系统类装载器装载类
        try {
            RunningEnvironment.callClass(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    mBaseBundle.clear();
                    return null;
                }
            });
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    /**
     * Returns true if the given key is contained in the mapping of this Bundle.
     *
     * @param key
     *            a String key
     * @return true if the key is part of the mapping, false otherwise
     */
    public boolean containsKey(final String key) {
        try {
            return RunningEnvironment.callClass(new Callable<Boolean>() {

                @Override
                public Boolean call() throws Exception {
                    return mBaseBundle.containsKey(key);
                }
            });
        } catch (Exception e) {
            // e.printStackTrace();
            return false;
        }
    }

    /**
     * Returns the entry with the given key as an object.
     *
     * @param key
     *            a String key
     * @return an Object, or null
     */
    public Object get(final String key) {
        try {
            return RunningEnvironment.callClass(new Callable<Object>() {

                @Override
                public Object call() throws Exception {
                    return mBaseBundle.get(key);
                }
            });
        } catch (Exception e) {
            // e.printStackTrace();
            return null;
        }
    }

    /**
     * Removes any entry with the given key from the mapping of this Bundle.
     *
     * @param key
     *            a String key
     */
    public void remove(final String key) {
        try {
            RunningEnvironment.callClass(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    mBaseBundle.remove(key);
                    return null;
                }
            });
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    /**
     * Inserts all mappings from the given Bundle into this Bundle.
     *
     * @param map
     *            a Bundle
     */
    public void putAll(final Bundle map) {
        try {
            RunningEnvironment.callClass(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    mBaseBundle.putAll(map);
                    return null;
                }
            });
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    /**
     * Returns a Set containing the Strings used as keys in this Bundle.
     *
     * @return a Set of String keys
     */
    public Set<String> keySet() {
        try {
            return RunningEnvironment.callClass(new Callable<Set<String>>() {

                @Override
                public Set<String> call() throws Exception {
                    return mBaseBundle.keySet();
                }
            });
        } catch (Exception e) {
            // e.printStackTrace();
            return null;
        }
    }

    /**
     * Reports whether the bundle contains any parcelled file descriptors.
     */
    public boolean hasFileDescriptors() {
        try {
            return RunningEnvironment.callClass(new Callable<Boolean>() {

                @Override
                public Boolean call() throws Exception {
                    return mBaseBundle.hasFileDescriptors();
                }
            });
        } catch (Exception e) {
            // e.printStackTrace();
            return false;
        }
    }

    /**
     * Inserts a Boolean value into the mapping of this Bundle, replacing any
     * existing value for the given key. Either key or value may be null.
     *
     * @param key
     *            a String, or null
     * @param value
     *            a Boolean, or null
     */
    public void putBoolean(String key, boolean value) {
        mBaseBundle.putBoolean(key, value);
    }

    /**
     * Inserts a byte value into the mapping of this Bundle, replacing any
     * existing value for the given key.
     *
     * @param key
     *            a String, or null
     * @param value
     *            a byte
     */
    public void putByte(String key, byte value) {
        mBaseBundle.putByte(key, value);
    }

    /**
     * Inserts a char value into the mapping of this Bundle, replacing any
     * existing value for the given key.
     *
     * @param key
     *            a String, or null
     * @param value
     *            a char, or null
     */
    public void putChar(String key, char value) {
        mBaseBundle.putChar(key, value);
    }

    /**
     * Inserts a short value into the mapping of this Bundle, replacing any
     * existing value for the given key.
     *
     * @param key
     *            a String, or null
     * @param value
     *            a short
     */
    public void putShort(String key, short value) {
        mBaseBundle.putShort(key, value);
    }

    /**
     * Inserts an int value into the mapping of this Bundle, replacing any
     * existing value for the given key.
     *
     * @param key
     *            a String, or null
     * @param value
     *            an int, or null
     */
    public void putInt(String key, int value) {
        mBaseBundle.putInt(key, value);
    }

    /**
     * Inserts a long value into the mapping of this Bundle, replacing any
     * existing value for the given key.
     *
     * @param key
     *            a String, or null
     * @param value
     *            a long
     */
    public void putLong(String key, long value) {
        mBaseBundle.putLong(key, value);
    }

    /**
     * Inserts a float value into the mapping of this Bundle, replacing any
     * existing value for the given key.
     *
     * @param key
     *            a String, or null
     * @param value
     *            a float
     */
    public void putFloat(String key, float value) {
        mBaseBundle.putFloat(key, value);
    }

    /**
     * Inserts a double value into the mapping of this Bundle, replacing any
     * existing value for the given key.
     *
     * @param key
     *            a String, or null
     * @param value
     *            a double
     */
    public void putDouble(String key, double value) {
        mBaseBundle.putDouble(key, value);
    }

    /**
     * Inserts a String value into the mapping of this Bundle, replacing any
     * existing value for the given key. Either key or value may be null.
     *
     * @param key
     *            a String, or null
     * @param value
     *            a String, or null
     */
    public void putString(String key, String value) {
        mBaseBundle.putString(key, value);
    }

    /**
     * Inserts a CharSequence value into the mapping of this Bundle, replacing
     * any existing value for the given key. Either key or value may be null.
     *
     * @param key
     *            a String, or null
     * @param value
     *            a CharSequence, or null
     */
    public void putCharSequence(String key, CharSequence value) {
        mBaseBundle.putCharSequence(key, value);
    }

    /**
     * Inserts a Parcelable value into the mapping of this Bundle, replacing any
     * existing value for the given key. Either key or value may be null.
     *
     * @param key
     *            a String, or null
     * @param value
     *            a Parcelable object, or null
     */
    public void putParcelable(String key, Parcelable value) {
        mBaseBundle.putParcelable(key, value);
    }

    /**
     * Inserts an array of Parcelable values into the mapping of this Bundle,
     * replacing any existing value for the given key. Either key or value may
     * be null.
     *
     * @param key
     *            a String, or null
     * @param value
     *            an array of Parcelable objects, or null
     */
    public void putParcelableArray(String key, Parcelable[] value) {
        mBaseBundle.putParcelableArray(key, value);
    }

    /**
     * Inserts a List of Parcelable values into the mapping of this Bundle,
     * replacing any existing value for the given key. Either key or value may
     * be null.
     *
     * @param key
     *            a String, or null
     * @param value
     *            an ArrayList of Parcelable objects, or null
     */
    public void putParcelableArrayList(String key,
                                       ArrayList<? extends Parcelable> value) {
        mBaseBundle.putParcelableArrayList(key, value);
    }

    /**
     * Inserts a SparceArray of Parcelable values into the mapping of this
     * Bundle, replacing any existing value for the given key. Either key or
     * value may be null.
     *
     * @param key
     *            a String, or null
     * @param value
     *            a SparseArray of Parcelable objects, or null
     */
    public void putSparseParcelableArray(String key,
                                         SparseArray<? extends Parcelable> value) {
        mBaseBundle.putSparseParcelableArray(key, value);
    }

    /**
     * Inserts an ArrayList<Integer> value into the mapping of this Bundle,
     * replacing any existing value for the given key. Either key or value may
     * be null.
     *
     * @param key
     *            a String, or null
     * @param value
     *            an ArrayList<Integer> object, or null
     */
    public void putIntegerArrayList(String key, ArrayList<Integer> value) {
        mBaseBundle.putIntegerArrayList(key, value);
    }

    /**
     * Inserts an ArrayList<String> value into the mapping of this Bundle,
     * replacing any existing value for the given key. Either key or value may
     * be null.
     *
     * @param key
     *            a String, or null
     * @param value
     *            an ArrayList<String> object, or null
     */
    public void putStringArrayList(String key, ArrayList<String> value) {
        mBaseBundle.putStringArrayList(key, value);
    }

    /**
     * Inserts an ArrayList<CharSequence> value into the mapping of this Bundle,
     * replacing any existing value for the given key. Either key or value may
     * be null.
     *
     * @param key
     *            a String, or null
     * @param value
     *            an ArrayList<CharSequence> object, or null
     */
    public void putCharSequenceArrayList(String key,
                                         ArrayList<CharSequence> value) {
        mBaseBundle.putCharSequenceArrayList(key, value);
    }

    /**
     * Inserts a Serializable value into the mapping of this Bundle, replacing
     * any existing value for the given key. Either key or value may be null.
     *
     * @param key
     *            a String, or null
     * @param value
     *            a Serializable object, or null
     */
    public void putSerializable(String key, Serializable value) {
        mBaseBundle.putSerializable(key, value);
    }

    /**
     * Inserts a boolean array value into the mapping of this Bundle, replacing
     * any existing value for the given key. Either key or value may be null.
     *
     * @param key
     *            a String, or null
     * @param value
     *            a boolean array object, or null
     */
    public void putBooleanArray(String key, boolean[] value) {
        mBaseBundle.putBooleanArray(key, value);
    }

    /**
     * Inserts a byte array value into the mapping of this Bundle, replacing any
     * existing value for the given key. Either key or value may be null.
     *
     * @param key
     *            a String, or null
     * @param value
     *            a byte array object, or null
     */
    public void putByteArray(String key, byte[] value) {
        mBaseBundle.putByteArray(key, value);
    }

    /**
     * Inserts a short array value into the mapping of this Bundle, replacing
     * any existing value for the given key. Either key or value may be null.
     *
     * @param key
     *            a String, or null
     * @param value
     *            a short array object, or null
     */
    public void putShortArray(String key, short[] value) {
        mBaseBundle.putShortArray(key, value);
    }

    /**
     * Inserts a char array value into the mapping of this Bundle, replacing any
     * existing value for the given key. Either key or value may be null.
     *
     * @param key
     *            a String, or null
     * @param value
     *            a char array object, or null
     */
    public void putCharArray(String key, char[] value) {
        mBaseBundle.putCharArray(key, value);
    }

    /**
     * Inserts an int array value into the mapping of this Bundle, replacing any
     * existing value for the given key. Either key or value may be null.
     *
     * @param key
     *            a String, or null
     * @param value
     *            an int array object, or null
     */
    public void putIntArray(String key, int[] value) {
        mBaseBundle.putIntArray(key, value);
    }

    /**
     * Inserts a long array value into the mapping of this Bundle, replacing any
     * existing value for the given key. Either key or value may be null.
     *
     * @param key
     *            a String, or null
     * @param value
     *            a long array object, or null
     */
    public void putLongArray(String key, long[] value) {
        mBaseBundle.putLongArray(key, value);
    }

    /**
     * Inserts a float array value into the mapping of this Bundle, replacing
     * any existing value for the given key. Either key or value may be null.
     *
     * @param key
     *            a String, or null
     * @param value
     *            a float array object, or null
     */
    public void putFloatArray(String key, float[] value) {
        mBaseBundle.putFloatArray(key, value);
    }

    /**
     * Inserts a double array value into the mapping of this Bundle, replacing
     * any existing value for the given key. Either key or value may be null.
     *
     * @param key
     *            a String, or null
     * @param value
     *            a double array object, or null
     */
    public void putDoubleArray(String key, double[] value) {
        mBaseBundle.putDoubleArray(key, value);
    }

    /**
     * Inserts a String array value into the mapping of this Bundle, replacing
     * any existing value for the given key. Either key or value may be null.
     *
     * @param key
     *            a String, or null
     * @param value
     *            a String array object, or null
     */
    public void putStringArray(String key, String[] value) {
        mBaseBundle.putStringArray(key, value);
    }

    /**
     * Inserts a CharSequence array value into the mapping of this Bundle,
     * replacing any existing value for the given key. Either key or value may
     * be null.
     *
     * @param key
     *            a String, or null
     * @param value
     *            a CharSequence array object, or null
     */
    public void putCharSequenceArray(String key, CharSequence[] value) {
        mBaseBundle.putCharSequenceArray(key, value);
    }

    /**
     * Inserts a Bundle value into the mapping of this Bundle, replacing any
     * existing value for the given key. Either key or value may be null.
     *
     * @param key
     *            a String, or null
     * @param value
     *            a Bundle object, or null
     */
    public void putBundle(String key, Bundle value) {
        mBaseBundle.putBundle(key, value);
    }

    /**
     * Inserts an {@link IBinder} value into the mapping of this Bundle,
     * replacing any existing value for the given key. Either key or value may
     * be null.
     *
     * <p class="note">
     * You should be very careful when using this function. In many places where
     * Bundles are used (such as inside of Intent objects), the Bundle can live
     * longer inside of another process than the process that had originally
     * created it. In that case, the IBinder you supply here will become invalid
     * when your process goes away, and no longer usable, even if a new process
     * is created for you later on.
     * </p>
     *
     * @param key
     *            a String, or null
     * @param value
     *            an IBinder object, or null
     */
//	public void putBinder(String key, IBinder value) {
//		mBaseBundle.putBinder(key, value);
//	}

    /**
     * Returns the value associated with the given key, or false if no mapping
     * of the desired type exists for the given key.
     *
     * @param key
     *            a String
     * @return a boolean value
     */
    public boolean getBoolean(final String key) {
        return getBoolean(key, false);
    }

    /**
     * Returns the value associated with the given key, or defaultValue if no
     * mapping of the desired type exists for the given key.
     *
     * @param key
     *            a String
     * @param defaultValue
     *            Value to return if key does not exist
     * @return a boolean value
     */
    public boolean getBoolean(final String key, final boolean defaultValue) {
        Object o = get(key);
        return (o == null) ? defaultValue : (Boolean) o;
    }

    /**
     * Returns the value associated with the given key, or (byte) 0 if no
     * mapping of the desired type exists for the given key.
     *
     * @param key
     *            a String
     * @return a byte value
     */
    public byte getByte(String key) {
        return getByte(key, (byte) 0);
    }

    /**
     * Returns the value associated with the given key, or defaultValue if no
     * mapping of the desired type exists for the given key.
     *
     * @param key
     *            a String
     * @param defaultValue
     *            Value to return if key does not exist
     * @return a byte value
     */
    public Byte getByte(final String key, final byte defaultValue) {
        Object o = get(key);
        return (o == null) ? defaultValue : (Byte) o;
    }

    /**
     * Returns the value associated with the given key, or (char) 0 if no
     * mapping of the desired type exists for the given key.
     *
     * @param key
     *            a String
     * @return a char value
     */
    public char getChar(String key) {
        return getChar(key, (char) 0);
    }

    /**
     * Returns the value associated with the given key, or defaultValue if no
     * mapping of the desired type exists for the given key.
     *
     * @param key
     *            a String
     * @param defaultValue
     *            Value to return if key does not exist
     * @return a char value
     */
    public char getChar(final String key, final char defaultValue) {
        Object o = get(key);
        return (o == null) ? defaultValue : (Character) o;
    }

    /**
     * Returns the value associated with the given key, or (short) 0 if no
     * mapping of the desired type exists for the given key.
     *
     * @param key
     *            a String
     * @return a short value
     */
    public short getShort(String key) {
        return getShort(key, (short) 0);
    }

    /**
     * Returns the value associated with the given key, or defaultValue if no
     * mapping of the desired type exists for the given key.
     *
     * @param key
     *            a String
     * @param defaultValue
     *            Value to return if key does not exist
     * @return a short value
     */
    public short getShort(String key, short defaultValue) {
        Object o = get(key);
        return (o == null) ? defaultValue : (Short) o;
    }

    /**
     * Returns the value associated with the given key, or 0 if no mapping of
     * the desired type exists for the given key.
     *
     * @param key
     *            a String
     * @return an int value
     */
    public int getInt(String key) {
        return getInt(key, 0);
    }

    /**
     * Returns the value associated with the given key, or defaultValue if no
     * mapping of the desired type exists for the given key.
     *
     * @param key
     *            a String
     * @param defaultValue
     *            Value to return if key does not exist
     * @return an int value
     */
    public int getInt(String key, int defaultValue) {
        Object o = get(key);
        return (o == null) ? defaultValue : (Integer) o;
    }

    /**
     * Returns the value associated with the given key, or 0L if no mapping of
     * the desired type exists for the given key.
     *
     * @param key
     *            a String
     * @return a long value
     */
    public long getLong(String key) {
        return getLong(key, 0L);
    }

    /**
     * Returns the value associated with the given key, or defaultValue if no
     * mapping of the desired type exists for the given key.
     *
     * @param key
     *            a String
     * @param defaultValue
     *            Value to return if key does not exist
     * @return a long value
     */
    public long getLong(String key, long defaultValue) {
        Object o = get(key);
        return (o == null) ? defaultValue : (Long) o;
    }

    /**
     * Returns the value associated with the given key, or 0.0f if no mapping of
     * the desired type exists for the given key.
     *
     * @param key
     *            a String
     * @return a float value
     */
    public float getFloat(String key) {
        return getFloat(key, 0.0f);
    }

    /**
     * Returns the value associated with the given key, or defaultValue if no
     * mapping of the desired type exists for the given key.
     *
     * @param key
     *            a String
     * @param defaultValue
     *            Value to return if key does not exist
     * @return a float value
     */
    public float getFloat(String key, float defaultValue) {
        Object o = get(key);
        return (o == null) ? defaultValue : (Float) o;
    }

    /**
     * Returns the value associated with the given key, or 0.0 if no mapping of
     * the desired type exists for the given key.
     *
     * @param key
     *            a String
     * @return a double value
     */
    public double getDouble(String key) {
        return getDouble(key, 0.0);
    }

    /**
     * Returns the value associated with the given key, or defaultValue if no
     * mapping of the desired type exists for the given key.
     *
     * @param key
     *            a String
     * @param defaultValue
     *            Value to return if key does not exist
     * @return a double value
     */
    public double getDouble(String key, double defaultValue) {
        Object o = get(key);
        return (o == null) ? defaultValue : (Double) o;
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of
     * the desired type exists for the given key or a null value is explicitly
     * associated with the key.
     *
     * @param key
     *            a String, or null
     * @return a String value, or null
     */
    public String getString(String key) {
        return (String) get(key);
    }

    /**
     * Returns the value associated with the given key, or defaultValue if no
     * mapping of the desired type exists for the given key.
     *
     * @param key
     *            a String, or null
     * @param defaultValue
     *            Value to return if key does not exist
     * @return the String value associated with the given key, or defaultValue
     *         if no valid String object is currently mapped to that key.
     */
    public String getString(String key, String defaultValue) {
        final String s = getString(key);
        return (s == null) ? defaultValue : s;
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of
     * the desired type exists for the given key or a null value is explicitly
     * associated with the key.
     *
     * @param key
     *            a String, or null
     * @return a CharSequence value, or null
     */
    public CharSequence getCharSequence(String key) {
        return (CharSequence) get(key);
    }

    /**
     * Returns the value associated with the given key, or defaultValue if no
     * mapping of the desired type exists for the given key.
     *
     * @param key
     *            a String, or null
     * @param defaultValue
     *            Value to return if key does not exist
     * @return the CharSequence value associated with the given key, or
     *         defaultValue if no valid CharSequence object is currently mapped
     *         to that key.
     */
    public CharSequence getCharSequence(String key, CharSequence defaultValue) {
        final CharSequence cs = getCharSequence(key);
        return (cs == null) ? defaultValue : cs;
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of
     * the desired type exists for the given key or a null value is explicitly
     * associated with the key.
     *
     * @param key
     *            a String, or null
     * @return a Bundle value, or null
     */
    public Bundle getBundle(String key) {
        return (Bundle) get(key);
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of
     * the desired type exists for the given key or a null value is explicitly
     * associated with the key.
     *
     * @param key
     *            a String, or null
     * @return a Parcelable value, or null
     */
    @SuppressWarnings("unchecked")
    public <T extends Parcelable> T getParcelable(String key) {
        return (T) get(key);
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of
     * the desired type exists for the given key or a null value is explicitly
     * associated with the key.
     *
     * @param key
     *            a String, or null
     * @return a Parcelable[] value, or null
     */
    public Parcelable[] getParcelableArray(String key) {
        return (Parcelable[]) get(key);
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of
     * the desired type exists for the given key or a null value is explicitly
     * associated with the key.
     *
     * @param key
     *            a String, or null
     * @return an ArrayList<T> value, or null
     */
    @SuppressWarnings("unchecked")
    public <T extends Parcelable> ArrayList<T> getParcelableArrayList(String key) {
        return (ArrayList<T>) get(key);
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of
     * the desired type exists for the given key or a null value is explicitly
     * associated with the key.
     *
     * @param key
     *            a String, or null
     *
     * @return a SparseArray of T values, or null
     */
    @SuppressWarnings("unchecked")
    public <T extends Parcelable> SparseArray<T> getSparseParcelableArray(
            String key) {
        return (SparseArray<T>) get(key);
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of
     * the desired type exists for the given key or a null value is explicitly
     * associated with the key.
     *
     * @param key
     *            a String, or null
     * @return a Serializable value, or null
     */
    public Serializable getSerializable(String key) {
        return (Serializable) get(key);
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of
     * the desired type exists for the given key or a null value is explicitly
     * associated with the key.
     *
     * @param key
     *            a String, or null
     * @return an ArrayList<String> value, or null
     */
    @SuppressWarnings("unchecked")
    public ArrayList<Integer> getIntegerArrayList(String key) {
        return (ArrayList<Integer>) get(key);
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of
     * the desired type exists for the given key or a null value is explicitly
     * associated with the key.
     *
     * @param key
     *            a String, or null
     * @return an ArrayList<String> value, or null
     */
    @SuppressWarnings("unchecked")
    public ArrayList<String> getStringArrayList(String key) {
        return (ArrayList<String>) get(key);
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of
     * the desired type exists for the given key or a null value is explicitly
     * associated with the key.
     *
     * @param key
     *            a String, or null
     * @return an ArrayList<CharSequence> value, or null
     */
    @SuppressWarnings("unchecked")
    public ArrayList<CharSequence> getCharSequenceArrayList(String key) {
        return (ArrayList<CharSequence>) get(key);
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of
     * the desired type exists for the given key or a null value is explicitly
     * associated with the key.
     *
     * @param key
     *            a String, or null
     * @return a boolean[] value, or null
     */
    public boolean[] getBooleanArray(String key) {
        return (boolean[]) get(key);
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of
     * the desired type exists for the given key or a null value is explicitly
     * associated with the key.
     *
     * @param key
     *            a String, or null
     * @return a byte[] value, or null
     */
    public byte[] getByteArray(String key) {
        return (byte[]) get(key);
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of
     * the desired type exists for the given key or a null value is explicitly
     * associated with the key.
     *
     * @param key
     *            a String, or null
     * @return a short[] value, or null
     */
    public short[] getShortArray(String key) {
        return (short[]) get(key);
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of
     * the desired type exists for the given key or a null value is explicitly
     * associated with the key.
     *
     * @param key
     *            a String, or null
     * @return a char[] value, or null
     */
    public char[] getCharArray(String key) {
        return (char[]) get(key);
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of
     * the desired type exists for the given key or a null value is explicitly
     * associated with the key.
     *
     * @param key
     *            a String, or null
     * @return an int[] value, or null
     */
    public int[] getIntArray(String key) {
        return (int[]) get(key);
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of
     * the desired type exists for the given key or a null value is explicitly
     * associated with the key.
     *
     * @param key
     *            a String, or null
     * @return a long[] value, or null
     */
    public long[] getLongArray(String key) {
        return (long[]) get(key);
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of
     * the desired type exists for the given key or a null value is explicitly
     * associated with the key.
     *
     * @param key
     *            a String, or null
     * @return a float[] value, or null
     */
    public float[] getFloatArray(String key) {
        return (float[]) get(key);
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of
     * the desired type exists for the given key or a null value is explicitly
     * associated with the key.
     *
     * @param key
     *            a String, or null
     * @return a double[] value, or null
     */
    public double[] getDoubleArray(String key) {
        return (double[]) get(key);
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of
     * the desired type exists for the given key or a null value is explicitly
     * associated with the key.
     *
     * @param key
     *            a String, or null
     * @return a String[] value, or null
     */
    public String[] getStringArray(String key) {
        return (String[]) get(key);
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of
     * the desired type exists for the given key or a null value is explicitly
     * associated with the key.
     *
     * @param key
     *            a String, or null
     * @return a CharSequence[] value, or null
     */
    public CharSequence[] getCharSequenceArray(String key) {
        return (CharSequence[]) get(key);
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of
     * the desired type exists for the given key or a null value is explicitly
     * associated with the key.
     *
     * @param key
     *            a String, or null
     * @return an IBinder value, or null
     */
    public IBinder getBinder(String key) {
        return (IBinder) get(key);
    }

    /**
     * Report the nature of this Parcelable's contents
     */
    public int describeContents() {
        return mBaseBundle.describeContents();
    }

    /**
     * Writes the Bundle contents to a Parcel, typically in order for it to be
     * passed through an IBinder connection.
     *
     * @param parcel
     *            The parcel to copy this bundle to.
     */
    public void writeToParcel(Parcel parcel, int flags) {
        mBaseBundle.writeToParcel(parcel, flags);
    }

    /**
     * Reads the Parcel contents into this Bundle, typically in order for it to
     * be passed through an IBinder connection.
     *
     * @param parcel
     *            The parcel to overwrite this bundle from.
     */
    public void readFromParcel(Parcel parcel) {
        mBaseBundle.readFromParcel(parcel);
    }

    @Override
    public synchronized String toString() {
        return "PluginBundle: " + mBaseBundle.toString();
    }

}
