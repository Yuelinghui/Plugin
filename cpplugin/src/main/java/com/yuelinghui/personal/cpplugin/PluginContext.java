package com.yuelinghui.personal.cpplugin;

import android.os.Bundle;


/**
 * Created by yuelinghui on 17/2/6.
 */

public interface PluginContext {


    /**
     * 插件初始化
     *
     *            账户信息接口
     * @param hostActivity
     *            宿主Activity
     *
     * @param savedInstanceState
     */
    void initUI(PluginActivity hostActivity,
                Bundle savedInstanceState);

    /**
     * 生成插件内业务数据
     *
     * @see UIData
     *
     * @return 业务数据
     */
    UIData createUIData();

    /**
     * module调用客户端的函数后回调
     *
     * @param jsonValue
     */
    public void onFunctionResult(String requestCode, String jsonValue);
}
