package com.yuelinghui.personal.cpplugin;

import java.io.Serializable;

/**
 * Created by yuelinghui on 17/2/6.
 */

public class PluginInfo implements Serializable {


    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * 插件路径
     */
    public String path;
    /**
     * 插件入口
     */
    public String entry;
    /**
     * 插件是否处于活动状态
     */
    public boolean active = true;

    /**
     * Constructor
     */
    public PluginInfo(String path, String entry) {
        this.path = path;
        this.entry = entry;
    }
}
