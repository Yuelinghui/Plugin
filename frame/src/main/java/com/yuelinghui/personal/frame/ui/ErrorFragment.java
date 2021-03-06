package com.yuelinghui.personal.frame.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.yuelinghui.personal.frame.R;
import com.yuelinghui.personal.plugin.Activityable;
import com.yuelinghui.personal.plugin.PluginContext;
import com.yuelinghui.personal.plugin.PluginLayoutInflater;

/**
 * 错误页面
 * 
 * @author xingtongju
 */
public class ErrorFragment extends CPFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LayoutInflater layoutInflater = PluginLayoutInflater.from(getActivity());
		View view = layoutInflater.inflate(R.layout.common_error_fragment,
				container, false);
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mActivity != null) {
					PluginContext context = mActivity.getPluginContext();
					if (context != null && context instanceof Activityable) {
						// 点我刷新的点击事件
					}
				}
			}
		});

		return view;
	}
}
