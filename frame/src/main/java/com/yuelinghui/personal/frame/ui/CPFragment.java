package com.yuelinghui.personal.frame.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.yuelinghui.R;
import com.yuelinghui.personal.host.PluginActivity;
import com.yuelinghui.personal.host.PluginContext;
import com.yuelinghui.personal.host.PluginFragment;
import com.yuelinghui.personal.host.UIData;

import java.util.List;

public class CPFragment extends PluginFragment {

	/**
	 * 缓存数据
	 */
	protected UIData mUIData = null;

	/**
	 * 父activity
	 */
	protected PluginActivity mActivity = null;

	/**
	 * 插件的Context，可转化成Plugin
	 */
	protected PluginContext mPluginContext;

	/**
	 * 设置的title
	 */
	private String mTitle = null;
	/**
	 * 是否设置了title
	 */
	private boolean mHasSetTitle = false;
	/**
	 * 埋点名称
	 */
	private String mBuryName = null;
	/**
	 * 是否埋点
	 */
	private boolean mIsBury = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.mActivity = (PluginActivity) getActivity();
		this.mUIData = mActivity.mUIData;
		this.mPluginContext = mActivity.getPluginContext();
	}

	/**
	 * 退回到上一个fragment
	 * 
	 * @author wyqiuchunlong
	 */
	public void backToFragment() {
		try {
			mActivity.getSupportFragmentManager().popBackStack();
		} catch (Exception e) {
		}
	}

	public void backToFragmentImmediate() {
		try {
			mActivity.getSupportFragmentManager().popBackStackImmediate();
		} catch (Exception e) {
		}
	}

	/**
	 * 退回到指定的fragment之前的都清空
	 * 
	 * @author wyqiuchunlong
	 * @param clazz
	 */
	public void backToFragment(Class<? extends Fragment> clazz) {
		String fragmentName = clazz.getName();
		FragmentManager manager = mActivity.getSupportFragmentManager();
		try {
			boolean pop = true;
			while (pop) {
				pop = manager.popBackStackImmediate();
			}
			Fragment newFragment = null;
			List<Fragment> fs = manager.getFragments();
			if (fs != null && fs.size() > 0) {
				for (Fragment f : fs) {
					if (f != null
							&& f.getClass().getName().compareTo(fragmentName) == 0) {
						newFragment = f;
						break;
					}
				}
			}
			if (newFragment == null) {
				newFragment = clazz.newInstance();
			}
			FragmentTransaction ft = manager.beginTransaction();
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.replace(R.id.fragment_container, newFragment);
			ft.commitAllowingStateLoss();
		} catch (Exception e) {
		}
	}

	/**
	 * 退回到指定的fragment，之前的不清空
	 * 
	 * @author wyqiuchunlong
	 * @param clazz
	 */
	public void backToStackFragment(Class<? extends Fragment> clazz) {
		String fragmentName = clazz.getName();
		FragmentManager manager = mActivity.getSupportFragmentManager();
		if (manager.findFragmentByTag(fragmentName) != null) {
			manager.popBackStack(fragmentName, 0);
		} else {
			try {
				boolean pop = true;
				while (pop) {
					pop = manager.popBackStackImmediate();
				}
				Fragment newFragment = null;
				List<Fragment> fs = manager.getFragments();
				if (fs != null && fs.size() > 0) {
					for (Fragment f : fs) {
						if (f != null
								&& f.getClass().getName()
										.compareTo(fragmentName) == 0) {
							newFragment = f;
							break;
						}
					}
				}
				if (newFragment == null) {
					newFragment = clazz.newInstance();
				}
				FragmentTransaction ft = manager.beginTransaction();
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				ft.replace(R.id.fragment_container, newFragment);
				ft.commitAllowingStateLoss();
			} catch (Exception e) {
			}
		}
	}


	/**
	 * 
	 * @author muzhi1991
	 * 
	 */
	public interface FunctionResultListener {
		public void onFunctionResult(String requestCode, String jsonValue);
	}

	public boolean onBackPressed() {
		return false;
	}

	/**
	 * 设置页面埋点
	 * 
	 * @param buryName
	 */
	public void setBuryName(String buryName) {
		mBuryName = buryName;
	}

	/**
	 * 获取页面埋点
	 * 
	 * @return
	 */
	public String getBuryName() {
		if (mBuryName == null) {
			if (mHasSetTitle) {
				return mTitle;
			}
		} else {
			return mBuryName;
		}
		return null;
	}

}
