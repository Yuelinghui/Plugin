package com.yuelinghui.personal.plugintest;

import android.os.Bundle;

import com.cpframe.CPPlugin;
import com.yuelinghui.personal.cpplugin.PluginActivity;
import com.yuelinghui.personal.cpplugin.UIData;

public class Plugin extends CPPlugin {

    @Override
    public void initUI(PluginActivity hostActivity, Bundle savedInstanceState) {
        super.initUI(hostActivity, savedInstanceState);
        if (hostActivity != null) {
            startFirstFragment(new TestFragment());
        }
    }

    @Override
    public UIData createUIData() {
        return null;
    }
}
