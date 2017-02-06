package com.yuelinghui.personal.host;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;


/**
 * Created by yuelinghui on 17/2/6.
 */

public class ModuleActivity extends PluginActivity {


    /**
     * 模块信息
     */
    public static final String EXTRA_MODULE = "com.wangyin.payment.module.EXTRA.MODULE";

    @Override
    protected void onPluginCreate(Bundle savedInstanceState) {
        if (mPluginContext != null) {
            int errTipResId = 0;
            // 添加functionProvider
            // 初始化ui
            try {
                mPluginContext.initUI(this, savedInstanceState);
            } catch (Exception e) {
                errTipResId = -1;
            }

            if (errTipResId != 0) {

                new Handler().post(new Runnable() {

                    @Override
                    public void run() {
                        finish();
                    }

                });

                return;
            }
        }
    }
     /**
     * 页面显示出来时，根据回调函数状态调用回调函数
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        // 获取焦点，存在回调函数，是startActivity方式的回调
        if (hasFocus && !TextUtils.isEmpty(getCallback())
                && mPluginContext != null && !isCallbackForResult()) {
            mPluginContext.onFunctionResult(getCallback(), null);
            releaseCallback();
        }
    }

}
