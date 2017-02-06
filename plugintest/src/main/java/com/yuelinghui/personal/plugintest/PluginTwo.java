package com.yuelinghui.personal.plugintest;

import android.os.Bundle;

import com.yuelinghui.personal.frame.ui.CPPlugin;
import com.yuelinghui.personal.frame.ui.ErrorFragment;
import com.yuelinghui.personal.plugin.PluginActivity;
import com.yuelinghui.personal.plugin.UIData;

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
