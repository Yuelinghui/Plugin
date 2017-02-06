package com.yuelinghui.personal.cpplugin;

import android.app.Activity;
import android.support.v4.app.Fragment;


/**
 * Created by yuelinghui on 17/2/6.
 */

public class PluginFragment extends Fragment {


    /**
     * 所依附的Activity
     */
    protected PluginActivity mActivity;
    /**
     * 业务数据，插件数据的全局访问点、存储、恢复
     */
    protected UIData mUiData;

    @Override
    public void onAttach(Activity activity) {
        if (activity instanceof PluginActivity) {
            mActivity = (PluginActivity) activity;
        }
        super.onAttach(activity);

    }
}
