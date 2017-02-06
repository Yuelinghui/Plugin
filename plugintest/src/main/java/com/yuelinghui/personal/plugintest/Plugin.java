package com.yuelinghui.personal.plugintest;

import android.os.Bundle;

import com.yuelinghui.personal.frame.ui.CPPlugin;
import com.yuelinghui.personal.plugin.PluginActivity;
import com.yuelinghui.personal.plugin.UIData;


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
