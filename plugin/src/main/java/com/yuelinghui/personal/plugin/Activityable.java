package com.yuelinghui.personal.plugin;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.WindowManager;


/**
 * Created by yuelinghui on 17/2/6.
 */

public interface Activityable {


    void onCreate(PluginActivity hostActivity, Bundle savedInstanceState);

    void onStart();

    void onRestart();

    void onActivityResult(int requestCode, int resultCode, Intent data);

    void onResume();

    void onPause();

    void onStop();

    void onDestroy();

    void onSaveInstanceState(Bundle outState);

    void onRestoreInstanceState(Bundle savedInstanceState);

    boolean onTouchEvent(MotionEvent event);

    void onWindowAttributesChanged(WindowManager.LayoutParams params);

    void onWindowFocusChanged(boolean hasFocus);
}
