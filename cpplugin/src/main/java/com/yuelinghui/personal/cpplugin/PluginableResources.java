package com.yuelinghui.personal.cpplugin;

import android.content.res.AssetFileDescriptor;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Movie;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

import java.io.InputStream;

/**
 * Created by yuelinghui on 17/2/6.
 */

public class PluginableResources extends Resources {



    private Resources mPluginResources;
    private Resources mAppResources;

    public PluginableResources(Resources pluginRes, Resources appRes) {
        super(pluginRes.getAssets(), pluginRes.getDisplayMetrics(), pluginRes
                .getConfiguration());

        mPluginResources = pluginRes;
        mAppResources = appRes;
    }

    public Resources getPluginResources() {
        return mPluginResources;
    }

    public Resources getAppResources() {
        return mAppResources;
    }

    public CharSequence getText(int id) throws NotFoundException {
        try {
            return mPluginResources.getText(id);
        } catch (NotFoundException e) {
            return mAppResources.getText(id);
        }
    }

    public CharSequence getQuantityText(int id, int quantity)
            throws NotFoundException {
        try {
            return mPluginResources.getQuantityText(id, quantity);
        } catch (NotFoundException e) {
            return mAppResources.getQuantityText(id, quantity);
        }
    }

    public String getString(int id) throws NotFoundException {
        try {
            return mPluginResources.getString(id);
        } catch (NotFoundException e) {
            return mAppResources.getString(id);
        }
    }

    public String getString(int id, Object... formatArgs)
            throws NotFoundException {
        try {
            return mPluginResources.getString(id, formatArgs);
        } catch (NotFoundException e) {
            return mAppResources.getString(id, formatArgs);
        }
    }

    public String getQuantityString(int id, int quantity, Object... formatArgs)
            throws NotFoundException {
        try {
            return mPluginResources.getQuantityString(id, quantity, formatArgs);
        } catch (NotFoundException e) {
            return mAppResources.getQuantityString(id, quantity, formatArgs);
        }
    }

    public String getQuantityString(int id, int quantity)
            throws NotFoundException {
        return getQuantityText(id, quantity).toString();
    }

    public CharSequence getText(int id, CharSequence def) {
        try {
            return mPluginResources.getText(id, def);
        } catch (NotFoundException e) {
            return mAppResources.getText(id, def);
        }
    }

    public CharSequence[] getTextArray(int id) throws NotFoundException {
        try {
            return mPluginResources.getTextArray(id);
        } catch (NotFoundException e) {
            return mAppResources.getTextArray(id);
        }
    }

    public String[] getStringArray(int id) throws NotFoundException {
        try {
            return mPluginResources.getStringArray(id);
        } catch (NotFoundException e) {
            return mAppResources.getStringArray(id);
        }
    }

    public int[] getIntArray(int id) throws NotFoundException {
        try {
            return mPluginResources.getIntArray(id);
        } catch (NotFoundException e) {
            return mAppResources.getIntArray(id);
        }
    }

    public TypedArray obtainTypedArray(int id) throws NotFoundException {
        try {
            return mPluginResources.obtainTypedArray(id);
        } catch (NotFoundException e) {
            return mAppResources.obtainTypedArray(id);
        }
    }

    public float getDimension(int id) throws NotFoundException {
        try {
            return mPluginResources.getDimension(id);
        } catch (NotFoundException e) {
            return mAppResources.getDimension(id);
        }
    }

    public int getDimensionPixelOffset(int id) throws NotFoundException {
        try {
            return mPluginResources.getDimensionPixelOffset(id);
        } catch (NotFoundException e) {
            return mAppResources.getDimensionPixelOffset(id);
        }
    }

    public int getDimensionPixelSize(int id) throws NotFoundException {
        try {
            return mPluginResources.getDimensionPixelSize(id);
        } catch (NotFoundException e) {
            return mAppResources.getDimensionPixelSize(id);
        }
    }

    public float getFraction(int id, int base, int pbase) {
        try {
            return mPluginResources.getFraction(id, base, pbase);
        } catch (NotFoundException e) {
            return mAppResources.getFraction(id, base, pbase);
        }
    }

    public Drawable getDrawable(int id) throws NotFoundException {
        try {
            return mPluginResources.getDrawable(id);
        } catch (NotFoundException e) {
            return mAppResources.getDrawable(id);
        }
    }

    public Drawable getDrawableForDensity(int id, int density)
            throws NotFoundException {
        try {
            return mPluginResources.getDrawableForDensity(id, density);
        } catch (NotFoundException e) {
            return mAppResources.getDrawableForDensity(id, density);
        }
    }

    public Movie getMovie(int id) throws NotFoundException {
        try {
            return mPluginResources.getMovie(id);
        } catch (NotFoundException e) {
            return mAppResources.getMovie(id);
        }
    }

    public int getColor(int id) throws NotFoundException {
        try {
            return mPluginResources.getColor(id);
        } catch (NotFoundException e) {
            return mAppResources.getColor(id);
        }
    }

    public ColorStateList getColorStateList(int id) throws NotFoundException {
        try {
            return mPluginResources.getColorStateList(id);
        } catch (NotFoundException e) {
            return mAppResources.getColorStateList(id);
        }
    }

    public boolean getBoolean(int id) throws NotFoundException {
        try {
            return mPluginResources.getBoolean(id);
        } catch (NotFoundException e) {
            return mAppResources.getBoolean(id);
        }
    }

    public int getInteger(int id) throws NotFoundException {
        try {
            return mPluginResources.getInteger(id);
        } catch (NotFoundException e) {
            return mAppResources.getInteger(id);
        }
    }

    public XmlResourceParser getLayout(int id) throws NotFoundException {
        try {
            return mPluginResources.getLayout(id);
        } catch (NotFoundException e) {
            return mAppResources.getLayout(id);
        }
    }

    public XmlResourceParser getAnimation(int id) throws NotFoundException {
        try {
            return mPluginResources.getAnimation(id);
        } catch (NotFoundException e) {
            return mAppResources.getAnimation(id);
        }
    }

    public XmlResourceParser getXml(int id) throws NotFoundException {
        try {
            return mPluginResources.getXml(id);
        } catch (NotFoundException e) {
            return mAppResources.getXml(id);
        }
    }

    public InputStream openRawResource(int id) throws NotFoundException {
        try {
            return mPluginResources.openRawResource(id);
        } catch (NotFoundException e) {
            return mAppResources.openRawResource(id);
        }
    }

    public InputStream openRawResource(int id, TypedValue value)
            throws NotFoundException {
        try {
            return mPluginResources.openRawResource(id, value);
        } catch (NotFoundException e) {
            return mAppResources.openRawResource(id, value);
        }
    }

    public AssetFileDescriptor openRawResourceFd(int id)
            throws NotFoundException {
        try {
            return mPluginResources.openRawResourceFd(id);
        } catch (NotFoundException e) {
            return mAppResources.openRawResourceFd(id);
        }
    }

    public void getValue(int id, TypedValue outValue, boolean resolveRefs)
            throws NotFoundException {
        try {
            mPluginResources.getValue(id, outValue, resolveRefs);
        } catch (NotFoundException e) {
            mAppResources.getValue(id, outValue, resolveRefs);
        }
    }

    public String getResourceName(int resid) throws NotFoundException {
        try {
            return mPluginResources.getResourceName(resid);
        } catch (NotFoundException e) {
            return mAppResources.getResourceName(resid);
        }
    }

    public String getResourcePackageName(int resid) throws NotFoundException {
        try {
            return mPluginResources.getResourcePackageName(resid);
        } catch (NotFoundException e) {
            return mAppResources.getResourcePackageName(resid);
        }
    }

    public String getResourceTypeName(int resid) throws NotFoundException {
        try {
            return mPluginResources.getResourceTypeName(resid);
        } catch (NotFoundException e) {
            return mAppResources.getResourceTypeName(resid);
        }
    }

    public String getResourceEntryName(int resid) throws NotFoundException {
        try {
            return mPluginResources.getResourceEntryName(resid);
        } catch (NotFoundException e) {
            return mAppResources.getResourceEntryName(resid);
        }
    }

}
