package com.yuelinghui.personal.plugintest;

import android.os.Bundle;

import com.cpframe.CPPlugin;
import com.cpframe.ui.ErrorFragment;
import com.yuelinghui.personal.cpplugin.PluginActivity;
import com.yuelinghui.personal.cpplugin.UIData;

/**
 * Created by yuelinghui on 17/2/6.
 */

public class PluginTwo extends CPPlugin {

    @Override
    public void initUI(PluginActivity hostActivity, Bundle savedInstanceState) {
        super.initUI(hostActivity, savedInstanceState);
        if (hostActivity != null) {
            startFirstFragment(new ErrorFragment());
        }
    }

    @Override
    public UIData createUIData() {
        return null;
    }
}
