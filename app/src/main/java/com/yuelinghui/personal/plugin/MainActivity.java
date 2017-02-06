package com.yuelinghui.personal.plugin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

import com.yuelinghui.personal.cpplugin.PluginableApplication;

public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.btn_open);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((PluginableApplication) getApplicationContext())
                        .openPlugin("")) {
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, ModuleActivity.class);
                    startActivity(intent);
                }
            }
        });
    }
}
