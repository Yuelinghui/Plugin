package com.yuelinghui.personal.plugintest;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.yuelinghui.personal.cpplugin.PluginFragment;

/**
 * Created by yuelinghui on 17/2/6.
 */
public class TestFragment extends PluginFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_main,container,false);

        Button button = (Button) view.findViewById(R.id.btn_start);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClassName(mActivity, PluginTwo.class.getName());
                mActivity.startPluginContext(intent);
            }
        });
        return view;
    }
}
