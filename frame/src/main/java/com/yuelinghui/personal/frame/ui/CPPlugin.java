package com.yuelinghui.personal.frame.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.yuelinghui.personal.frame.R;
import com.yuelinghui.personal.plugin.Activityable;
import com.yuelinghui.personal.plugin.PluginActivity;
import com.yuelinghui.personal.plugin.PluginContext;
import com.yuelinghui.personal.plugin.PluginFragment;
import com.yuelinghui.personal.plugin.UIData;

/**
 * 
 * @author shixiaoqiang
 * 
 */
public abstract class CPPlugin implements PluginContext, Activityable {

	/**
	 * 获取账号信息
	 */
	public static final String RESULT_TAG_GET_INFO = "GET_INFO";
	/**
	 * 登录
	 */
	public static final String LOGIN = "LOGIN";
	/**
	 * 结果成功，startActivityForResult调用
	 */
	public final static int RESULT_SUCCESS = 1024;
	/**
	 * 结果失败，startActivityForResult调用
	 */
	public final static int RESULT_FAIL = 1023;
	/**
	 * 通用 activity 布局（无滚动，需Fragment自己处理滚动）
	 */
	final static public int FRAGMENT_LAYOUT = R.layout.common_activity;
	/**
	 * 普通 activity 布局（有滚动，Fragment不能包含滚动视图）
	 */
	final static public int SCROLL_LAYOUT = R.layout.common_activity_withscroll;
	public PluginActivity mHostActivity;
	/**
	 * 业务数据
	 */
	public UIData mUIData = null;
	/**
	 * 标题
	 */
	public TextView mTitleTxt = null;
	/**
	 * 标题---父布局
	 */
	public View mTitleLayout = null;
	/**
	 * 自定义标题
	 */
	public FrameLayout mTitleCustomLayout = null;
	/**
	 *
	 */
	public ViewGroup mTilteBaseLayout = null;
	/**
	 * 分割线
	 */
	public View mTitleDivider = null;

	/**
	 * 埋点名称
	 */
	private String mBuryName = null;
	/**
	 * 是否埋点
	 */
	private boolean mIsBury = true;

	/**
	 * 设置标题和布局文件
	 *
	 * @param layoutResID
	 * @param title
	 */
	protected void setContentViewAndTitle(int layoutResID, CharSequence title) {
		mHostActivity.setTheme(R.style.Theme_Activity_Common);
		mHostActivity.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		mHostActivity.setContentView(layoutResID);
	}

	/**
	 * 设置标题和布局view
	 *
	 * @param view
	 *            布局view
	 * @param title
	 */
	protected void setContentViewAndTitle(View view, LayoutParams params,
			CharSequence title, int titleBarColor) {
		mHostActivity.setTheme(R.style.Theme_Activity_Common);
		mHostActivity.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		mHostActivity.setContentView(view, params);
	}


	/**
	 * 切换fragment
	 * 
	 * @param fragment
	 */
	public void startFirstFragment(PluginFragment fragment) {

		if (mHostActivity.isFinishing()) {
			return;
		}

		if (fragment != null) {
			mHostActivity.startFirstFragment(R.id.fragment_container, fragment);
		}

	}

	/**
	 * 启动Fragment，不入栈
	 * 
	 * @author wyqiuchunlong
	 * @param fragment
	 */
	public void startFragmentWithoutHistory(Fragment fragment) {

		if (mHostActivity.isFinishing()) {
			return;
		}

		FragmentTransaction ft = mHostActivity.getSupportFragmentManager()
				.beginTransaction();
		ft.setCustomAnimations(R.anim.push_right_in, R.anim.push_left_out,
				R.anim.push_left_in, R.anim.push_right_out);
		ft.replace(R.id.fragment_container, fragment);
		// ft.addToBackStack(fragment.getClass().getName());
		ft.commitAllowingStateLoss();
	}

	/**
	 * 切换fragment
	 * 
	 * @param fragment
	 */
	public void startFragmentWithoutAnim(Fragment fragment) {

		if (mHostActivity.isFinishing()) {
			return;
		}
		FragmentTransaction ft = mHostActivity.getSupportFragmentManager()
				.beginTransaction();
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.replace(R.id.fragment_container, fragment);
		ft.addToBackStack(fragment.getClass().getName());
		ft.commitAllowingStateLoss();
	}


	@Override
	public void initUI(PluginActivity hostActivity, Bundle savedInstanceState) {
		if (hostActivity != null) {
			mHostActivity = hostActivity;
			mUIData = hostActivity.mUIData;
			// 如果单独插件运行请注释掉下句，在钱包运行打开
			// EventBus.getDefault().register(this);
			setContentViewAndTitle(R.layout.activity_default_host, "");
		}
	}

	@Override
	public void onFunctionResult(String arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onActivityResult(int arg0, int arg1, Intent arg2) {

	}


	@Override
	public void onCreate(PluginActivity arg0, Bundle arg1) {

	}



	@Override
	public void onPause() {
	}

	@Override
	public void onRestart() {

	}

	@Override
	public void onResume() {
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {

	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
	}

	@Override
	public void onStart() {

	}

	@Override
	public void onStop() {

	}

	@Override
	public boolean onTouchEvent(MotionEvent arg0) {
		return false;
	}

	@Override
	public void onWindowAttributesChanged(LayoutParams arg0) {

	}

	@Override
	public void onWindowFocusChanged(boolean arg0) {

	}

	@Override
	public void onDestroy() {

	}
}
