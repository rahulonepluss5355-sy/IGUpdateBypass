package com.igbypass.module;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.view.Gravity;
import android.util.TypedValue;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(Color.parseColor("#121212"));

        TextView title = new TextView(this);
        title.setText("IG Update Bypass");
        title.setTextColor(Color.WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);

        TextView status = new TextView(this);
        status.setText("Module is NOT active.\nPlease enable it in LSPosed/EdXposed.");
        status.setTextColor(Color.parseColor("#FF5252"));
        status.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        status.setGravity(Gravity.CENTER);
        status.setPadding(32, 16, 32, 16);

        TextView hint = new TextView(this);
        hint.setText("If this text is red, Xposed is not running.\nIf module is active, you will never see this screen.");
        hint.setTextColor(Color.parseColor("#888888"));
        hint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(32, 8, 32, 8);

        layout.addView(title);
        layout.addView(status);
        layout.addView(hint);

        setContentView(layout);
    }
}
