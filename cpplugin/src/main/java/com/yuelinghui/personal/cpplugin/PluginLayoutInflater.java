package com.yuelinghui.personal.cpplugin;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Created by yuelinghui on 17/2/6.
 */

public class PluginLayoutInflater {

    private static LayoutInflater mInflater = null;

    public static LayoutInflater from(Context context) {

        mInflater = LayoutInflater.from(context).cloneInContext(context);
        if (mInflater == null) {
            throw new AssertionError("PluginLayoutInflater not found.");
        }
        final LayoutInflater.Factory factory = mInflater.getFactory();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                && factory != null) {
            mInflater.setFactory2(new LayoutInflater.Factory2() {

                @Override
                public View onCreateView(String name, Context context,
                                         AttributeSet attrs) {
                    View view = createViewFactory(context, name, attrs);
                    return view;
                }

                @Override
                public View onCreateView(View parent, String name,
                                         Context context, AttributeSet attrs) {
                    View view = createViewFactory(context, name, attrs);
                    return view;
                }
            });
        } else {
            mInflater.setFactory(new LayoutInflater.Factory() {

                @Override
                public View onCreateView(String name, Context context,
                                         AttributeSet attrs) {
                    View view = createViewFactory(context, name, attrs);
                    return view;
                }
            });
        }

        return mInflater;
    }

    private static View createViewFactory(Context context, String name,
                                          AttributeSet attrs) {
        if (-1 == name.indexOf(".")) {
            return null;
        }
        try {
            View view = mInflater.createView(name, null, attrs);
            if (clearLayoutInflaterMap(name)) {
                return view;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 清除View在Map中的缓存
     *
     * @param name
     * @return
     */
    @SuppressWarnings("unchecked")
    private static boolean clearLayoutInflaterMap(String name) {
        Field field = null;
        try {
            field = LayoutInflater.class.getDeclaredField("sConstructorMap");
            if (field == null) {
                return false;
            }
            field.setAccessible(true);
            HashMap<String, Constructor<? extends View>> map;
            map = (HashMap<String, Constructor<? extends View>>) field
                    .get(null);
            if (map == null || map.size() <= 0 || !map.containsKey(name)) {
                return true;
            }
            map.remove(name);
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (field != null) {
                    field.setAccessible(false);
                }
            } catch (Exception ce) {
            }
        }
    }
}
