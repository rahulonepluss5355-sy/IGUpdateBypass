package com.igbypass.module;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {

    public static boolean moduleActive = false;

    protected boolean isModuleActive() {
        return moduleActive;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tvStatus = findViewById(R.id.tv_status);
        TextView tvHint = findViewById(R.id.tv_hint);

        if (isModuleActive()) {
            tvStatus.setText("Module is ACTIVE");
            tvStatus.setTextColor(0xFF4CAF50);
            tvHint.setText("After enabling, reboot or force-stop MyInsta.");
        } else {
            tvStatus.setText("Module is NOT ACTIVE");
            tvStatus.setTextColor(0xFFF44336);
            tvHint.setText("Enable this module in LSPosed/Xposed Manager, then reboot.");
        }
    }
}
